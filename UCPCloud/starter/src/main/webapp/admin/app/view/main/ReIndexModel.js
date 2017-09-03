Ext.define('admin.view.main.ReIndexModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.reIndex',
    data: {
        listTitle: '重建索引',
        operationId : '',
        reindexType : '',
        srcIndex : '',
        targetIndex : ''
    },
    stores : {
        reIndexs: {type: 'reIndexs'},
        types: {type: 'types'}
    }

});
