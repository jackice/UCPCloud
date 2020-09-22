Ext.define('admin.view.main.group.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyGroup',
    controller: 'group',
    viewModel: 'group',
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: '保存',
        formBind: true, //only enabled once the form is valid
        disabled: true,
        handler: 'modifySave'
    }],
    bodyPadding: 5,
	scrollable : 'y',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [{
                name: 'groupId',
                readOnly: true,
                fieldLabel: '组编号'
            },{
                fieldLabel: '组名',
                name: 'groupName',
                allowBlank: false
            },{
                xtype: 'tabpanel',
                defaults: {
                    bodyPadding: 10,
                    scrollable: true
                },
                items: [{
                    title : '包含的用户',
                    fixed : true,
                    items : [{
                        xtype: 'itemselector',
                        name: 'users',
                        id: 'itemselector-Users',
                        fieldLabel: '用户',
                        delimiter:null,
                        store : {type : 'users',pageSize:10000, sorters  : [{
                            property : "userId",
                            direction: "DESC"
                        }]},
                        displayField: 'userName',
                        valueField: '_id',
                        msgTarget: 'side',
                        height : 300,
                        width  : 600,
                        scrollable : true,
                        fromTitle: '有效的',
                        toTitle: '已选择的'
                    }]
                }, {
                    title : '包含的组',
                    fixed : true,
                    items : [{
                        xtype: 'itemselector',
                        name: 'childGroups',
                        id: 'itemselector-Groups',
                        anchor: '100%',
                        fieldLabel: '组',
                        delimiter:null,
                        store : {type : 'groups',pageSize:10000},
                        displayField: 'groupName',
                        valueField: '_id',
                        msgTarget: 'side',
                        height : 300,
                        width  : 600,
                        scrollable : true,
                        fromTitle: '有效的',
                        toTitle: '已选择的'
                    }]
                }]
            }]
        });

        this.callParent();
    },
    listeners: {
        afterrender : 'loadModifyData'
    }
});