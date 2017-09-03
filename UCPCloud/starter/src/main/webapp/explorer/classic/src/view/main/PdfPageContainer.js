Ext.define('explorer.view.main.PdfPageContainer', {
    extend: 'Ext.view.View',
    xtype: ['pdfpagecontainer'],
    itemSelector: '.container',
    scrollable: true,
    pages: [],
    tpl: [
        '<tpl for=".">',
        '<div class="container" style="position: relative;">',
        '<div class="canvasWrapper" style="background-image:url(/pdfjs-1.1.366-dist/web/images/loading-icon.gif);background-repeat: no-repeat; background-position:center;"></div>',
        '<div class="textLayer"></div>',
        '</div>',
        '</tpl>',
    ],

    initComponent: function () {
        this.pages = [];
        this.callParent();
    },
    scrollable: {
        x: false,
        listeners: {
            scroll: function (scroller, x, y, eOpts) {
                var me = scroller.component;
                var children = me.getEl().dom.children;

                var viewHeight = me.getHeight() + y;
                var offsetHeight = 0;

                var viewCanvas = Ext.Array.findBy(children, function (box) {
                    offsetHeight += box.offsetHeight;
                    return offsetHeight >= viewHeight;
                });
                if (!viewCanvas)return;
                var pageNum = Ext.Array.indexOf(children, viewCanvas) + 1;
                me.drawContextPages(pageNum);
                me.fireEvent('pageChanged', pageNum)
            }
        }
    },

    listeners: {
        itemadd: function (records, index, node, eOpts) {
            var me = this;
            var div = node[0];
            var pageNum = index + 1;

            if (me.drawPagePromise)
                me.drawPagePromise = me.drawPagePromise.then(me.initPage(div, pageNum));
            else
                me.drawPagePromise = Q(me.initPage(div, pageNum)).then();
        }
    },

    scrollIntoView: function (pageNum) {
        var me = this;
        var box = me.getEl().dom.children[pageNum - 1];
        if (!box)return;
        if (pageNum > me.pdfDoc.pdfInfo.numPages)return;
        box.scrollIntoView();
    },

    initPage: function (box, pageNum) {
        var me = this;
        var deferred = Q.defer();
        var scale = 1.0;
        var fit = me.up('pdf').down('toolbar').down('segmentedbutton').getValue();
        me.pdfDoc.getPage(pageNum).then(function (page) {
            if (fit === 'width')scale = 1.0 * me.getWidth() / page.view[2];
            if (fit === 'height')scale = 1.0 * me.getHeight() / page.view[3];
            var viewport = page.getViewport(scale);
            var page = new me.PdfPage({
                box: box,
                pageNum: pageNum,
                page: page,
                viewport: viewport
            });
            me.pages.push(page);
            if (pageNum === 1) {
                page.draw().done();
                deferred.resolve(page);
            }
            else
                deferred.resolve(page);
        }).catch(function (e) {
            deferred.reject(e);
        });
        return deferred.promise;
    },

    getContextPageNum: function () {
        var y = this.getScrollable().getPosition().y;
        var children = this.getEl().dom.children;
        var viewHeight = this.getHeight() + y;
        var offsetHeight = 0;

        var viewCanvas = Ext.Array.findBy(children, function (box) {
            offsetHeight += box.offsetHeight;
            return offsetHeight >= viewHeight;
        });
        if (!viewCanvas)return;
        return Ext.Array.indexOf(children, viewCanvas) + 1;
    },

    drawContextPages: function (pageNum) {
        var me = this;
        if (pageNum === undefined)pageNum = me.getContextPageNum();

        var renders = [];
        Ext.each(me.pages, function (page, index) {
            num = index + 1;
            if (num >= pageNum + 2 || num <= pageNum - 2)
                page.destroy();
            else {
                renders.push(page.draw());
            }

        });

        Q.all(renders).done(function (values) {
            console.log(values)
        }, function (err) {
            console.log(err)
        });
    },

    PdfPage: (function () {
        function PdfPage(option) {
            this.box = option.box;
            this.canvasWrapper = this.box.children[0];
            this.pageNum = option.pageNum;
            this.page = option.page;
            this.viewport = option.viewport;
            this.state = 0;
            this.box.style.height = this.viewport.height + 'px';
            this.box.style.width = this.viewport.width + 'px';
            this.canvasWrapper.style.height = this.viewport.height + 'px';
            this.canvasWrapper.style.width = this.viewport.width + 'px';
            this.textLayer = new TextLayerBuilder({
                textLayerDiv: this.box.children[1],
                pageNumber: this.pageNum,
                viewport: this.viewport
            });
        }

        PdfPage.prototype = {
            destroy: function (whatever) {
                if (!whatever && this.state === 0)return;
                this.page.destroy();
                if(this.canvas && this.state === 2) {
                    this.canvasWrapper.removeChild(this.canvas);
                    delete this.canvas;
                }
                this.state = 0;
            },

            draw: function () {
                var me = this;
                if (me.state !== 0)return;

                this.canvas = document.createElement('canvas');
                this.canvas.width = this.viewport.width;
                this.canvas.height = this.viewport.height;
                var renderContext = {
                    canvasContext: this.canvas.getContext('2d'),
                    viewport: this.viewport
                };
                me.state = 1;
                var deferred = Q.defer();
                var TEXT_LAYER_RENDER_DELAY = 200; // ms
                this.page.render(renderContext).then(function () {
                    me.state = 2;
                    me.canvasWrapper.appendChild(me.canvas);
                    me.page.getTextContent().then(function textContentResolved(textContent) {
                        me.textLayer.setTextContent(textContent);
                        me.textLayer.render(TEXT_LAYER_RENDER_DELAY);
                        deferred.resolve();
                    });
                }).catch(function (e) {
                    deferred.reject(e);
                });
                return deferred.promise;
            },

            reset: function (scale) {

                this.viewport = this.page.getViewport(scale);
                this.box.style.height = this.viewport.height + 'px';
                this.box.style.width = this.viewport.width + 'px';
                this.canvasWrapper.style.height = this.viewport.height + 'px';
                this.canvasWrapper.style.width = this.viewport.width + 'px';

                this.destroy(true);
                this.textLayer.reset(this.viewport);
            }


        }

        var TextLayerBuilder = (function TextLayerBuilderClosure() {
            function TextLayerBuilder(options) {
                this.textLayerDiv = options.textLayerDiv;
                this.renderingDone = false;
                this.divContentDone = false;
                this.pageIdx = options.pageIndex;
                this.pageNumber = this.pageIdx + 1;
                this.viewport = options.viewport;
                this.textDivs = [];
                this.textLayerDiv.style.height = this.viewport.height + 'px';
                this.textLayerDiv.style.width = this.viewport.width + 'px';
            }

            TextLayerBuilder.prototype = {

                MAX_TEXT_DIVS_TO_RENDER: 1000,
                renderLayer: function TextLayerBuilder_renderLayer() {
                    var textLayerFrag = document.createDocumentFragment();
                    var textDivs = this.textDivs;
                    var textDivsLength = textDivs.length;
                    var canvas = document.createElement('canvas');
                    var ctx = canvas.getContext('2d');

                    // No point in rendering many divs as it would make the browser
                    // unusable even after the divs are rendered.
                    if (textDivsLength > this.MAX_TEXT_DIVS_TO_RENDER) {
                        this._finishRendering();
                        return;
                    }

                    var lastFontSize;
                    var lastFontFamily;
                    for (var i = 0; i < textDivsLength; i++) {
                        var textDiv = textDivs[i];
                        if (textDiv.dataset.isWhitespace !== undefined) {
                            continue;
                        }

                        var fontSize = textDiv.style.fontSize;
                        var fontFamily = textDiv.style.fontFamily;

                        // Only build font string and set to context if different from last.
                        if (fontSize !== lastFontSize || fontFamily !== lastFontFamily) {
                            ctx.font = fontSize + ' ' + fontFamily;
                            lastFontSize = fontSize;
                            lastFontFamily = fontFamily;
                        }

                        var width = ctx.measureText(textDiv.textContent).width;
                        if (width > 0) {
                            textLayerFrag.appendChild(textDiv);
                            var transform;
                            if (textDiv.dataset.canvasWidth !== undefined) {
                                // Dataset values come of type string.
                                var textScale = textDiv.dataset.canvasWidth / width;
                                transform = 'scaleX(' + textScale + ')';
                            } else {
                                transform = '';
                            }
                            var rotation = textDiv.dataset.angle;
                            if (rotation) {
                                transform = 'rotate(' + rotation + 'deg) ' + transform;
                            }
                            if (transform) {
                                this.CustomStyle.setProp('transform', textDiv, transform);
                            }
                        }
                    }

                    this.textLayerDiv.appendChild(textLayerFrag);
                },

                render: function TextLayerBuilder_render(timeout) {
                    if (!this.divContentDone || this.renderingDone) {
                        return;
                    }

                    if (this.renderTimer) {
                        clearTimeout(this.renderTimer);
                        this.renderTimer = null;
                    }

                    if (!timeout) { // Render right away
                        this.renderLayer();
                    } else { // Schedule
                        var self = this;
                        this.renderTimer = setTimeout(function () {
                            self.renderLayer();
                            self.renderTimer = null;
                        }, timeout);
                    }
                },

                NonWhitespaceRegexp: /\S/,

                appendText: function TextLayerBuilder_appendText(geom, styles) {
                    var style = styles[geom.fontName];
                    var textDiv = document.createElement('div');
                    this.textDivs.push(textDiv);
                    if (!this.NonWhitespaceRegexp.test(geom.str)) {
                        textDiv.dataset.isWhitespace = true;
                        return;
                    }
                    var tx = PDFJS.Util.transform(this.viewport.transform, geom.transform);
                    var angle = Math.atan2(tx[1], tx[0]);
                    if (style.vertical) {
                        angle += Math.PI / 2;
                    }
                    var fontHeight = Math.sqrt((tx[2] * tx[2]) + (tx[3] * tx[3]));
                    var fontAscent = fontHeight;
                    if (style.ascent) {
                        fontAscent = style.ascent * fontAscent;
                    } else if (style.descent) {
                        fontAscent = (1 + style.descent) * fontAscent;
                    }

                    var left;
                    var top;
                    if (angle === 0) {
                        left = tx[4];
                        top = tx[5] - fontAscent;
                    } else {
                        left = tx[4] + (fontAscent * Math.sin(angle));
                        top = tx[5] - (fontAscent * Math.cos(angle));
                    }
                    textDiv.style.left = left + 'px';
                    textDiv.style.top = top + 'px';
                    textDiv.style.fontSize = fontHeight + 'px';
                    textDiv.style.fontFamily = style.fontFamily;

                    textDiv.textContent = geom.str;
                    // |fontName| is only used by the Font Inspector. This test will succeed
                    // when e.g. the Font Inspector is off but the Stepper is on, but it's
                    // not worth the effort to do a more accurate test.
                    if (PDFJS.pdfBug) {
                        textDiv.dataset.fontName = geom.fontName;
                    }
                    // Storing into dataset will convert number into string.
                    if (angle !== 0) {
                        textDiv.dataset.angle = angle * (180 / Math.PI);
                    }
                    // We don't bother scaling single-char text divs, because it has very
                    // little effect on text highlighting. This makes scrolling on docs with
                    // lots of such divs a lot faster.
                    if (geom.str.length > 1) {
                        if (style.vertical) {
                            textDiv.dataset.canvasWidth = geom.height * this.viewport.scale;
                        } else {
                            textDiv.dataset.canvasWidth = geom.width * this.viewport.scale;
                        }
                    }
                },

                setTextContent: function TextLayerBuilder_setTextContent(textContent) {
                    this.textContent = textContent;

                    var textItems = textContent.items;
                    for (var i = 0, len = textItems.length; i < len; i++) {
                        this.appendText(textItems[i], textContent.styles);
                    }
                    this.divContentDone = true;
                },

                CustomStyle: (function CustomStyleClosure() {

                    // As noted on: http://www.zachstronaut.com/posts/2009/02/17/
                    //              animate-css-transforms-firefox-webkit.html
                    // in some versions of IE9 it is critical that ms appear in this list
                    // before Moz
                    var prefixes = ['ms', 'Moz', 'Webkit', 'O'];
                    var _cache = {};

                    function CustomStyle() {
                    }

                    CustomStyle.getProp = function get(propName, element) {
                        // check cache only when no element is given
                        if (arguments.length === 1 && typeof _cache[propName] === 'string') {
                            return _cache[propName];
                        }

                        element = element || document.documentElement;
                        var style = element.style, prefixed, uPropName;

                        // test standard property first
                        if (typeof style[propName] === 'string') {
                            return (_cache[propName] = propName);
                        }

                        // capitalize
                        uPropName = propName.charAt(0).toUpperCase() + propName.slice(1);

                        // test vendor specific properties
                        for (var i = 0, l = prefixes.length; i < l; i++) {
                            prefixed = prefixes[i] + uPropName;
                            if (typeof style[prefixed] === 'string') {
                                return (_cache[propName] = prefixed);
                            }
                        }

                        //if all fails then set to undefined
                        return (_cache[propName] = 'undefined');
                    };

                    CustomStyle.setProp = function set(propName, element, str) {
                        var prop = this.getProp(propName);
                        if (prop !== 'undefined') {
                            element.style[prop] = str;
                        }
                    };

                    return CustomStyle;
                })(),

                reset: function (viewport) {
                    this.viewport = viewport;
                    delete this.textDivs;
                    this.textDivs = [];
                    while (this.textLayerDiv.hasChildNodes()) {
                        this.textLayerDiv.removeChild(this.textLayerDiv.lastChild);
                    }
                    this.textLayerDiv.style.height = this.viewport.height + 'px';
                    this.textLayerDiv.style.width = this.viewport.width + 'px';
                }
            };
            return TextLayerBuilder;
        })();

        return PdfPage;
    })()


});
