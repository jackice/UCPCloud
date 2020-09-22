Ext.define('admin.view.component.Range', {
    extend: 'Ext.container.Container',
    xtype: 'range',
    layout: 'hbox',

    initComponent: function () {
        Ext.apply(this, {
            items: [{
                xtype: 'combo',
                name: 'gt',
                displayField: 'name',
                valueField: 'value',
                value: 'gt',
                maxWidth: 80,
                editable: false,
                store: Ext.create('Ext.data.Store', {
                    data: [{value: 'gt', name: '>'}, {value: 'gte', name: '>='}]
                })
            }, {
                xtype: 'textfield',
                name: 'gtValue',
                maxWidth: 120,
                validator: this.validator ? admin.view.component.Validators[this.validator] : undefined
            }, {
                xtype: 'combo',
                name: 'lt',
                displayField: 'name',
                valueField: 'value',
                maxWidth: 80,
                value: 'lt',
                editable: false,
                store: Ext.create('Ext.data.Store', {
                    data: [{value: 'lt', name: '<'}, {value: 'lte', name: '<='}]
                })
            }, {
                xtype: 'textfield',
                name: 'ltValue',
                maxWidth: 120,
                validator: this.validator ? admin.view.component.Validators[this.validator] : undefined
            }
            ]
        });
        this.callParent();
    },

    getValue: function () {
        var gt = this.down('combo[name=gt]').getValue();
        var gtValue = this.down('textfield[name=gtValue]').getValue(gtValue);
        var lt = this.down('combo[name=lt]').getValue();
        var ltValue = this.down('textfield[name=ltValue]').getValue();
        var result = {};
        result[gt] = gtValue;
        result[lt] = ltValue;
        return result;
    },

    setValue: function (value) {
        var gt = value['gte'] ? 'gte' : 'gt';
        var lt = value['lte'] ? 'lte' : 'lt';
        var gtValue = value[gt];
        var ltValue = value[lt];
        this.down('combo[name=gt]').setValue(gt);
        this.down('textfield[name=gtValue]').setValue(gtValue);
        this.down('combo[name=lt]').setValue(lt);
        this.down('textfield[name=ltValue]').setValue(ltValue);

    }


});