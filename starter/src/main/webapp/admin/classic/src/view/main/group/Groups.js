Ext.define('admin.view.main.group.Groups', {
    extend: 'Ext.grid.Panel',
    xtype: 'groups',

    controller: 'group',
    viewModel: 'group',
    bind: {
        title: '{listTitle}',
        store :  '{groups}'
    },
    columns: [
        { text: '组编号',width: 200, flex : 1, dataIndex: 'groupId' },
        { text: '组名称',width: 200, flex : 1, dataIndex: 'groupName' },
       { text: '创建人',width: 130, dataIndex: 'createdBy' },
              { text: '创建时间',width: 150, dataIndex: 'createdOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' },
              { text: '修改人',width: 130, dataIndex: 'lastUpdatedBy' },
              { text: '修改时间',width: 150, dataIndex: 'lastUpdatedOn', xtype: 'datecolumn',   format:'Y-m-d H:i:s' }
    ],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
			{ xtype: 'button', iconCls: 'fa fa-plus', text: '新增', handler: 'openCreateWin' },
            { xtype: 'button', iconCls: 'fa fa-times', text: '删除',handler: 'deleteGroup'}        ]
    },{
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        bind: {
            store :  '{groups}'
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