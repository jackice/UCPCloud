Ext.define('admin.view.main.tag.Modify', {
    extend: 'Ext.form.Panel',
    xtype: 'modifyTag',
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
                handler: 'modifySave'
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
        xtype : 'hiddenfield',
        name: '_id'
    },{
        fieldLabel: '标签名称',
        name: 'tagContext',
        readOnly: true
    },{
        fieldLabel: '标签描述',
        name: 'description'
    }],
    listeners: {
        afterrender : 'loadModifyData'
    }
});