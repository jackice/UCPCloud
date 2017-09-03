Ext.define('entry.view.init.InitForm', {
    extend: 'Ext.form.Panel',
    xtype: 'initForm',

    title: '初始化',
    frame: true,
    width: 320,
    bodyPadding: 10,

    defaultType: 'numberfield',

    items: [],

    buttons: [
        {
            text: '初始化',
            handler: 'do'
        }
    ],

    initComponent: function () {
        this.defaults = {
            anchor: '100%',
            labelWidth: 120
        };

        this.callParent();
    }
});