Ext.define('admin.view.main.user.Create', {
    extend: 'Ext.form.Panel',
    xtype: 'createUser',
    controller: 'user',
    viewModel: 'user',
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
        fieldLabel: '用户编号',
        name: 'userId',
        allowBlank: false
    },{
        fieldLabel: '用户名称',
        name: 'userName',
        allowBlank: false
    },{
        fieldLabel: '邮箱',
        name: 'email',
        vtype: 'email',
        allowBlank: true
    },{
        fieldLabel: '密码',
        name: 'password',
        allowBlank: false
    }]
});