Ext.define('explorer.view.main.SearchCondition', {
    extend: 'Ext.container.Container',
    xtype: 'searchcondition',

    controller: 'advsearch',
    defaults: {
        anchor: '100%'

    },
    margin :  '3 5 3 5',
    defaultType : 'textfield',
    layout: 'hbox',
    items: [{
        xtype: 'combobox',
        name : 'query',
        displayField: 'name',
        valueField: 'value',
        maxWidth : 120,
        store: Ext.create("Ext.data.Store", {
            fields: ["name", "value"],
            data: [
                { name: "must", value: "must" },
                { name: "must_not", value: "must_not" },
                { name: "should", value: "should" }
            ]
        })
    },{
        xtype: 'combobox',
        name : 'property',
        minWidth : 80,
        displayField: 'displayName',
        valueField: 'name',
        //bind: {
        //    store :  '{properties}'
        //},
        queryMode: 'local',
        listeners: {
            change: "changeProperty"
        }
    },
        {
            xtype: 'combobox',
            name : 'operator',
            displayField: 'name',
            valueField: 'value',
            maxWidth : 100,
            listeners: {
                change:"changeOperator"
            }
        },
        {
            name: 'value'
        },{
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    xtype: 'button',
                    text: '+',
                    style: {
                        'margin-left': '10px'
                    },
                    listeners: {
                        click: 'onAddButton'
                    }
                },
                {
                    xtype: 'button',
                    text: '-',
                    fieldReference: 'fieldInterval',
                    style: {
                        'margin-left': '10px'
                    },
                    listeners: {
                        click: 'onDeleteButton'
                    }
                }
            ]
        }]
});
