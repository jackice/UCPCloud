Ext.define('explorer.view.main.MutiFile', {
    extend: 'Ext.form.field.File',
    xtype: 'multifile',


    onRender: function () {
        var me = this,
            inputEl, button, buttonEl, trigger;

        me.callParent(arguments);

        me.fileInputEl.dom.multiple = 'multiple';
        //inputEl = me.inputEl;
        //inputEl.
    },

    reset : function(){
        var me = this;
        me.callParent();
        me.fileInputEl.dom.multiple = 'multiple';
    }
});