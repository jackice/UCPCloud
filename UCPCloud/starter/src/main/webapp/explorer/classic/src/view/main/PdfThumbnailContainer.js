 Ext.define('explorer.view.main.PdfThumbnailContainer', {
    extend: 'Ext.view.View',
    xtype: ['pdfthumbnailcontainer'],
    frame: true,
    margin: '0 0 0 0',
	padding: '5',
    scrollable: 'vertical',
    itemSelector: 'div',
    focusCls: 'noclass',
    renderStates: [],
    style: {
        'background-color': '#f6f6f6',
		'border-right': '2px solid #5fa2dd',
    },
    scrollable: {
        x: false,
        listeners: {
            scroll: function (scroller, x, y, eOpts) {
                var me = scroller.component;
                var pages = me.getEl().dom.children;
                var viewCanvas = Ext.Array.findBy(pages, function (canvas) {
                    return canvas.offsetTop > y
                });
                var pageNum = Ext.Array.indexOf(pages, viewCanvas) + 1;

                for (var i = -3; i < 5; i++) {
                    if (Ext.Array.contains(me.renderStates, pageNum + i))continue;
                    me.renderStates.push(pageNum + i);
                    var targetCanvas = pages[pageNum + i - 1];
                    if (!targetCanvas)continue;
                    if (me.drawThumbnailPromise)
                        me.drawThumbnailPromise = me.drawThumbnailPromise.then(me.drawThumbnail(targetCanvas, pageNum + i));
                    else
                        me.drawThumbnailPromise = Q(me.drawThumbnail(targetCanvas, pageNum + i))
                }
            }
        }
    },

    tpl: [
        '<tpl for=".">',
        '<div style="margin: 10px 10px 10px 20px;-webkit-box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);-moz-box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);overflow:hidden;border:solid #fff 5px; background-color:#fff;background-image:url(/pdfjs-1.1.366-dist/web/images/loading-icon.gif);background-repeat: no-repeat; background-position:center;">',
        '</div></tpl>',
    ],

    listeners: {
        itemadd: function (records, index, node, eOpts) {
            var me = this;
            var canvas = node[0];
            var pageNum = index + 1;

            if (me.drawThumbnailBorderPromise)
                me.drawThumbnailBorderPromise = me.drawThumbnailBorderPromise.then(me.initThumbnail(canvas, pageNum));
            else
                me.drawThumbnailBorderPromise = Q(me.initThumbnail(canvas, pageNum))


            if (index > 10)return;
            me.renderStates.push(pageNum);
            if (me.drawThumbnailPromise)
                me.drawThumbnailPromise = me.drawThumbnailPromise.then(me.drawThumbnail(canvas, pageNum));
            else
                me.drawThumbnailPromise = Q(me.drawThumbnail(canvas, pageNum))

        }

        //itemclick: function (view, record, item, index, e, eOpts) {
        //    Ext.each(view.getEl().dom.children, function (child) {
        //        child.style.borderWidth = '1px';
        //        child.style.outlineColor = "";
        //        child.style.outlineWidth = "";
        //        child.style.outlineStyle = "";
        //        child.style.outlineOffset = "";
        //    });
        //    item.style.outlineColor = 'rgb(95,162,211)';
        //    item.style.outlineWidth = '2px';
        //    item.style.outlineStyle = 'solid';
        //    item.style.outlineOffset = '-2px';
        //}
    },

    scrollIntoView: function (pageNum) {
        var me = this;
        var box = me.getEl().dom.children[pageNum - 1];
        if (!box)return;
        if (pageNum > me.pdfDoc.pdfInfo.numPages)return;
        box.scrollIntoView();
    },

    initThumbnail: function (canvas, pageNum) {
        var me = this;
        var deferred = Q.defer();
        me.pdfDoc.getPage(pageNum).then(function (page) {
            var wScale = 150.0 / page.view[2];
            var hScale = 150.0 / page.view[3];
            var viewport = page.getViewport(wScale > hScale ? hScale : wScale);
            canvas.height = viewport.height;
            canvas.width = viewport.width;
        }).catch(function (e) {
            deferred.reject(e);
        });
        return deferred.promise;
    },

    drawThumbnail: function (div, pageNum) {
        var me = this;
        var deferred = Q.defer();
        var canvas = document.createElement('canvas');
        var ctx = canvas.getContext('2d');
        me.pdfDoc.getPage(pageNum).then(function (page) {
            var wScale = 150.0 / page.view[2];
            var hScale = 150.0 / page.view[3];
            var viewport = page.getViewport(wScale > hScale ? hScale : wScale);
            canvas.height = viewport.height - 2;
            canvas.width = viewport.width - 2;
            div.style.height = viewport.height + 'px';
            div.style.width = viewport.width + 'px';
            var renderContext = {
                canvasContext: ctx,
                viewport: viewport
            };
            var renderTask = page.render(renderContext);
            renderTask.promise.then(function () {
                div.appendChild(canvas);
                deferred.resolve()
            }).catch(function (e) {
                deferred.reject(e);
            });
        }).catch(function (e) {
            deferred.reject(e);
        });
        return deferred.promise;
    }
});