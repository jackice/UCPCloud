Ext.define('admin.view.main.LogController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.log',
    showWin : function(grid, record, tr, rowIndex, e, eOpts){
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '查看日志',
            items: [{
                xtype: 'log',
                record: record
            }]
        }).show();
    },
    searchLog : function(){
        var store = this.getViewModel().getStore('logs');
        var query = this.getView().down('toolbar').down('textfield[name=query]').getValue();

        store.on("beforeload", function(){
            Ext.apply(store.proxy.extraParams, {query: query});
        });
        store.load();
    },
    loadData : function(){
        var me = this;
        var record = this.getView().record;
        var data = record.getData();
        var form = this.getView().getForm();
        Ext.Object.each(data, function(key, value, myself) {
            if(Ext.isObject(value)){
                Ext.Object.each(value, function(k, v, myself) {
                        me.findObjAndSetValue(key+'.'+k,v);
                });
            }
            if(Ext.isString(value)){
                me.findObjAndSetValue(key,value);
            }
        });
        //this.getView().getForm().loadRecord(record);
    },
    findObjAndSetValue : function(name,value){
        var form = this.getView();
        var textfield = form.query('textfield[name='+name+']');
        var textareafield = form.query('textareafield[name='+name+']');
        if(textfield&&textfield.length>0){
            textfield[0].setValue(value);
        }
        if(textareafield&&textareafield.length>0){
            textareafield[0].setValue(value);
        }
    }
});
