Ext.define('admin.view.main.ConfigModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.config',
    stores: {
        users: {type: 'users', pageSize: 1},
        groups: {type: 'groups'}
    },
    data: {
        title: '配置',
        analyzerText: '赞华（中国）电子系统有限公司',
        analyzer: 'standard'
    }

});
