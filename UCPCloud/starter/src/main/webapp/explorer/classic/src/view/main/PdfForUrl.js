Ext.define('explorer.view.main.PdfForUrl', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-pdf',
    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',
        'explorer.view.main.Pdf'
    ],
    layout: 'fit',
    title: '在线浏览',
    listeners: {
        afterrender: function (e) {
            var me = e;
            var params = Ext.Object.fromQueryString(window.location.search);
            Ext.Ajax.request({
                url: '/svc/' + params.id + '/_id',
                callback: function (options, success, response) {
                    if (!success) {
                        var error =  Ext.decode(response.responseText);
                        Ext.Msg.alert('提示信息', error.reason);
                        return;
                    }
                    if(response.responseText&&response.responseText!=''){
                        var data = Ext.decode(response.responseText);
                        if (data._streams && data._streams.length > 0) {
                            var _streams = data._streams;
                            var sContentType = _streams[0].contentType;
                            if (Ext.Array.contains(['application/pdf', 'image/tiff', 'image/jpeg', 'image/png'], sContentType)) {
                                var record = Ext.create('Ext.data.Model', {
                                    fields: [
                                        {name: '_id', type: 'string'},
                                        {name: '_type', type: 'string'},
                                        {name: '_streams', type: 'auto'}
                                    ]
                                });
                                record.set({'_id': data._id, '_type': data._type, '_streams': data._streams});
                                me.add({
                                    xtype: 'pdf',
                                    record: record
                                });

                            } else {
                                Ext.Msg.alert('提示信息', '不支持该格式的文档浏览');
                            }
                        } else {
                            Ext.Msg.alert('提示信息', '无附件可以浏览');
                        }
                    }


                }
            });
        }
    }
});
