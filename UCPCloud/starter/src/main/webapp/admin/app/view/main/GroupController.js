Ext.define('admin.view.main.GroupController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.group',

    openCreateWin: function (sender, record) {
        Ext.create('Ext.window.Window', {
            layout: 'center',
            title: '新建组',
			width: '75%',
        	height: '95%',
            items: [{
                xtype: 'createGroup',
				width: '100%',
				height: '100%',
                bodyPadding: '10 20',
                store: this.getView().getStore()
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        if (record.get('groupId') == 'everyone') {
            Ext.Msg.alert('提示信息', 'everyone组不能查看！');
            return;
        }
        Ext.create('Ext.window.Window', {
            layout: 'center',
            title: '修改组',
			width: '75%',
        	height: '95%',
            items: [{
                xtype: 'modifyGroup',
				width: '100%',
				height: '100%',
                bodyPadding: '10 20',
                record: record
            }]
        }).show();
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        var users = record.get('users');
        var usersObj = this.getView().down('itemselector');
        // usersObj.setValue(['AU9j1LcP4oXt9xabfnOL']);
        //Ext.Array.each(users, function (user, index, countriesItSelf) {
        //    var userIds = usersObj.setValue(user.userId);
        //});
        this.getView().getForm().loadRecord(record);
    },
    deleteGroup: function (e) {
        var me = this;
        var grid = e.up('grid');
        var record = this.getView().getSelectionModel().getSelection();
        if (record && record.length > 0) {
            var store = record[0].store;
            if (record[0].get('groupId') == 'adminGroup') {
                Ext.Msg.alert('提示信息', '管理员组账号不能删除！');
                return;
            }
            Ext.Msg.confirm("确认", "是否删除这个组？", function (r) {
                if(r==='yes'){
                    store.remove(record[0]);
                    me.sync(store);
                }
            });
        } else {
            Ext.Msg.alert('提示信息', '请至少选择一行');
        }
    },
    createSave: function (e) {
        var me = this;
        var form = this.getView().getForm();
        if (!form.isValid())return;
        var store = this.getView().store;
        var group = Ext.create('chemistry.model.Group', form.getValues());
        group.phantom = true;
        var view = this.getView();
        var window = view.up('window');
        var doc = Ext.getBody( );
        doc.mask('加载中...');
        view.mask('加载中...');
        group.save({
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
        var record = me.getView().record;
        var form = me.getView().getForm();
        if (!form.isValid())return;
        var group = form.getValues();
        var groupId = group.groupId;
        record.beginEdit();
        record.set({'groupName': group.groupName,'users': group.users,'childGroups': Ext.Array.remove( group.childGroups, groupId)});
        var store = me.getView().record.store;
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
    },

    sync: function (store) {
        var view = this.getView();
        var window = view.up('window');
        var doc = Ext.getBody( );
        doc.mask('加载中...');
        if (window)window.mask('加载中...');
        store.sync({
            callback: function (batch, options) {

                if(batch.exceptions&&batch.exceptions.length>0){
                    if (window)window.unmask();
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
