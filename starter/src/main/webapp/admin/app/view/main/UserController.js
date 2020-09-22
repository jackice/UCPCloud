Ext.define('admin.view.main.UserController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.user',

    openCreateWin: function () {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建用户',
            items: [{
                xtype: 'createUser',
                store: this.getView().store
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '修改用户',
            items: [{
                xtype: 'modifyUser',
                record: record
            }]
        }).show();
        return;
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        this.getView().getForm().loadRecord(record);
    },
    deleteUser: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
        if (record[0].get('userId') == 'admin') {
            Ext.Msg.alert('提示信息', '管理员账号不能删除！');
            return;
        }
        var userId = Ext.util.Cookies.get('userId');
        if (record[0].get('userId') == userId) {
            Ext.Msg.alert('提示信息', '当前用户不能删除！');
            return;
        }
        Ext.Msg.confirm("确认", "是否删除该用户?", function (r) {
            if(r==='yes'){
                var store = record[0].store;
                store.remove(record[0]);
                me.sync(store);
            }
        });
    },

    createSave: function (e) {
        var me = this;
        var form = this.getView().getForm();
        if (!form.isValid())return;
        var store = me.getView().store;
        var user = Ext.create('chemistry.model.User', form.getValues());
        user.phantom = true;
        var view = this.getView();
        var doc = Ext.getBody( );
        var window = view.up('window');
        doc.mask('加载中...');
        view.mask('加载中...');
        user.save({
            callback: function(record, operation, success) {
                if (!success) {
                    var error =  Ext.decode(operation.error.response.responseText);
                    view.unmask();
                    doc.unmask();
                    Ext.toast({
                        html:  '保存失败!<br />'+error.reason,
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    return;
                }
                setTimeout(function(){
                    view.unmask();
                    doc.unmask();
                    window.close();
                    Ext.toast({
                        html: '保存成功',
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                },2000);
            }
        });
    },

    modifySave: function (e) {
        var me = this;
        var form = e.up('form').getForm();
        if (!form.isValid())return;
        var store = me.getView().record.store;
        var record = me.getView().record;
        record.beginEdit( );
        record.set(form.getValues());
        var view = this.getView();
        var window = view.up('window');
        var doc = Ext.getBody( );
        doc.mask('加载中...');
        view.mask('加载中...');
        record.save({
            callback: function(record, operation, success) {
                if (!success) {
                    var error =  Ext.decode(operation.error.response.responseText);
                    view.unmask();
                    doc.unmask();
                    Ext.toast({
                        html:  '修改失败!<br />'+error.reason,
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    record.cancelEdit( );
                    return;
                }
                setTimeout(function(){
                    view.unmask();
                    doc.unmask();
                    window.close();
                    Ext.toast({
                        html: '修改成功',
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                },2000);
            }
        });
    } ,

    sync: function (store) {
        var me = this;
        var view = me.getView();
        var window = view.up('window');
        var doc = Ext.getBody( );
        doc.mask('加载中...');
        if(window)window.mask('加载中...');
        store.sync({
            callback: function (batch, options) {
                if(batch.exceptions&&batch.exceptions.length>0){
                    if(window)window.unmask();
                    doc.unmask();
                    var error =  Ext.decode(batch.exceptions[0].error.response.responseText);
                    Ext.toast({
                        html:  '操作失败!<br />'+error.reason,
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                }else{
                    setTimeout(function(){
                        if (window){
                            window.unmask();
                            window.close();
                        }
                        doc.unmask();
                    Ext.toast({
                        html: '操作成功',
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    },2000);
                }

            }
        });


    }


});
