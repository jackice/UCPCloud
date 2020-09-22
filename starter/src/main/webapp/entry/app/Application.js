Ext.define('entry.Application', {
    extend: 'Ext.app.Application',

    name: 'entry',

    views: [
        'entry.view.login.LoginFrame',
        'entry.view.init.InitFrame'
    ],
    stores: [],

    launch: function () {
        var me = this;
        Ext.Ajax.clearListeners();
        Ext.Ajax.request({
            url: '/initialization/status',
            success: function (response, opts) {
                me.onSuccess(response.responseText);
            }, failure: function (response, opts) {
                Ext.Msg.show({
                    title: '错误',
                    message: '网络通信失败!',
                    buttons: Ext.Msg.OK,
                    icon: Ext.Msg.ERROR
                });
            }
        });
    },

    onSuccess: function (status) {
        var me = this;
        if ('false' === status) {
            me.setMainView('entry.view.init.InitFrame');
        } else {
            me.setMainView('entry.view.login.LoginFrame');
        }
    },

    onAppUpdate: function () {
        Ext.Msg.confirm('Application Update', 'This application has an update, reload?',
            function (choice) {
                if (choice === 'yes') {
                    window.location.reload();
                }
            }
        );
    }
});
