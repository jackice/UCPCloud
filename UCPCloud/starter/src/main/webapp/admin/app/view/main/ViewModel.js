Ext.define('admin.view.main.ViewModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.view',
    data: {
        listTitle: '视图管理'
    },
    stores : {
        views: {type: 'views'},
        types: {type: 'types'}
    }

});
