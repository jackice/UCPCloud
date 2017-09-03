Ext.define('admin.view.main.type.ModifyTypeInfo', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyTypeInfo',
    controller: 'type',
    viewModel: 'type',
    bodyPadding: 5,
    layout: 'anchor',
    items: [{
        xtype: 'fieldset',
        title: '类型基本信息',
        defaultType: 'textfield',
        defaults: {
            anchor: '100%'
        },
        items: [
            {allowBlank: false, fieldLabel: '类型名称',  readOnly: true,name: 'name', emptyText: ''},
            {allowBlank: false, fieldLabel: '显示名称', name: 'displayName', emptyText: ''},
            {allowBlank: true, fieldLabel: '描述', name: 'description', emptyText: ''}
        ]}]
});