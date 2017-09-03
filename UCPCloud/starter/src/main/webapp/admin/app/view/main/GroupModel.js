Ext.define('admin.view.main.GroupModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.group',
    stores: {
        users: {type: 'users',pageSize : 1},
        groups: {type: 'groups'}
    },
    data: {
        listTitle: '组管理',
        createTitle : '创建组',
        modifyTitle : '修改组'
    }

});
