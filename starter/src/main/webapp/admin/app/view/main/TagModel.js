Ext.define('admin.view.main.TagModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.tag',
    data: {
        listTitle: '标签管理'
    },
    stores : {
        tags: {type: 'tags'}
    }

});
