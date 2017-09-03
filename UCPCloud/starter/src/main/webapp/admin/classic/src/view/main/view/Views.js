Ext.define('admin.view.main.view.Views', {
    extend: 'Ext.grid.Panel',
    xtype: 'views',

    controller: 'view',
    viewModel: 'view',
    bind: {
        title: '{listTitle}',
        store :  '{views}'
    },
    columns: [
        { text: '视图名称', flex : 1, dataIndex: 'viewName' },
        { text: '所属组',width: 200, dataIndex: 'groups' },
        { text: '所属用户',width: 200,  dataIndex: 'users' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button', iconCls: 'fa fa-refresh', text: '刷新', handler: 'refreshView' },
            { xtype: 'button', iconCls: 'fa fa-plus', text: '新增', handler: 'openCreateWin' },
            { xtype: 'button', iconCls: 'fa fa-times', text: '删除',handler: 'deleteView'}        ]
    }
       ],
    listeners:{
        "rowdblclick" : 'openModifyWin',
        "show" : function(e,eOpts){
                e.store.load();
        }
    }
});