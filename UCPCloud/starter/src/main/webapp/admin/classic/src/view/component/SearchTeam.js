Ext.define('admin.view.component.SearchTeam', {
    extend: 'Ext.container.Container',
    xtype: 'searchteam',
    requires: ['admin.view.component.Validators'],
    layout: 'hbox',
    operatorMap: {
        'string': ['term', 'fuzzy', 'wildcard'],
        'integer': ['term', 'range'],
        'float': ['term', 'range'],
        'boolean': ['term'],
        'date': ['term', 'range']
    },

    initComponent: function () {
        Ext.apply(this, {
            items: [
                {
                    xtype: 'combo',
                    name: 'condition',
                    minWidth: 50,
                    displayField: 'name',
                    valueField: 'type',
                    value: 'must',
                    editable: false,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['name', 'type'],
                        data: [{name: 'must', value: 'must'}, {name: 'must not', value: 'must_not'}, {
                            name: 'shuold',
                            value: 'shuold'
                        }]
                    })
                }, {
                    xtype: 'combo',
                    name: 'property',
                    minWidth: 80,
                    displayField: 'name',
                    valueField: 'name',
                    queryMode: 'local',
                    store: Ext.create('Ext.data.Store', {fields: ['name', 'type'], data: this.properties}),
                    getType: function () {
                        var index = this.getStore().findBy(function (row) {
                            return row.get('name') === this.getValue();
                        }.bind(this));
                        return this.getStore().getAt(index).get('type')
                    },
                    listeners: {
                        change: function (property, newValue, oldValue, eOpts) {
                            var operator = property.nextSibling('combo[name=operator]');
                            var operators = this.operatorMap[property.getType()];
                            operator.setValue(null);
                            operator.bindStore(operators);
                            operator.setValue(operators[0]);
                        }.bind(this)
                    }
                }, {
                    xtype: 'combo',
                    name: 'operator',
                    editable: false,
                    maxWidth: 120,
                    listeners: {
                        change: function (operator, newValue, oldValue, eOpts) {
                            var property = operator.previousSibling('combo[name=property]');
                            var inputValue = operator.nextSibling('container[name=inputValue]');
                            if (inputValue)this.remove(inputValue);
                            this.add({
                                xtype: newValue,
                                name: 'inputValue',
                                validator: property.getType()
                            });
                        }.bind(this)
                    }
                }, {
                    xtype: 'button', text: 'value', handler: function () {
                      console.log(JSON.stringify(this.getValue()))
                    }.bind(this)
                }
            ],
            listeners: {
                afterrender: function () {
                    this.setValue(this.value);
                }.bind(this)
            }
        });
        this.callParent();

    },

    setValue: function () {
        var operatorValue = Ext.Object.getAllKeys(this.value)[0];
        var propertyValue = Ext.Object.getAllKeys(this.value[operatorValue])[0];
        var valueValue = this.value[operatorValue][propertyValue];
        this.down('combo[name=property]').setValue(propertyValue);
        this.down('combo[name=operator]').setValue(operatorValue);
        this.down('container[name=inputValue]').setValue(valueValue);
    },

    getValue: function () {
        var propertyValue = this.down('combo[name=property]').getValue();
        var operatorValue = this.down('combo[name=operator]').getValue();
        var valueValue = this.down('container[name=inputValue]').getValue();
        var result = {};
        result[operatorValue] = {};
        result[operatorValue][propertyValue] = valueValue;
        return result;
    }
});