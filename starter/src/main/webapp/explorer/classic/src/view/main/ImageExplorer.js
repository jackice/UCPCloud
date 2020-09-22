Ext.define('explorer.view.main.ImageExplorer', {
    extend: 'Ext.panel.Panel',
    xtype: 'imageexplorer',
    layout: 'border',
	
    controller: 'imageexplorer',
    viewModel: 'imageexplorer',


    defaults: {
        //collapsible: false,
        //split: true
    },

    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [{
                xtype: 'dataview',
                region: 'west',
                frame: true,
                margin: '5 0 0 0',
                width: 210,
                minWidth: 100,
                maxWidth: 250,
                scrollable: 'vertical',
                itemSelector: 'li',
                style: {
                    'background-color': '#ffffff'
                },
                bind: {
                    store: '{images}'
                },
                itemId: 'thumbnails',
                tpl: [
                    '<ol>',
                    '<tpl for=".">',
                    '<li>',
                    '<img style="width:150px" src="/svc/' + me.record.get('_type') + '/' + me.record.get('_id') + '/_streams/{streamId}?accept={contentType}&pageIndex={pageIndex}" />',
                    '</li>',
                    '</tpl>',
                    '</ol>'
                ],
                listeners: {
                    afterrender: 'loadImages',
                    selectionchange: 'onSelectionChanged',
                    refresh: 'onRefresh'
                }
            }, {
                region: 'center',
                layout: 'fit',
                scrollable: true,
                itemId: 'big',
                tbar: [
                    {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-search-plus',
                        handler: 'zoomout'
                    },
                    {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-search-minus',
                        handler: 'zoomin'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-undo',
                        handler: 'rotateleft'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-repeat',
                        handler: 'rotateright'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-arrows-v',
                        handler: 'fitheight'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-arrows-h',
                        handler: 'fitwidth'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-ban',
                        handler: 'restore'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-fast-backward',
                        handler: 'first'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-backward',
                        handler: 'backward'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-forward',
                        handler: 'forward'
                    }, {
                        xypte: 'button',
                        scale: 'large',
                        iconCls: 'fa fa-fast-forward',
                        handler: 'last'
                    }
                ],
                items: [
                    {
                        xtype: 'image',
                        scrollable: true,
                        split: true,
                        resizable: false
                    }
                ]
            }, {
                region: 'east',
                width: 210,
                minWidth: 210,
                maxWidth: 210,
                header: false,
                frame: true,
                xtype: 'propertygrid'
                //bbar: [
                //    {
                //        xtype: 'button',
                //        text: '保存'
                //        //handler: me.saveMetadata
                //    }
                //]
            }
            ]
        });

        this.callParent();
    }


});