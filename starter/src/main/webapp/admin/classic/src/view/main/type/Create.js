Ext.define('admin.view.main.type.Create', {
    extend: 'Ext.window.Window',
    xtype: 'createtype',
    controller: 'type',
    viewModel: 'type',
    title:'新建类型',
    layout: 'center',
	bodyPadding: 5,
	width: '75%',
	height: '95%',
    layout: {
        type: 'border',
        padding: 5
    },
    initComponent: function () {
        var me = this;
        this.items = [{
            region: 'north',
            xtype: 'createTypeInfo'
        },{
            region: 'center',
            xtype: 'grid',
			minHeight : 300,
            bind: {
                title: '{propertyTitle}'
            },
            store : Ext.create('Ext.data.Store', {
                model: 'chemistry.model.Property'
            }),
            columns: [{
                header: '属性名称',
                dataIndex: 'name',
                width: 150
            }, {
                header: '属性类型',
                dataIndex: 'type',
                width: 100
            },  {
                header: '默认值',
                dataIndex: 'defaultValue',
                width: 130
            }, {
                header: '约束条件',
                dataIndex: 'pattern',
                width: 200
            }, {
                header: '约束提示',
                dataIndex: 'promptMessage',
                width: 220
            }, {
                header: '必填',
                dataIndex: 'required',
                width: 80,
                renderer: function (v, meta) {
                    if(v){
                        return '是';
                    }else{
                        return '否';
                    }

                }

            },
                //    {
                //    header: 'Index',
                //    dataIndex: 'index',
                //    width: 100
                //},{
                //    header: 'IndexAnalyzer',
                //    dataIndex: 'indexAnalyzer',
                //    width: 100
                //},{
                //    header: 'SearchAnalyzer',
                //    dataIndex: 'searchAnalyzer',
                //    width: 100
                //},
                {
                    header: '排序',
                    dataIndex: 'order',
                    width: 80,
					validator : function (val) {
							if(!val) return true;
							var strP =  /^-?\d+$/;
							if (!strP.test(val)) return '请输入一个整数';
							if(val > 2147483647||val < -2147483647) return '请输入一个整数';
							return true;
				 }
                }],
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                items: ['->',
                    { xtype: 'button', text: '新增', handler: 'addProperty' },
                    { xtype: 'button', text: '删除',handler: 'deleteProperty'}
                ]
            }],
            listeners: {
                itemdblclick: 'modifyProperty'
            }
        }];
        me.callParent();
    },
	scrollable : 'y',
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: '保存',
        handler : 'createSave'
    }]

});