Ext.define('admin.view.main.type.Types', {
    extend: 'Ext.grid.Panel',
    xtype: 'types',

    controller: 'type',
    viewModel: 'type',
    bind: {
        title: '{listTitle}',
        store :  '{types}'
    },
    columns: [
        //{ text: 'Name',width: 200, flex : 1, dataIndex: 'name' },
        { text: '类型名称',width: 300, dataIndex: 'displayName' },
        { text: '描述',width: 200, flex : 1, dataIndex: 'description' }
    ],
    initComponent: function () {
        var me = this;
        this.dockedItems =  [{
            xtype: 'toolbar',
            dock: 'top',
            items: ['->',
                { xtype: 'button', iconCls: 'fa fa-download', text: '下载', handler: 'downloadType' },
                { xtype: 'button', iconCls: 'fa fa-upload', text: '上传', handler: 'uploadType' },
                { xtype: 'button', iconCls: 'fa fa-refresh', text: '刷新', handler: 'refreshType' },
                { xtype: 'button', iconCls: 'fa fa-plus', text: '新增', handler: 'openCreateWin' },
                { xtype: 'button', iconCls: 'fa fa-times', text: '删除',handler: 'deleteType'}            ]
        }];
        me.callParent();
    },
    listeners:{
        "rowdblclick" : 'openModifyWin',
        "show" : function(e,eOpts){
                e.store.load();
        }
    }
});