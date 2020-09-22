Ext.define('admin.view.component.Term', {
    extend: 'Ext.container.Container',
    xtype: ['term', 'wildcard', 'fuzzy'],
    layout: 'hbox',
    initComponent: function () {
        Ext.apply(this, {
            items: [{
                xtype: 'textfield',
                name: 'inputValue',
                value: this.value,
                validator: this.validator ? admin.view.component.Validators[this.validator] : undefined,
                maxWidth: 100
            }]
        });
        this.callParent();
    },

    getValue: function () {
        return this.down('textfield[name=inputValue]').getValue();
    },

    setValue: function (value) {
        this.down('textfield[name=inputValue]').setValue(value);
    }
});