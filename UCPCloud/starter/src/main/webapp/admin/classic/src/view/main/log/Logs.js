Ext.define('admin.view.main.log.Logs', {
    extend: 'Ext.grid.Panel',
    xtype: 'logs',

    controller: 'log',
    viewModel: 'log',
    bind: {
        title: '{listTitle}',
        store: '{logs}'
    },
    initComponent: function () {
        Ext.apply(this, {
            defaults:{
                listeners: {
                    specialkey: function(field, e){
                        if (e.getKey() == e.ENTER) {
                            var button = field.up('grid').down('button[itemId=search]');
                            button.click(e);
                        }
                    }
                }
            }
        });
        this.callParent();
    },
    sortableColumns: false,
    columns: [
        {text: '操作用户', width: 200, flex: 1, dataIndex: 'userName'},
        {
            text: '开始时间', width: 150, flex: 1, dataIndex: 'timeInfo',
            renderer: function (v, meta) {
                return v.start_format;
            }
        },
        {
            text: '结束时间', width: 150, dataIndex: 'timeInfo',
            renderer: function (v, meta) {
                return v.end_format;
            }
        },
        {
            text: '消耗', width: 100, dataIndex: 'timeInfo',
            renderer: function (v, meta) {
                return v.consume_format;
            }
        },
        {
            text: 'IP地址', width: 200, dataIndex: 'requestInfo',
            renderer: function (v, meta) {
                return v.ipAddress;
            }
        },
        {
            text: '方法', width: 130, dataIndex: 'requestInfo',
            renderer: function (v, meta) {
                return v.method;
            }
        }
    ],
    dockedItems: [{
        dock: 'top',
        xtype: 'toolbar',
        items: [
            {
                xtype : 'form',
                layout: 'hbox',
                defaults:{
                    listeners: {
                        specialkey: function(field, e){
                            if (e.getKey() == e.ENTER) {
                                var button = field.up('form').down('button[itemId=search]');
                                button.click(e);
                            }
                        }
                    }
                },
                items : [{
                    xtype: 'textfield',
                    name: 'query',
                    fieldLabel: '查询条件'
                }, {
                    xtype: 'button',
                    itemId : 'search',
                    text: '查询',
                    iconCls: 'fa fa-search x-btn-icon-el-soft-green-small',
                    handler: 'searchLog'
                }]
            }
           ]
    }, {
        xtype: 'pagingtoolbar',
        dock: 'bottom',
        bind: {
            store: '{logs}'
        }
    }],
    listeners: {
        "rowdblclick": 'showWin'
    }
});