Ext.define('explorer.view.main.FullTextSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'fulltext',

    controller: 'mainheader',
    //bind: {
    //    store: '{fulltext}'
    //},
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: [
            {xtype:'label',text:'搜索关键字:'},
            {xtype:'label',itemId:'searchText',text:''},
            '->',
            {xtype: 'button', iconCls: 'fa fa-file-text', text: '详细信息', handler: 'detail'},
            {xtype: 'button', iconCls: 'fa fa-times', text: '删除', handler: 'deleteDoc'}
        ]
    }, {
        xtype: 'toolbar',
        dock: 'bottom',
        items: [
            {xtype:'label',text:'显示数量::'},
            {
            xtype: 'combo',
            store: [25, 50, 100],
            value: 25,
            forceSelection: true,
            width: 100,
            listeners:{
                select:'pageSizeChanged'
            }
        }]
    }],

    initComponent: function () {
        Ext.apply(this, {
            store: [],
            features: [{
                ftype: 'rowbody',
                setupRowData: function (record, rowIndex, rowValues) {
                    var headerCt = this.view.headerCt,
                        colspan = headerCt.getColumnCount();
                    var html = ''
                    Ext.Object.each(record.get("_highlight"), function (key, value) {
                        html += '<div style="padding: 1em">' + value + '</div>'
                    });
                    Ext.apply(rowValues, {
                        rowBody: html,
                        rowBodyColspan: colspan
                    });
                }
            }],
            viewConfig: {
                trackOver: false,
                stripeRows: false
            },
            selType: 'checkboxmodel',
            columns: [
                {text: '分数', dataIndex: '_score', xtype: 'numbercolumn', flex: 1, sortable: false},
                {text: '文档名称', dataIndex: 'name', flex: 1, sortable: false}, {
                    header: '类型',
                    dataIndex: '_type',
                    sortable: false,
                    flex: 1,
                    renderer: function (v, meta) {
                        return v.displayName;
                    }
                },
                {text: '创建人', dataIndex: 'createdBy', flex: 1, sortable: false},
                {
                    text: '创建时间',
                    xtype: 'datecolumn',
                    format: 'Y-m-d H:i:s',
                    dataIndex: 'createdOn',
                    flex: 1,
                    sortable: false
                },
                {text: '修改人', dataIndex: 'lastUpdatedBy', flex: 1, sortable: false},
                {
                    text: '修改时间',
                    xtype: 'datecolumn',
                    format: 'Y-m-d H:i:s',
                    dataIndex: 'lastUpdatedOn',
                    flex: 1, sortable: false
                }
            ]

        });
        this.callParent();
    },

    listeners: {
        itemdblclick: 'showImage'
    }
});
