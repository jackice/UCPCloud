Ext.application({
    name: 'explorer',

    extend: 'explorer.Application',

    views: [
        'explorer.view.main.Main',
        'explorer.view.main.PdfForUrl'
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
            this.routeModule();
        }

    },

    routeModule: function () {
        if ( window.location.href.indexOf("onlineView.html") > -1) {
            this.setMainView('explorer.view.main.PdfForUrl');
        } else {
            this.setMainView('explorer.view.main.Main');
        }
    }


});
