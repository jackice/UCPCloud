Ext.define('entry.view.LoginController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.login',

    login: function () {
        var me = this;
        var form = me.getView().down('form').getForm();
        if (!form.isValid())return;
        var combo = me.getView().down('form').down('combo');
        var userId = me.getViewModel().get("userId");
        var password = me.getViewModel().get("password");
        var digest = 'Basic ' + Ext.util.Base64.encode(userId + ':' + password)
        Ext.Ajax.request({
            method: 'GET',
            headers: {Authorization: digest},
            url: '/svc/users/' + userId+'?getAll=true',
            success: function (response, options) {
                var result = Ext.JSON.decode(response.responseText);
                var groups = result.groups.join( ',' );
                Ext.util.Cookies.set('userId', userId);
                Ext.util.Cookies.set('userName', result.userName);
                Ext.util.Cookies.set('groups', groups);
                Ext.util.Cookies.set('digest', digest);
                var params = Ext.Object.fromQueryString(window.location.search);
                var url = params.url ? params.url : combo.getValue();
                if(url.indexOf("/admin/")>-1&&groups.indexOf("adminGroup")==-1){
                    Ext.Msg.alert('提示信息', '您没有登录系统管理的权限！');
                    return false;
                }
                window.location.href = url;
            }, failure: function (response, options) {
                var errorMsg = response.responseText;
                if (errorMsg.indexOf("Bad credentials")>-1) {
                    errorMsg = '密码错误';
                }else if (errorMsg.indexOf("不存在")>0) {
                    errorMsg = "用户:'"+userId+"'不存在";
                }else{
                    errorMsg = '登录失败';
                }
                Ext.toast({
                    //html: '<span style="color: red;">登录失败</span>',
                    html: '<span style="color: red;">'+errorMsg+'</span>',
                    width: 200,
                    align: 't'
                });
            }
        })
    }

});
