Ext.define('admin.view.main.type.CreateTypeInfo', {
    extend: 'Ext.form.Panel',
    xtype: 'createTypeInfo',
    controller: 'type',
    viewModel: 'type',
    bodyPadding: 5,

    items: [{
        xtype: 'fieldset',
        title: '类型基本信息',
        defaultType: 'textfield',
        defaults: {
            anchor: '100%'
        },
        items: [
            {allowBlank: false, fieldLabel: '类型名称', name: 'name', emptyText: '',regex : /^([A-Za-z0-9]{1})([\w]*)$/,regexText:'只能输入数字,字母和下划线,不能下划线开头'},
            {allowBlank: false, fieldLabel: '显示名称', name: 'displayName', emptyText: ''},
            {allowBlank: true, fieldLabel: '描述', name: 'description', emptyText: ''}
        ]}]
});