Ext.define('admin.view.main.reindex.IndexCondition', {
    extend: 'Ext.form.Panel',
    xtype: 'indexCondition',
    controller: 'reIndex',
    viewModel: 'reIndex',
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    initComponent: function () {
        var me = this;
        this.items =  [ {
            xtype: 'combo',
            fieldLabel: '类型',
            name: 'type',
            displayField: 'displayName',
            allowBlank: false,
            valueField: 'name',
            bind: {
                store :  '{types}'
            }
        }];
        me.callParent();
    },
    buttons: [{
        text: '重建索引',
        handler: 'reIndex'
    }]

});
