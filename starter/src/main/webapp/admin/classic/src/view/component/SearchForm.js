Ext.define('admin.view.component.SearchForm', {
    extend: 'Ext.form.Panel',
    xtype: 'searchform',
    bodyPadding: 5,
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },
    initComponent: function () {
        //Ext.apply(this, {
        //    items: [{
        //        xtype: 'tagfield',
        //        fieldLabel: '类型',
        //        name: 'type',
        //        displayField: 'displayName',
        //        allowBlank: false,
        //        valueField: 'name',
        //        bind: {
        //            store: '{types}'
        //        },
        //        filterPickList: true,
        //        publishes: 'value',
        //        listeners: {
        //            change: "changeType"
        //        }
        //    }, {
        //        xtype: 'fieldset',
        //        title: '条件',
        //        defaults: {
        //            anchor: '100%'
        //        },
        //        items: [{
        //            xtype : 'searchcondition'
        //        }]
        //    },
        //        {
        //            fieldLabel: 'minimum_should_match',
        //            xtype: 'textfield',
        //            name: 'minimum_should_match',
        //            value: '1'
        //        }]
        //});
        this.callParent();

    }

});