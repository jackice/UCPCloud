Ext.define('explorer.view.main.Documents', {
    extend: 'Ext.grid.Panel',
    xtype: 'documents',

    controller: 'document',
    //bind: {
    //    store :  '{documents}'
    //},
    //session: true,
    //store: {type: 'documents'},
    selModel : 'checkboxmodel',
    //initComponent: function () {
        //var query = this.getView().query;
        //var params = {};
        //if (this.getView().limit && this.getView().limit != '') {
        //
        //    params = {
        //        highlight: true,
        //        query: query,
        //        limit: this.getView().limit
        //    };
        //} else {
        //    params = {
        //        highlight: false,
        //        query: query,
        //        types: this.getView().qType
        //    };
        //}
        //var store = Ext.create('explorer.store.Documents', {
        //    autoLoad: true
        //});
        //store.getProxy().extraParams = params;
        //e.bindStore(store);
        //this.down('pagingtoolbar').bindStore(store);

        //Ext.apply(this, {
        //    store: store,
        //    dockedItems: [{
        //        xtype: 'toolbar',
        //        dock: 'top',
        //        items: ['->',
        //            {xtype: 'button', text: '详细信息', handler: 'detail'},
        //            {xtype: 'button', text: '删除', handler: 'deleteDoc'}
        //        ]
        //    }, {
        //        xtype: 'pagingtoolbar',
        //        dock: 'bottom',
        //        displayInfo: true,
        //        displayMsg: 'Displaying Contents {0} - {1} of {2}',
        //        emptyMsg: "No Contents to display"
        //    }]
        //});
    //    this.callParent();
    //},
    columns: [{
        header: '文档名称',
        dataIndex: 'name'
    }, {
        header: '类型',
        dataIndex: '_type',
        flex: 1,
        renderer: function (v, meta) {
            return v.displayName;
        }
    }, {
        header: '创建时间',
        dataIndex: 'createdOn',
        xtype: 'datecolumn',
        format: 'Y-m-d H:i:s',
        flex: 1
    }, {
        header: '创建人',
        dataIndex: 'createdBy',
        flex: 1
    }, {
        header: '修改时间',
        dataIndex: 'lastUpdatedOn',
        xtype: 'datecolumn',
        flex: 1,
        format: 'Y-m-d H:i:s'
    }, {
        header: '修改人',
        dataIndex: 'lastUpdatedBy',
        flex: 1
    }],

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            {xtype: 'button', iconCls: 'fa fa-file-text', text: '详细信息', handler: 'detail'},
            {xtype: 'button', iconCls: 'fa fa-times', text: '删除', handler: 'deleteDoc'}
        ]
    }, {
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        displayInfo: true,
        displayMsg: '显示 {0} - {1}条，共 {2} 条',
        emptyMsg: "没有数据"
    }],

    listeners: {
        afterrender: 'loadData',
        itemdblclick: 'showImage'
    }
});
