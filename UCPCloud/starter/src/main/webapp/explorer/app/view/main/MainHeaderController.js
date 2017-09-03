Ext.define('explorer.view.main.MainHeaderController', {
    extend: 'explorer.view.main.DocumentController',

    alias: 'controller.mainheader',
    fullText: function (bt, e) {
        var me = this;
        var keytext = bt.up('toolbar').down('textfield').getValue().trim();
        var tabPanel = bt.up('app-header').up('app-main').down('tabpanel');
        var pageSize = this.getViewModel().get('pageSize');
        if (!keytext || keytext === '') return;

        var index = tabPanel.items.length;
        var fulltext = tabPanel.add({
            title: me.getViewModel().get('fullTextTitle'),
            closable: true,
            xtype: 'fulltext',
            docQuery: keytext,
            limit: pageSize,
            index: index
        });
        tabPanel.setActiveTab(index);
        fulltext.down('label[itemId=searchText]').setText(keytext);
        var fulltextStore = Ext.create('Ext.data.Store', {
            model: 'chemistry.model.Document',
            pageSize: pageSize,
            autoLoad: true,
            proxy: {
                extraParams: {query: encodeURIComponent(keytext), highlight: true, allowableActions: true},
                type: 'ajax',
                url: '/svc',
                reader: {
                    type: 'json',
                    rootProperty: 'documents'
                }
            }
        });
        fulltext.bindStore(fulltextStore);
    },

    pageSizeChanged: function (combo, record, eOpts ) {
        var fulltextStore = combo.up('fulltext').getStore();
        fulltextStore.setPageSize(combo.getValue());
        fulltextStore.load();
    },

    advQuery: function (bt, e) {
        Ext.create('Ext.window.Window', {
            title: '高级查询',
            layout: 'center',
            width: '75%',
            height: '95%',
            items: [{
                xtype: 'advancedsearch',
                width: '100%',
                height: '100%',
                bodyPadding: '10 20'
            }]
        }).show();
    },
    indexDoc: function (bt, e) {
        Ext.create('Ext.window.Window', {
            title: '新建文档',
            layout: 'center',
            width: '75%',
            height: '95%',
            modal: true,
            items: [{
                xtype: 'indexdocument',
                width: '100%',
                height: '100%',
                bodyPadding: '10'
            }]
        }).show();
    },
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
