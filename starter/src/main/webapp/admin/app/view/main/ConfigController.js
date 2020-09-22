Ext.define('admin.view.main.ConfigController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.config',

    analyze: function () {
        var me = this;
        Ext.Ajax.request({
            method: 'GET',
            url: '/analyze',
            params: {
                text: me.getViewModel().get('analyzerText'),
                analyzer: me.getViewModel().get('analyzer')
            },
            success: function (response, opts) {
                var words = Ext.JSON.decode(response.responseText)
                Ext.Msg.alert('分词结果', words.join(','));
            },
            failure: function (response, opts) {
                Ext.Msg.show({
                    title: response.status,
                    message: response.responseText,
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.ERROR
                });
            }
        });
    }


});
