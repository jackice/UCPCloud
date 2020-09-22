Ext.define('entry.view.InitController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.init',

    do: function (btn) {
        btn.setDisabled(true);
        var me = this;
        var viewport = me.getView();
        var shards = me.getViewModel().get("shards");
        var replicas = me.getViewModel().get("replicas");
        Ext.Ajax.request({
            method: 'POST',
            url: '/initialization/initial',
            success: function (response, options) {
                window.location.reload();
            }, failure: function (response, options) {
                Ext.toast({
                    html: '<span style="color: red;">初始化失败</span>',
                    width: 200,
                    align: 't'
                });
            }
        })
    }

});
