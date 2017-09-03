Ext.define('admin.view.main.ReIndexController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.reIndex',
    reIndex : function(e){
        var me = this;
        var form = this.getView().getForm();
        var flag = false;
        var params = null;
        if (!form.isValid()) return;
        Ext.Object.eachValue(form.getValues(), function (value) {
            if(value!=''){
                flag = true;
            }
        });
        if(flag){
            params = form.getValues();
        }
        this.getView().btn.setDisabled(true);
        Ext.Ajax.request({
            url: '/svc/_reindex',
            method : 'POST',
            params : params,
                callback: function (options, success, response) {
                if (!success) {
                    var error =  Ext.decode(response.responseText);
                    Ext.toast({
                        html:  '保存失败!<br />'+error.reason,
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    return;
                }
                if(response.responseText&&response.responseText!=''){
                    var result = Ext.decode(response.responseText);
                    if(result.operationId&&result.operationId!=''){
                        me.getViewModel().set({'operationId':result.operationId});
                        me.getViewModel().set({'reindexType':result.type});
                        me.refresh();
                    }
                }
                    me.getView().up('window').close();
            }
        });
    },
    onAxisLabelRender :  function (axis, label, layoutContext) {
        return layoutContext.renderer(label) + '%';
    },
    onSeriesTooltipRender : function(tooltip, record, item){
        tooltip.setHtml(record.get('finished') + ': ' + record.get('total') );
    },
    openWin : function(e){
        var me = this;
        var btn =  me.getView().getReferences().reIndexButton;
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '重建索引条件',
            items: [{
                xtype: 'indexCondition',
                btn :btn
            }]
        }).show();
        btn.setDisabled(true);
        return;
    },
    onTimeChartRendered : function(){
        this.timeChartTask = Ext.TaskManager.start({
            run: this.loadData,
            interval: 5000,
            scope: this
        });
    },
    onTimeChartDestroy : function(){
        if (this.timeChartTask) {
            Ext.TaskManager.stop(this.timeChartTask);
        }
    },
    loadData : function(callback){
        var me = this;
        Ext.Ajax.request({
            url: '/svc/_reindex/_status',
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if(response.responseText&&response.responseText!=''){
                    var result = Ext.decode(response.responseText);
                    if(result.operationId&&result.operationId!=''){
                        me.getViewModel().set({'operationId':result.operationId});
                        me.getViewModel().set({'reindexType':result.type});
                        me.refresh();
                    }
                    if(result.isFinished){
                        me.getView().getReferences().reIndexButton.setDisabled(false);
                    }else{
                        me.getView().getReferences().reIndexButton.setDisabled(true);
                    }
                }
            }
        });
        return true;
    },
    refresh : function(){
        var me = this;
        var operationId =  me.getViewModel().get('operationId');
        var store = me.getViewModel().getStore('reIndexs');
        if(operationId&&operationId!=''){
            store.load({
                params : {'operationId':operationId}
            });
        }else{
            me.onTimeChartDestroy();
        }
    }

});
