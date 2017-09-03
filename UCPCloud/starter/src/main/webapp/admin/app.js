Ext.application({
    name: 'admin',

    extend: 'admin.Application',

    views: [
        'admin.view.main.Main'
    ],

    launch: function () {
        Ext.USE_NATIVE_JSON=true;
        if (!(Ext.util.Cookies.get('userId') && Ext.util.Cookies.get('digest'))) {
            window.location.href = '/entry/index.html?url=' + encodeURIComponent(window.location.href);
        } else {
            Ext.Ajax.setDefaultHeaders({Authorization: Ext.util.Cookies.get('digest')})
            Ext.Ajax.addListener('requestexception', function (conn, response, options, eOpts) {
                if (response.status === 401) {
                    Ext.util.Cookies.clear('userId');
                    Ext.util.Cookies.clear('digest');
                    window.location.href = '/entry/index.html?url=' + encodeURIComponent(window.location.href);
                }
            });
            this.setMainView('admin.view.main.Main');
        }

    }

});
