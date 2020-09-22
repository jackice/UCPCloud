Ext.define('admin.view.main.view.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyView',
    controller: 'view',
    viewModel: 'view',
    frame: true,
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },

    items: [{
        name: '_id',
        xtype: 'hiddenfield'
    },{
        fieldLabel: '视图名称',
        xtype: 'textfield',
        name: 'viewName',
        readOnly: true
    },{
        xtype: 'tagfield',
        fieldLabel: '类型',
        name: 'type',
        displayField: 'displayName',
        allowBlank: false,
        valueField: 'name',
        bind: {
            store: '{types}'
        },
        filterPickList: true,
        publishes: 'value',
        listeners: {
            change: "changeType"
        }
    }, {
        xtype: 'fieldset',
        title: '条件',
		width: 1050
        //defaults: {
//            anchor: '100%'
//        }
        //items: [{
        //    xtype : 'searchcondition'
        //}]
    },
        {
            fieldLabel: 'minimum_should_match',
            xtype: 'numberfield',
            name: 'minimum_should_match',
            value: 1,
            maxValue: 99,
            minValue: 1
        },
        {
            fieldLabel: '权限',
            xtype: 'tagfield',
            name : 'permissionObj',
            displayField: 'name',
            valueField: 'id',
            forceSelection: true
        }],
    listeners: {
        afterrender : 'loadModifyData'
    },
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    },{
        text: '保存',
        handler: 'save'
    }]
});