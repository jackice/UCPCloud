Ext.define('explorer.view.main.IndexDocController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.indexdoc',
    changeTypeForIndex: function (combo, newValue, oldValue, eOpts) {
        var me = this;
        if (newValue && newValue != oldValue) {

            var type = newValue;
            me.drawPeopertyByType(type);
        }

    },
    drawPeopertyByType: function (type, flag, callback) {
        var me = this;
        var form = this.getView();
        var isUpdate = false;
        if(form.docData){
            isUpdate = true;
        }
        var fieldset = form.down('fieldset[itemId="propertyList"]');
        fieldset.removeAll(true);
        Ext.Ajax.request({
            url: '/svc/types/' + type,
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if (response.responseText != '') {
                    var properties = Ext.decode(response.responseText);
                    var sortedProperties = Ext.Array.sort(properties.properties?properties.properties:[], function (a, b) {
                        if (a.order > b.order) return 1;
                        if (a.order < b.order) return -1;
                        if (a.order === b.order) return 0;
                    });
                    Ext.Array.each(sortedProperties, function (property, index, countriesItSelf) {
                        var field = me.drawPeopertyField(property,isUpdate);
                        fieldset.add(field);
                    });
                    if (flag != undefined) {
                        Ext.Array.each(fieldset.items.items, function (field, index, countriesItSelf) {
                            field.setReadOnly(!flag);
                        });
                    }
                    if (callback) {
                        callback();
                    }
                }
            }
        });
    },
    drawPeopertyField: function (property,isUpdate) {
        var type = property.type;
        var field = {};

        var promptMessage = property.promptMessage?property.promptMessage:'您输入的值与约束条件不符';
        if (type == 'boolean') {
            field = {
                xtype: 'combobox',
                name: property.name,
                fieldLabel: property.name,
                editable : false,
                minWidth: 100,
                store: [true, false],
                regex: property.pattern,
                regexText:promptMessage
            }
        }else if(type == 'date'){
            field = {
                xtype: 'datefield',
                fieldLabel: property.name,
                anchor: '100%',
                altFormats: 'c',
                format: 'Y-m-d',
                name: property.name
            };
            field.validator = function (val) {
                if(!val) return true;
                if (property.pattern) {
                    var re = new RegExp(property.pattern);
                    if( !re.test(val)){
                        return promptMessage;
                    }
                }
                return true;
            }

        }else{
            field = {
                fieldLabel: property.name,
                name: property.name
            };
            if (type == 'string') {
                field.validator = function (val) {
                    if(!val) return true;
                    if (property.pattern) {
                        var re = new RegExp(property.pattern);
                        if( !re.test(val)){
                            return promptMessage;
                        }
                    }
                    return true;
                }
            }
            if (type == 'integer') {
                field.validator = function (val) {
                    if(!val) return true;
                    var strP =  /^-?\d+$/;
                    if (!strP.test(val)) return "该输入项必须是整型";
                    if(val > 2147483647||val < -2147483647) return "该输入项必须是整型";
                    if (property.pattern) {
                        var re = new RegExp(property.pattern);
                        if( !re.test(val)){
                            return promptMessage;
                        }
                    }
                    return true;
                }
            }
            if (type == 'float') {
                field.validator = function (val) {
                    if(!val) return true;
                    var strP =  /^-?\d+(\.\d+)?$/;
                    if (!strP.test(val)){
                        return "该输入项必须是浮点型";
                    }
                    if(val.length>24){
                        return "该输入项必须是浮点型";
                    }
                    if (val&&property.pattern) {
                        var re = new RegExp(property.pattern);
                        if( !re.test(val)){
                            return promptMessage;
                        }
                    }
                    return true;
                }
            }
        }
        if(!isUpdate){
            if(type == 'date'){
                if (property.defaultValue != '') {
                    field.value = new Date(property.defaultValue);
                }
            }else
                field.value= property.defaultValue;
        }
        if (property.required) {
            field.allowBlank = false;
        }
        return field;
    },
    save: function () {
        var me = this;
        var form = this.getView().getForm();
        if (form.isValid()) {
            var data = this.getView().docData;
            var store = me.getView().docStore;
            var typeObj = this.getView().down('combo[name=type]');
            var name = this.getView().down('textfield[itemId=documentName]').getValue();
            var aclcontainer = this.getView().down('container[itemId=aclList]');
            var aclItems = aclcontainer.items;
            if (aclItems) {
                var flag = false;
                var _acl = {"read": {"users": [], "groups": []}, "write": {"users": [], "groups": []}};
                var readOperationObj = aclItems.items[0].child('tagfield[name="operationObj"]').getValueRecords();
                var writeOperationObj = aclItems.items[1].child('tagfield[name="operationObj"]').getValueRecords();

                Ext.Array.each(Ext.isArray(readOperationObj)?readOperationObj:[],  function (operationObj, index, countriesItSelf) {
                    if (operationObj.get('isUser')) {
                       _acl.read.users.push(operationObj.get('id'));
                    }
                });
                Ext.Array.each(Ext.isArray(readOperationObj)?readOperationObj:[],  function (operationObj, index, countriesItSelf) {
                    if (operationObj.get('isGroup')) {
                        _acl.read.groups.push(operationObj.get('id'));
                    }
                });
                Ext.Array.each(Ext.isArray(writeOperationObj)?writeOperationObj:[],  function (operationObj, index, countriesItSelf) {
                    if (operationObj.get('isUser')) {
                         _acl.write.users.push(operationObj.get('id'));
                    }
                });
                Ext.Array.each(Ext.isArray(writeOperationObj)?writeOperationObj:[],  function (operationObj, index, countriesItSelf) {
                    if (operationObj.get('isGroup')) {
                        _acl.write.groups.push(operationObj.get('id'));
                    }
                });
                if (writeOperationObj.length>0||readOperationObj.length>0) {
                    this.getView().down('hiddenfield[name=_acl]').setValue(Ext.encode(_acl));
                } else {
                    this.getView().down('hiddenfield[name=_acl]').setValue('');
                }
            }
            var url = '';
            var msg = "";
            if (this.getView().down('hiddenfield[name=_id]') && this.getView().down('hiddenfield[name=_id]').getValue() != '') {
                var _allowableActions = data.get("_allowableActions");
                if (!Ext.Array.contains(_allowableActions, "write")) {
                    Ext.Msg.alert('提示信息', '没有操作权限.');
                    return;
                }
                url = '/svc/' + this.getView().down('hiddenfield[name=type]').getValue() + '/' + this.getView().down('hiddenfield[name=_id]').getValue();
            } else {
                //this.getView().down('hiddenfield[name=$typeName]').setValue(typeObj.getSelectedRecord().get('displayName'));
                url = '/svc/' + typeObj.getValue();
            }
            form.submit({
                url: url,
                waitMsg: '加载中 ...',
                success: function (form, action) {
                    me.getView().up('window').close();
                    if(store){
                        store.load();
                    }
                    Ext.toast({
                        html: '操作成功',
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });

                },
                failure: function (form, action) {
                    var error = Ext.decode(action.response.responseText);
                    Ext.toast({
                        html: '操作失败!<br />' + error.reason,
                        title: '提示信息',
                        width: 200,
                        align: 't'
                    });
                }
            });

        }
    },
    loadAclOperationObj: function (acl, flag) {
        var me = this;
        var userResult = [];
        var groupResult = [];
        var data = [];
        Ext.Ajax.request({
            url: '/svc/users?limit=100000',
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if (response.responseText != '') {
                    var users = Ext.decode(response.responseText);
                    userResult = Ext.Array.map(users.users, function (item, index) {
                        return {'id': item.userId, 'name': item.userName, 'isUser': true, 'isGroup': false};
                    });
                }
                Ext.Ajax.request({
                    url: '/svc/groups?limit=100000',
                    callback: function (options, success, response) {
                        if (!success) {
                            return;
                        }
                        if (response.responseText != '') {
                            var groups = Ext.decode(response.responseText);
                            groupResult = Ext.Array.map(groups.groups, function (item, index) {
                                return {'id': item.groupId, 'name': item.groupName, 'isUser': false, 'isGroup': true};
                            });
                        }
                        data = Ext.Array.merge(userResult, groupResult);
                        Ext.Array.each(me.getView().query('tagfield[name="operationObj"]'), function (obj, index, countriesItSelf) {
                            obj.bindStore(
                                Ext.create('Ext.data.Store', {
                                    fields: ['id', 'name', 'isUser', 'isGroup'],
                                    data: data
                                }));
                        });
                        if (acl && acl.read) {
                            me.getView().query('tagfield[name="operationObj"]')[0].setValue(acl.read);
                            me.getView().query('tagfield[name="operationObj"]')[0].setReadOnly(!flag);
                        }
                        if (acl && acl.write) {
                            me.getView().query('tagfield[name="operationObj"]')[1].setValue(acl.write);
                            me.getView().query('tagfield[name="operationObj"]')[1].setReadOnly(!flag);
                        }

                    }
                });
            }
        });
    },
    loadData: function () {
        var data = this.getView().docData;
        var form = this.getView().getForm();
        var _allowableActions = data.get('_allowableActions');
        var flag = Ext.Array.contains(_allowableActions, "write");
        if (!flag) {
            form.findField('name').setReadOnly(true);
            form.findField('tag').setReadOnly(true);
            this.getView().down('button[itemId=updateBtn]').hide();
        }
        var type = data.get('_type');
        form.findField('typeName').setValue(type.displayName);
        form.findField('type').setValue(type.name);
        form.findField('tag').setValue(data.get('tag'));
        var streams = data.get('_streams');
        var _acl = data.get('_acl');
        var acl = {read: [], write: []};
        Ext.Object.each(_acl, function (key, value, myself) {
            if (key === 'read') {
                if (value) {
                    Ext.Object.each(value, function (k, val, myself) {
                        if (val && val.length > 0) {
                            Ext.Array.each(val, function (name, index, countriesItSelf) {
                                acl.read.push(name);
                            });
                        }
                    });
                }
            }
            if (key === 'write') {
                if (value) {
                    Ext.Object.each(value, function (k, val, myself) {
                        if (val && val.length > 0) {
                            Ext.Array.each(val, function (name, index, countriesItSelf) {
                                acl.write.push(name);
                            });
                        }
                    });
                }
            }
        });
        this.loadAclOperationObj(acl, flag);
        this.drawStreamTable(streams, flag);
        this.drawPeopertyByType(type.name, flag, function () {
            form.loadRecord(data);
        });

    },
    closeWin: function () {
        this.getView().up('window').close();
    },
    drawStreamTable: function (streams, flag) {
        var me = this;
        var streamcontainer = this.getView().down('fieldset[itemId=stream]');
        var removeStreamIds = this.getView().down('hiddenfield[itemId=removeStreamIds]');
        this.getView().down('multifile').setDisabled(!flag);
        Ext.Array.each(streams, function (stream, index, countriesItSelf) {
            if (stream && stream.streamName != '') {
                streamcontainer.insert(index, {
                    xtype: 'container',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'label',
                            text: stream.streamName,
                            width : 400,
                            margin: '0 0 0 10'
                        },
                        {
                            xtype: 'button',
                            text: '-',
                            fieldReference: 'fieldInterval',
                            style: {
                                'margin-left': '10px'
                            },
                            streamId: stream.streamId,
                            listeners: {
                                click: function (bt, e) {
                                    if (flag) {
                                        var stream = bt.up('container');
                                        var streamId = bt.streamId;
                                        var vals = [];
                                        if (removeStreamIds.getValue() && removeStreamIds.getValue().length > 0) {
                                            vals = removeStreamIds.getValue().split(',');
                                        }
                                        vals.push(streamId);
                                        removeStreamIds.setValue(vals);
                                        streamcontainer.remove(stream);
                                    }
                                }
                            }
                        }
                    ]
                });
            }
        });
    }
});
