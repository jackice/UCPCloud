Ext.define('admin.view.main.TypeController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.type',

    openCreateWin: function (sender, record) {
        var me = this;
        var store = this.getView().store;
        Ext.create('admin.view.main.type.Create',{store:store,isInstance:false}).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        var me = this;
        var isInstance = false;
        Ext.Ajax.request({
            url: '/svc/' + record.get('name')+'/_count',
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                var responseText = Ext.decode(response.responseText);
                if(responseText&&responseText.count>0){
                    isInstance = true;
                }
                Ext.create('admin.view.main.type.Modify', {record: record,isInstance:isInstance}).show();
            }
        });

        return;
    },
    refreshType: function (sender, record) {
        var me = this;
        var store = this.getViewModel().getStore('types');
        store.load();
        return;
    },
    loadModifyData: function () {
        var me = this;
        var record = this.getView().record;
        this.getView().down('form').loadRecord(record);
        var type = record.get('name');
        Ext.Ajax.request({
            url: '/svc/types/' + type,
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if (response.responseText != '') {
                    var properties = Ext.decode(response.responseText);
                    var store = Ext.create('Ext.data.Store', {
                        model: 'chemistry.model.Property',
                        data: properties.properties,
                        initData: properties.properties
                    });
                    me.getView().down('grid').bindStore(store);

                }
            }
        });
    },
    deleteType: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        if (record && record.length > 0) {
            var type = record[0];
            var name = type.get('name');
            Ext.Ajax.request({
                url: '/svc/' + name+'/_count',
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    var responseText = Ext.decode(response.responseText);
                    if(responseText&&responseText.count>0){
                        Ext.Msg.alert('提示信息', '该文档类型已经有实例，不能删除！');
                    }else{
                        Ext.Msg.confirm("确认", "是否删除该类型？", function (r) {
                            if(r==='yes'){
                                me.getViewModel().getStore('types').remove(type);
                            }
                        });
                    }

                }
            });

        } else {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
    },
    addProperty: function (e) {
        var store = e.up('grid').store;
        var order = store.data.length + 1;
        // Create a model instance
            var r = Ext.create('chemistry.model.Property', {
                name: '',
                type: 'string',
                pattern: '',
                promptMessage: '',
                index: 'not_analyzed',
                defaultValue: '',
                analyzer: '',
                required: false,
                order: order
            });
        var win = Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建属性',
            items: [{
                xtype: 'createProperty',
                fData : r,
                pStore : store
            }]
        }).show();
        if(this.getView().isInstance!=undefined&&this.getView().isInstance){
           var required =  win.down('createProperty').getForm().findField('required');
            required.setValue(false).setReadOnly( true );
        }
    },
    deleteProperty: function (e) {
        var grid = e.up('grid');
        var store = e.up('grid').store;
        var sm = grid.getSelectionModel();
        var record = grid.getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
        store.remove(record);
    },
    downloadType: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();

        if (record && record.length > 0) {
            var name = record[0].get('name');
            var url = '/svc/types/'+name+'?accept=application/octet-stream';
            Ext.Ajax.download(url);
        } else {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
    },
    uploadType: function (e) {
        var me = this;
        var store = this.getView()    .store;
            Ext.create('Ext.window.Window', {
                layout: 'fit',
                width : 400,
                height : 180,
                title: '上传类型',
                items: [{
                    xtype: 'form',
                    layout: 'anchor',
                    bodyPadding: 10,
                    defaults: {
                        anchor: '100%'
                    },
                    items : [{
                        xtype: 'filefield',
                        name : 'typeFile',
                        hideLabel: true,
                        buttonText: '选择类型',
                        allowBlank: false
                    }],
                    buttons: [{
                        text: '关闭',
                        handler: function(e) {
                            e.up('window').close();
                        }
                    }, {
                        text: '上传',
                        formBind: true, //only enabled once the form is valid
                        disabled: true,
                        handler: function(e){
                            var form = e.up('form').getForm();;
                            if (!form.isValid()) return false;
                            var window = e.up('window');
                            window.mask('loading...');
                            form.submit({
                                url: '/svc/types/import',
                                waitMsg: '上传中 ...',
                                success: function (form, action) {
                                    setTimeout(function(){
                                        window.unmask();
                                        window.close();
                                        Ext.toast({
                                            html: "上传成功",
                                            title: '提示信息',
                                            width: 200,
                                            align: 't'
                                        });
                                        store.load();
                                    },2000);
                                },
                                failure: function (form, action) {
                                    var error = Ext.decode(action.response.responseText);
                                    window.unmask();
                                    Ext.toast({
                                        html: '上传失败!<br />'+error.reason,
                                        title: '提示信息',
                                        width: 200,
                                        align: 't'
                                    });
                                }
                            });
                        }
                    }]
                }]
            }).show();



    },
    deleteModifyProperty: function (e) {
        var grid = e.up('grid');
        var store = e.up('grid').store;

        var record = grid.getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
            var isOld = Ext.Array.contains(store.initData?store.initData:[], record[0].data);
            if (isOld) {
                Ext.Msg.alert('提示信息', '您不能删除这个属性');
                return;
            } else
                store.remove(record);
    },

    createSave: function (e) {
        var me = this;
        var form = e.up('window').down('form');
        var grid = e.up('window').down('grid');
        var store = e.up('window').store;
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if (record && !record.isValid() && record.get('name') != '') {
                return;
            }
            var properties = [];
            var size = gstore.getCount();
            for (var i = 0; i < size; i++) {
                var pRecord = gstore.getAt(i);
                properties.push(pRecord.getData());
            }
            properties = Ext.Array.sort(properties, function (a, b) {
                if (a.order < b.order)
                    return -1;
                if (a.order > b.order)
                    return 1;
                if (a.order == b.order)
                    return 0;
            });
            type.properties = properties;
            var window = e.up('window');
            var doc = Ext.getBody( );
            doc.mask('loading...');
            window.mask('loading...');
            Ext.Ajax.request({
                method: 'POST',
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: '/svc/types',
                params: Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    me.callbackHandler(success, response,window,store);

                }
            });
        }

    },
    changeType : function( e, newValue, oldValue, eOpts ){
        var form = e.up('createProperty');
        var defaultValue = form.getComponent('defaultValue');
        if (newValue == 'boolean') {
            form.remove(defaultValue);
            form.insert(2,{
                xtype: 'combobox',
                itemId : 'defaultValue',
                name: 'defaultValue',
                fieldLabel: '默认值',
                editable : false,
                minWidth: 100,
                store: [true, false]
            });
        }else if(newValue == 'date'){
            form.remove(defaultValue);
            form.insert(2,{
                xtype: 'datefield',
                itemId : 'defaultValue',
                name: 'defaultValue',
                fieldLabel: '默认值',
                anchor: '100%',
                altFormats: 'c',
                format: 'Y-m-d'
            });
        }else{
           var  field = {
               itemId : 'defaultValue',
               name: 'defaultValue',
               fieldLabel: '默认值'
            };
            if (newValue == 'integer') {
                field.validator = function (val) {
                    if(!val) return true;
                    var strP =  /^-?\d+$/;
                    if (!strP.test(val)) return "该输入项必须是整型";
                    if(val > 2147483647||val < -2147483647) return "该输入项必须是整型";
                    return true;
                }
            }
            if (newValue == 'float') {
                field.validator = function (val) {
                    if(!val) return true;
                    var strP =  /^-?\d+(\.\d+)?$/;
                    if (!strP.test(val)){
                        return "该输入项必须是浮点型";
                    }
                    if(val.length>24){
                        return "该输入项必须是浮点型";
                    }

                    return true;
                }
            }
            form.remove(defaultValue);
            form.insert(2,field);
        }
    },
    callbackHandler : function(success,response,window,store){
        var doc = Ext.getBody( );
        if (!success) {
            var error =  Ext.decode(response.responseText);
            window.unmask();
            doc.unmask();
            Ext.toast({
                html:  '操作失败!<br />'+error.reason,
                title: '提示信息',
                width: 200,
                align: 't'
            });
            return;
        }
        setTimeout(function(){
            window.unmask();
            doc.unmask();
            window.close();
            Ext.toast({
                html: "操作成功",
                title: '提示信息',
                width: 200,
                align: 't'
            });
            store.load();
        },2000);
    },
    modifySave: function (e) {
        var me = this;
        var form = e.up('window').down('form');
        var grid = e.up('window').down('grid');
        var store = this.getView().record.store;
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if (record && !record.isValid() && record.get('name') != '') {
                return;
            }
            var properties = []
            var size = gstore.getCount();
            for (var i = 0; i < size; i++) {
                var pRecord = gstore.getAt(i);
                properties.push(pRecord.getData());
            }
            properties = Ext.Array.sort(properties, function (a, b) {
                if (a.order < b.order)
                    return -1;
                if (a.order > b.order)
                    return 1;
                if (a.order == b.order)
                    return 0;
            });
            type.properties = properties;
            //type.set('properties',properties);
            var window = e.up('window');
            var doc = Ext.getBody( );
            doc.mask('加载中...');
            window.mask('加载中...');
            Ext.Ajax.request({
                method: 'PUT',
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: '/svc/types/' + type.name,
                params: Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    me.callbackHandler(success, response,window,store);
                }
            });
        }

    },
    loadProperty : function(btn,e){
        var from = this.getView();
        var record = from.fData;
        var isUpdate = from.isUpdate;
        from.loadRecord( record );
        if(isUpdate){
            from.getForm().findField('name').disabled = true;
            //from.getForm().findField('type').disabled = true;
            //from.getForm().findField('index').disabled = true;
            //from.getForm().findField('indexAnalyzer').disabled = true;
            //from.getForm().findField('searchAnalyzer').disabled = true;
        }
    },
    saveProperty : function(e){
        var form = this.getView().getForm();
        if (form.isValid()) {
           var store =  this.getView().pStore;
            var record = this.getView().fData;
            var name = form.findField('name').getValue();
            var type = form.findField('type').getValue();
            var defaultValue = form.findField('defaultValue').getRawValue();
            var pattern = form.findField('pattern').getValue();
            var promptMessage = form.findField('promptMessage').getValue();
            var re = new RegExp(pattern);
            if (defaultValue&&defaultValue!=''&&!re.test(defaultValue)){
                if(promptMessage&&promptMessage!=''){
                    form.findField('defaultValue').setActiveError( promptMessage );
                }else{
                    form.findField('defaultValue').setActiveError("您输入的值与约束条件不符");
                }
                return false;
            }else{
                var n = store.findBy(function(record ,id){
                    if(record.get('name')==name)
                        return true;
                });

                if(n>-1){
                    if(this.getView().isUpdate&&record.get('name')==name){
                        record.set(form.getValues());
                        this.getView().up('window').close();
                    }else{
                        Ext.Msg.alert('提示信息', '属性已存在！');
                    }
                }else if(this.getView().isUpdate){
                    record.set(form.getValues());
                    record.set('name',name);
                    this.getView().up('window').close();
                }
                else{
                    store.insert(0, form.getValues());
                    this.getView().up('window').close();
                }

            }

        }
    },
    modifyProperty : function(c, record, item, index, e, eOpts){
        var store = c.up('grid').store;
        var isOld = Ext.Array.contains(store.initData?store.initData:[], record.data);
        if(isOld) return;
        var order = store.data.length + 1;
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '修改属性',
            items: [{
                xtype: 'createProperty',
                fData : record,
                pStore : store,
                isUpdate : true
            }]
        }).show();
    }
});
