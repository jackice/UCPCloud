/**
 * This class is the controller for the main view for the application. It is specified as
 * the "controller" of the Main view class.
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('admin.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',

    logout: function () {
        Ext.Msg.prompt('确认', '是否退出系统?', function (btn, text) {
            if (btn != 'ok')return;
            Ext.util.Cookies.clear('userId');
            Ext.util.Cookies.clear('digest');
        });
        Ext.Msg.show({
            title: '确认',
            message: '是否退出系统?',
            buttons: Ext.Msg.OKCANCEL,
            icon: Ext.Msg.QUESTION,
            fn: function (btn) {
                if (btn != 'ok')return;
                Ext.Ajax.request({url:'/svc/logout',method:'post'}).then(function(){
                    Ext.util.Cookies.clear('userId');
                    Ext.util.Cookies.clear('digest');
                    window.location.href = '/entry/index.html';
                });
            }
        });
    }
});
