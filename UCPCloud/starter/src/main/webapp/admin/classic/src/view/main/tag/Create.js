Ext.define('admin.view.main.tag.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createTag',
    controller: 'tag',
    viewModel: 'tag',
    initComponent: function(){
        var me = this;
        Ext.apply(this, {
            buttons: [{
                text: '关闭',
                handler: function() {
                    this.up('window').close();
                }
            }, {
                text: '保存',
                formBind: true, //only enabled once the form is valid
                disabled: true,
                handler: 'createSave'
            }]

        });
        this.callParent();
    },
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        fieldLabel: '标签名称',
        name: 'tagContext',
        allowBlank: false
    },{
        fieldLabel: '标签描述',
        name: 'description'
    }]
});