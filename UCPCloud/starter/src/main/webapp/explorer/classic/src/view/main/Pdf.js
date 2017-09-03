Ext.define('explorer.view.main.Pdf', {
    extend: 'Ext.panel.Panel',
    xtype: ['pdf'],
    controller: 'imageexplorer',
    layout: 'border',
    renderStates: [],
    renderBigStates: [],
    pages: [],
    listeners: {
        afterrender: function () {
            var me = this;
            me.mask.show();
            //var url = '../pdfjs-1.1.366-dist/web/compressed.tracemonkey-pldi-09.pdf';
            var streamId = me.record.get('_streams')[0].streamId;
            var url = '/svc/' + me.record.get('_type').name + '/' + me.record.get('_id') + '/_streams/' + streamId + '?accept=application%2Fpdf';


            PDFJS.imageResourcesPath = '../pdfjs-1.1.366-dist/web/images/';
            PDFJS.workerSrc = '../pdfjs-1.1.366-dist/build/pdf.worker.js';
            PDFJS.cMapUrl = '../web/cmaps/';
            PDFJS.cMapPacked = true;
            me.pdfDoc = null;
            me.pageRendering = false;
            me.pageNumPending = null;
            me.scale = 1.0;

            var params = {
                url: url,
                httpHeaders: {Authorization: Ext.util.Cookies.get('digest')}
            };

            PDFJS.getDocument(params).then(function (pdfDoc) {
                me.pdfDoc = pdfDoc;
                var imageStore = Ext.create('Ext.data.Store', {
                    fields: ['viewport']
                });
                me.down('displayfield[itemId=totalPage]').setValue('/' + me.pdfDoc.pdfInfo.numPages);
                for (var i = 1; i <= me.pdfDoc.numPages; i++) {
                    me.pdfDoc.getPage(i).then(function (page) {
                        imageStore.add({viewport: page.getViewport(1.0)});
                    });
                }
                var thumbnails = me.down('*[itemId=thumbnails]');
                thumbnails.pdfDoc = pdfDoc;
                thumbnails.bindStore(imageStore);
                var pages = me.down('*[itemId=pages]');
                pages.pdfDoc = pdfDoc;
                pages.bindStore(imageStore);
                me.mask.hide();
            });
            var explorer = this;
            var propertygrid = explorer.down('propertygrid');
            Ext.Ajax.request({
                method: 'GET',
                url: '/svc/' + explorer.record.get('_type').name + '/' + explorer.record.get('_id') + '?containsType=true',
                callback: function (opts, success, response) {
                    if (!success)return;
                    var obj = Ext.decode(response.responseText);
                    var source = {};
                    Ext.Array.each(Ext.Object.getAllKeys(obj), function (key) {
                        if (Ext.String.startsWith(key, '_'))return;
                        if (Ext.isArray(obj[key])) {
                            source[key] = obj[key].join(",");
                            return;
                        }
                        source[key] = obj[key];
                    });

                    var orderKeys = Ext.Array.sort(obj._type.properties ? obj._type.properties : [], function (a, b) {
                        if (a.order > b.order) return 1;
                        if (a.order < b.order) return -1;
                        if (a.order === b.order) return 0;
                    });


                    var sourceConfig = {};

                    var sortedSource = {'名称': obj.name, '标签': obj.tag, '类型': obj._type.displayName};
                    sortedSource['创建时间'] = obj['createdOn'];
                    sortedSource['创建人'] = obj['createdBy'];
                    sortedSource['修改时间'] = obj['lastUpdatedOn'];
                    sortedSource['修改人'] = obj['lastUpdatedBy'];
                    Ext.Array.each(orderKeys, function (item) {
                        sortedSource[item.name] = obj[item.name];
                    });


                    propertygrid.setSource(sortedSource, {
                        createdOn: {
                            type: 'date',
                            editor: {xtype: 'label'},
                            renderer: function (v) {
                                return Ext.Date.format(new Date(v), 'Y-m-d');
                            }
                        },
                        lastUpdatedOn: {
                            type: 'date',
                            renderer: function (v) {
                                return Ext.Date.format(new Date(v), 'Y-m-d');
                            }
                        }
                    });
                    var view = propertygrid.getView();
                    var tip = Ext.create('Ext.tip.ToolTip', {
                        // The overall target element.
                        target: view.el,
                        // Each grid row causes its own separate show and hide.
                        delegate: view.itemSelector,
                        // Moving within the row should not hide the tip.
                        trackMouse: true,
                        // Render immediately so that tip.body can be referenced prior to the first show.
                        //renderTo: Ext.getBody(),
                        listeners: {
                            // Change content dynamically depending on which element triggered the show.
                            beforeshow: function updateTipBody(tip) {
                                var val = tip.triggerElement.querySelector('td[data-columnid=value]').querySelector('div').innerHTML;
                                if (val != '' && val != '&nbsp;') {
                                    tip.update(val);
                                } else {
                                    return false;
                                }
                            }
                        }
                    });

                }
            });
        },
        destroy: function (me, eOpts) {
            //销毁打印事件
            if (window.matchMedia) {
                var mediaQueryList = window.matchMedia('print');
                mediaQueryList.removeListener(me.matchMediaPrint)
            } else {
                window.onbeforeprint = undefined;
                window.onafterprint = undefined
            }
        }
    },

    initComponent: function () {
        var me = this;
        me.mask = new Ext.LoadMask({
            msg: 'Please wait...',
            target: me
        });

        Ext.apply(me, {
            items: [{
                xtype: 'pdfthumbnailcontainer',
                region: 'west',
                itemId: 'thumbnails',
                width: 210,
                minWidth: 100,
                maxWidth: 250,
                listeners: {
                    selectionchange: function (view, selected, eOpts) {
                        if (selected.length === 0)return;
                        var me = this.up('pdf');
                        var item = selected[0];
                        var PAGE_TO_VIEW = this.indexOf(item) + 1;
                        var pdfpagecontainer = me.down('pdfpagecontainer');
                        pdfpagecontainer.scrollIntoView(PAGE_TO_VIEW);
                    }
                }
            }, {

                region: 'center',
                layout: 'fit',
                scrollable: true,
                itemId: 'big',
                tbar: [
                    //{
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-undo',
                    //    handler: 'rotateleft'
                    //}, {
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-ban',
                    //    handler: 'restore'
                    //}, {
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-repeat',
                    //    handler: 'rotateright'
                    //},

                    {
                        xtype: 'segmentedbutton',
                        items: [{
                            iconCls: 'fa fa-arrows-v',
                            value: 'height',
                            handler: function () {
                                var me = this.up('pdf');
                                var pdfpagecontainer = me.down('pdfpagecontainer');
                                Ext.each(pdfpagecontainer.pages, function (pdfPageView) {
                                    var scale = 1.0 * pdfpagecontainer.getHeight() / pdfPageView.page.view[3];
                                    pdfPageView.reset(scale);
                                });
                                pdfpagecontainer.drawContextPages();
                            }
                        }, {
                            iconCls: 'fa fa-arrows-h',
                            value: 'width',
                            pressed: true,
                            handler: function () {
                                var me = this.up('pdf');
                                var pdfpagecontainer = me.down('pdfpagecontainer');
                                Ext.each(pdfpagecontainer.pages, function (pdfPageView) {
                                    var scale = 1.0 * pdfpagecontainer.getWidth() / pdfPageView.page.view[2];
                                    pdfPageView.reset(scale);
                                });
                                pdfpagecontainer.drawContextPages();
                            }
                        }]
                    },
                    //{
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-fast-backward',
                    //    handler: 'first'
                    //},
                    {
                        xtype: 'textfield',
                        itemId: 'page',
                        width: 50,
                        fieldStyle: 'font-size:large;text-align:center;vertical-align:middle;',
                        value: '1',
                        enableKeyEvents: true,
                        listeners: {
                            keydown: function (textfield, e, eOpts) {
                                if (e.getKey() !== Ext.event.Event.ENTER) return;
                                var me = this.up('pdf');
                                var pageNum = parseInt(textfield.getValue());
                                var box = me.down('dataview').getEl().dom;
                                if (pageNum > me.pdfDoc.pdfInfo.numPages)return;
                                var child = box.children[pageNum - 1];
                                child.scrollIntoView();
                                child.click();
                            }
                        }
                    }, {
                        xtype: 'displayfield',
                        itemId: 'totalPage',
                        fieldStyle: 'font-size:large;text-align:center;vertical-align:middle;line-height:25px;',
                        value: '/'
                    },
                    //{
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-backward',
                    //    handler: 'backward'
                    //}, {
                    //    xtype: 'button',
                    //
                    //    iconCls: 'fa fa-forward',
                    //    handler: 'forward'
                    //}, {
                    //    xtype: 'button',
                    //    iconCls: 'fa fa-fast-forward',
                    //    handler: 'last'
                    //},
                    {
                        xtype: 'button',
                        iconCls: 'fa fa-print',
                        handler: function () {

                            var me = this.up('pdf');
                            me.mask.show();
                            var thumbs = me.down('dataview').getStore();
                            var printContainer = document.createElement("div");
                            printContainer.id = 'printContainer';
                            var html = document.getElementsByTagName('html')[0];
                            var pageStyleSheet = document.createElement('style');
                            me.pdfDoc.getPage(1).then(function (page) {
                                pageStyleSheet.id = 'pageStyleSheet';
                                var pageSize = page.getViewport(1);
                                pageStyleSheet.textContent =
                                    '@supports ((size:A4) and (size:1pt 1pt)) {' +
                                    '@page { size: ' + pageSize.width + 'pt ' + pageSize.height + 'pt;margin:0;padding:0}' +
                                    '#printContainer {height:100%;}' +
                                    '#printContainer > div {width:100% !important;height:100% !important;}' +
                                    '}';
                            });


                            var task = Ext.TaskManager.newTask({
                                run: function () {
                                    if (printContainer.children[thumbs.getCount() - 1].hasChildNodes()) {
                                        Ext.TaskManager.stop(task);
                                        window.__body = html.removeChild(document.body);
                                        var printBody = document.createElement("body");
                                        printBody.style.height = '100%';
                                        html.appendChild(printBody);
                                        printBody.appendChild(printContainer);
                                        printBody.appendChild(pageStyleSheet);

                                        if (Ext.isIE || /edge/.test(navigator.userAgent.toLowerCase()))
                                            setTimeout("window.print()", 1000);
                                        else
                                            window.print();

                                    }
                                },
                                interval: 1000
                            });
                            Ext.TaskManager.start(task);

                            thumbs.each(function (item, index) {
                                var div = document.createElement("div");
                                div.style.margin = '0';
                                div.style.padding = '0';
                                printContainer.appendChild(div);
                                me.pdfDoc.getPage(index + 1).then(function (page) {
                                    var canvas = document.createElement("canvas");
                                    canvas.style.margin = '0';
                                    canvas.style.padding = '0';
                                    div.appendChild(canvas);
                                    var ctx = canvas.getContext('2d');
                                    var viewport = page.getViewport(1);
                                    div.style.height = viewport.height + 'pt';
                                    div.style.width = viewport.width + 'pt';
                                    var PRINT_OUTPUT_SCALE = 2;

                                    canvas.width = Math.floor(viewport.width) * PRINT_OUTPUT_SCALE;
                                    canvas.height = Math.floor(viewport.height) * PRINT_OUTPUT_SCALE;
                                    canvas.style.width = (PRINT_OUTPUT_SCALE * 100) + '%';
                                    canvas.style.height = (PRINT_OUTPUT_SCALE * 100) + '%';
                                    var cssScale = 'scale(' + (1 / PRINT_OUTPUT_SCALE) + ', ' + (1 / PRINT_OUTPUT_SCALE) + ')';
                                    canvas.style.transform = cssScale;
                                    canvas.style.transformOrigin = '0% 0%';

                                    var renderContext = {
                                        canvasContext: ctx,
                                        viewport: viewport,
                                        intent: 'print'
                                    };

                                    ctx.save();
                                    ctx.fillStyle = 'rgb(255, 255, 255)';
                                    ctx.fillRect(0, 0, canvas.width, canvas.height);
                                    ctx.restore();
                                    ctx._transformMatrix = [PRINT_OUTPUT_SCALE, 0, 0, PRINT_OUTPUT_SCALE, 0, 0];
                                    ctx.scale(PRINT_OUTPUT_SCALE, PRINT_OUTPUT_SCALE);

                                    var pageSize = page.getViewport(1);
                                    var renderTask = page.render(renderContext);
                                    renderTask.promise.then(function () {
                                        if (Ext.isIE || /edge/.test(navigator.userAgent.toLowerCase())) {
                                            var image = new Image();
                                            image.src = canvas.toDataURL("image/png");
                                            image.style.width = (pageSize.width - 1) + 'pt';
                                            image.style.height = (pageSize.height - 1) + 'pt';
                                            image.style.margin = '0';
                                            image.style.padding = '0';
                                            div.removeChild(canvas);
                                            div.appendChild(image);
                                        }
                                    });
                                });
                            });

                        }
                    }
                ],
                items: [{
                    xtype: 'pdfpagecontainer',
                    itemId: 'pages',
                    listeners: {
                        pageChanged: function (pageNum) {
                            var me = this.up('pdf');
                            me.down('textfield[itemId=page]').setValue(pageNum);
                        }
                    }
                }]
            }, {
                region: 'east',
                width: 210,
                minWidth: 210,
                maxWidth: 210,
                header: false,
                frame: true,
                clicksToEdit: 100,
                sortableColumns: false,
                xtype: 'propertygrid'
            }
            ]
        });

        //订阅打印事件
        me.matchMediaPrint = function (mql) {
            if (mql.matches) {
                me.beforePrint();
            } else {
                me.afterPrint();
            }
        };
        if (Ext.isIE || /edge/.test(navigator.userAgent.toLowerCase())) {
            window.onbeforeprint = function () {
                me.beforePrint();
            }
            window.onafterprint = function () {
                me.afterPrint();
            }
        } else if (window.matchMedia) {
            var mediaQueryList = window.matchMedia('print');
            mediaQueryList.addListener(me.matchMediaPrint, me);
        }
        this.callParent();

    },

    beforePrint: function () {
        this.mask.hide();
        console.log('Functionality to run before printing.');
    },

    afterPrint: function () {
        var html = document.getElementsByTagName('html')[0];
        html.removeChild(document.body)
        html.appendChild(window.__body)
    }

});
