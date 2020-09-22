Ext.define('admin.view.main.group.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createGroup',
    controller: 'group',
    viewModel: 'group',
    requires : [
        'Ext.ux.form.ItemSelector',
        'Ext.ux.ajax.JsonSimlet',
        'Ext.ux.ajax.SimManager'
    ],
    initComponent: function(){
        var me = this;
        Ext.apply(this, {
            buttons: [{
                text: '关闭',
                handler: function() {
                    this.up('window').close();
                }
            }, {
                text: '保存',
                formBind: true,
                disabled: true,
                handler: 'createSave'
            }]

        });
        this.callParent();
    },
    bodyPadding: 5,
    scrollable : true,
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        fieldLabel: '组编号',
        name: 'groupId',
        allowBlank: false
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
        },{
            title : '包含的组',
            fixed : true,
            items : [ {
                xtype: 'itemselector',
                name: 'childGroups',
                id: 'itemselector-Groups',
                fieldLabel: '组',
                delimiter:null,
                store : {type : 'groups',pageSize:10000},
                displayField: 'groupName',
                valueField: '_id',
                msgTarget: 'side',
                width  : 600,
                height : 300,
                scrollable : true,
                fromTitle: '有效的',
                toTitle: '已选择的'
            }]
        }]
    }]
});