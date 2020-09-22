Ext.define('admin.view.main.user.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyUser',
    controller: 'user',
    viewModel: 'user',
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: '保存',
        formBind: true, //only enabled once the form is valid
        disabled: true,
        handler: 'modifySave'
    }],
    bodyPadding: 5,
    width: 480,
    layout: 'anchor',
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        name: '_id',
        xtype: 'hiddenfield'
    },{
        fieldLabel: '用户编号',
        name: 'userId',
        readOnly: true
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
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});