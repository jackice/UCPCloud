Ext.define('admin.view.main.tag.Tags', {
    extend: 'Ext.grid.Panel',
    xtype: 'tags',

    controller: 'tag',
    viewModel: 'tag',
    bind: {
        title: '{listTitle}',
        store :  '{tags}'
    },
    columns: [
        { text: '标签名称', flex : 1, dataIndex: 'tagContext' },
        { text: '描述', flex : 1, dataIndex: 'description' }],
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
			{ xtype: 'button', iconCls: 'fa fa-plus', text: '新增', handler: 'openCreateWin' },
            { xtype: 'button', iconCls: 'fa fa-times', text: '删除',handler: 'deleteTag'}        ]
    }
        ,{
            xtype: 'pagingtoolbar',
            dock: 'bottom',
            bind: {
                store :  '{tags}'
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