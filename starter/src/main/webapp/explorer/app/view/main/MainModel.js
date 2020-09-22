Ext.define('explorer.view.main.MainModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.main',

    data: {
        headerTitle : '文档管理系统',
        name: 'explorer',
        queryCondition : '',
        qType : [],
        fullTextTitle : '全文搜索',
        advQueryTitle : '高级查询',
        pageSize: 25

    },

    stores: {
        //views: {type: 'views'},
        fulltext: {
            type: 'documents'
        },
        documents: {type: 'documents'},
        types: {type: 'types'}
    }
});
