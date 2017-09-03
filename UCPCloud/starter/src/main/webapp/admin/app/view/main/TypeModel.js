Ext.define('admin.view.main.TypeModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.type',
    stores: {
        types: {type: 'types'},
        properties : {type : 'properties'}
    },
    data: {
        properties : [],
        listTitle: '类型管理',
        createTitle : '创建类型',
        modifyTitle : '修改类型',
        propertyTitle : '业务属性列表'
    }

});
