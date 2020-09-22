Ext.define('admin.view.main.type.Modify', {
    extend: 'Ext.window.Window',
    xtype: 'modifytype',
    controller: 'type',
    viewModel: 'type',
    title:'修改类型',
    layout: 'center',
	bodyPadding: 6,
	scrollable: 'y',
	width: '75%',
	height: '95%',
	items: [{
				width: '100%',
				height: '100%',
                bodyPadding: '10 20'
            }],
    layout: {
        type: 'border',
        padding: 5
    },
    items:[{
        region: 'north',
        xtype: 'modifyTypeInfo'
    },{
        region: 'center',
        xtype: 'grid',
		minHeight : 300,
        bind: {
            title: '{propertyTitle}'
        },
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
                { xtype: 'button', text: '删除',handler: 'deleteModifyProperty'}
            ]
        }],
        listeners: {
            itemdblclick: 'modifyProperty'
        }
    }],
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: '保存',
        handler : 'modifySave'
    }],
    listeners: {
        afterrender:'loadModifyData'
    }

});