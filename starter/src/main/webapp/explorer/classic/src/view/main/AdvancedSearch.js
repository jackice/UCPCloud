Ext.define('explorer.view.main.AdvancedSearch', {
    extend: 'Ext.form.Panel',
    xtype: 'advancedsearch',

    controller: 'advsearch',
    bodyPadding: 5,
    fieldDefaults: {
        labelAlign: 'left',
        labelWidth: 150,
        msgTarget: 'side'
    },
    scrollable : true,
    items: [{
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
		width: 1050,
        defaults: {
            anchor: '100%'
        },
        items: [{
            xtype : 'searchcondition'
        }]
    },
        {
            fieldLabel: 'minimum_should_match',
            xtype: 'numberfield',
            name: 'minimum_should_match',
            value: 1,
            maxValue: 99,
            minValue: 1
        }],
    buttons: [{
        text: '搜索',
        handler: 'search'
    }]
});
