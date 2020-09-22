Ext.define('admin.view.main.user.Users', {
    extend: 'Ext.grid.Panel',
    xtype: 'users',

    controller: 'user',
    viewModel: 'user',
    bind: {
        title: '{listTitle}',
        store :  '{users}'
    },
        columns: [
        { text: '用户编号',width: 200, flex : 1, dataIndex: 'userId' },
        { text: '用户名称',width: 200, flex : 1, dataIndex: 'userName' },
        { text: '创建人',width: 130, dataIndex: 'createdBy' },
        { text: '创建时间',width: 150, dataIndex: 'createdOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' },
        { text: '修改人',width: 130, dataIndex: 'lastUpdatedBy' },
        { text: '修改时间',width: 150, dataIndex: 'lastUpdatedOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button',iconCls: 'fa fa-user-plus', text: '新增', handler: 'openCreateWin' },
            { xtype: 'button',iconCls: 'fa fa-user-times',  text: '删除',handler: 'deleteUser'}        ]
    },{
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        activeItem : 1,
        bind: {
            store :  '{users}'
        },
        displayInfo: true,
        displayMsg: '显示 {0} - {1}条，共 {2} 条',
        emptyMsg: "没有数据"
    }],
    listeners:{
        "rowdblclick" : 'openModifyWin',
        "show" : function(e,eOpts){
            if(e.store&&(e.down('pagingtoolbar').store===e.store)){
                e.store.load();
            }
        }
    }
});