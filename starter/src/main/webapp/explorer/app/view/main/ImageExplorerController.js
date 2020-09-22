Ext.define('explorer.view.main.ImageExplorerController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.imageexplorer',

    loadImages: function () {
        var explorer = this.getView();
        var images = this.getStore('images');
        var propertygrid = explorer.down('propertygrid');
        Ext.Ajax.request({
            method: 'GET',
            url: '/svc/' + explorer.record.get('_type') + '/' + explorer.record.get('_id'),
            callback: function (opts, success, response) {
                if (!success)return;
                var obj = Ext.decode(response.responseText);
                var data = [];
                Ext.Array.each(obj._streams, function (stream) {
                    if (!stream.pageCount || stream.pageCount == 0) {
                        data.push(stream)
                    } else {
                        for (var i = 0; i < stream.pageCount; i++) {
                            data.push(Ext.Object.merge({pageIndex: i}, stream))
                        }
                    }
                });
                images.setData(data);


                var source = {};
                Ext.Array.each(Ext.Object.getAllKeys(obj), function (key) {
                    if (Ext.String.startsWith(key, '_'))return;
                    source[key] = obj[key];
                });

                propertygrid.setSource(source, {
                    _createAt: {
                        type: 'date',
                        renderer: function (v) {
                            return Ext.Date.format(new Date(v), 'Y-m-d');
                        }
                    },
                    _createBy: {}
                });

            }
        });
    },

    onSelectionChanged: function (view, selected, eOpts) {
        if (!selected || selected.length === 0)return;
        var explorer = this.getView();
        var thumbnails = explorer.getComponent('thumbnails');

        Ext.Array.each(thumbnails.getNodes(), function (node) {
            node.style.border = '3px solid #fff';
        });

        var big = explorer.getComponent('big');
        var image = big.down('image');
        image.setSrc('/svc/' + explorer.record.get('_type') + '/' + explorer.record.get('_id') + '/_streams/' + selected[0].get('streamId') + '?accept=' + selected[0].get('contentType') + '&pageIndex=' + selected[0].get('pageIndex'))
        var node = thumbnails.getNode(selected[0]);
        //node.style['background-color'] = '#2a3f5d';
        node.style.border = '3px solid #2a3f5d';
    },

    onRefresh: function () {
        var thumbnails = this.getView().getComponent('thumbnails');
        thumbnails.isFocusable(false);
        thumbnails.getSelectionModel().select(0);
        this.restore();
    },

    zoomin: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setWidth(image.getWidth() * 1.1);
        image.setHeight(image.getHeight() * 1.1);
    },

    zoomout: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setWidth(image.getWidth() * 0.9);
        image.setHeight(image.getHeight() * 0.9);
    },

    rotateleft: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setStyle({
            '-ms-transform': 'rotate(-90deg)', /* IE 9 */
            '-moz-transform': 'rotate(-90deg)', /* Firefox */
            '-webkit-transform': 'rotate(-90deg)', /* Safari and Chrome */
            '-o-transform': 'rotate(-90deg)' /* Opera */
        });
    },

    rotateright: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setStyle({
            '-ms-transform': 'rotate(90deg)', /* IE 9 */
            '-moz-transform': 'rotate(90deg)', /* Firefox */
            '-webkit-transform': 'rotate(90deg)', /* Safari and Chrome */
            '-o-transform': 'rotate(90deg)' /* Opera */
        });

    },

    fitwidth: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setStyle({
            'width': (big.getWidth() - 18) + 'px',
            height: 'auto'
        });
    },

    fitheight: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setStyle({
            'width': 'auto',
            height: (big.getHeight() - 60) + 'px'
        });
    },

    restore: function () {
        var me = this.getView();
        var big = me.getComponent('big');
        var image = big.down('image');
        image.setStyle({
            'width': 'auto',
            height: 'auto',
            '-ms-transform': '',
            '-moz-transform': '',
            '-webkit-transform': '',
            '-o-transform': ''
        });

    },

    first: function () {
        var me = this.getView();
        var thumbnails = me.getComponent('thumbnails');
        thumbnails.getSelectionModel().select(0);
        var node = thumbnails.getNode(thumbnails.getSelection()[0]);
        node.scrollIntoView();

    },

    backward: function () {
        var me = this.getView();
        var thumbnails = me.getComponent('thumbnails');
        var selection = thumbnails.getSelection();
        if (selection || selection > 0) {
            var index = thumbnails.getStore().indexOf(selection[0]);
            thumbnails.getSelectionModel().select(index - 1);
            var node = thumbnails.getNode(thumbnails.getSelection()[0]);
            node.scrollIntoView();
        }
    },

    forward: function () {
        var me = this.getView();
        var thumbnails = me.getComponent('thumbnails');
        var selection = thumbnails.getSelection();
        if (selection || selection > 0) {
            var index = thumbnails.getStore().indexOf(selection[0]);
            thumbnails.getSelectionModel().select(index + 1);
            var node = thumbnails.getNode(thumbnails.getSelection()[0]);
            node.scrollIntoView();
        }

    },

    last: function () {
        var me = this.getView();
        var thumbnails = me.getComponent('thumbnails');
        var count = thumbnails.getStore().getCount();
        thumbnails.getSelectionModel().select(count - 1);
        var node = thumbnails.getNode(thumbnails.getSelection()[0]);
        node.scrollIntoView({block: "end", behavior: "smooth"});
    },
    loadProperty : function(){
        var explorer = this.getView();
        var propertygrid = explorer.down('propertygrid');
        Ext.Ajax.request({
            method: 'GET',
            url: '/svc/' + explorer.record.get('_type') + '/' + explorer.record.get('_id'),
            callback: function (opts, success, response) {
                if (!success)return;
                var obj = Ext.decode(response.responseText);


                var source = {};
                Ext.Array.each(Ext.Object.getAllKeys(obj), function (key) {
                    if (Ext.String.startsWith(key, '_'))return;
                    source[key] = obj[key];
                });

                propertygrid.setSource(source, {
                    _createAt: {
                        type: 'date',
                        renderer: function (v) {
                            return Ext.Date.format(new Date(v), 'Y-m-d');
                        }
                    },
                    _createBy: {}
                });

            }
        });
    }

});
