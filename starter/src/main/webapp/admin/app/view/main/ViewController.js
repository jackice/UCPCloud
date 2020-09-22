Ext.define('admin.view.main.ViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.view',
    loadAclOperationObj: function (acl) {
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
                        Ext.Array.each(me.getView().query('tagfield[name="permissionObj"]'), function(obj, index, countriesItSelf) {
                            obj.bindStore(
                                Ext.create('Ext.data.Store', {
                                    fields: ['id', 'name', 'isUser', 'isGroup'],
                                    data: data
                                }));
                        });
                        if(acl){
                            me.getView().query('tagfield[name="permissionObj"]')[0].setValue(acl);
                        }
                    }
                });
            }
        });
    },
    openCreateWin: function () {
        Ext.create('Ext.window.Window', {
            layout: 'center',
        	width: '75%',
       		height: '95%',
            title: '新建视图',
            items: [{
                xtype: 'createView',
				width: '100%',
				height: '100%',
                bodyPadding: '10 20',
				scrollable: true,
                store: this.getView().getStore()
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'center',
        	width: '75%',
       		height: '95%',
            title: '修改视图',
            items: [{
                xtype: 'modifyView',
				width: '100%',
				height: '100%',
                bodyPadding: '10 20',
				scrollable: true,
                record: record,
                store: this.getView().getStore()
            }]
        }).show();
        return;
    },
    loadModifyData: function (e, eOpts) {
        var me = this;
        var record = this.getView().record;
        var queryContext = Ext.JSON.decode(decodeURIComponent(record.get('queryContext')));
        var query = queryContext.query;
        var users = [];
        var groups = [];
        if(record.get('users')&&record.get('users')!=''){
            var users = record.get('users').split(',');
        }

        if(record.get('groups')&&record.get('groups')!=''){
            var groups = record.get('groups').split(',');
        }

        var pData = Ext.Array.merge(users, groups);
        this.loadAclOperationObj(pData);
        this.getView().getForm().loadRecord(record);
        this.getView().getForm().findField('type').setValue(queryContext.type);
        var isMulti = false;
        if (queryContext.type&&queryContext.type.length>1) {
            isMulti = true;
        }
        if(query.bool&&!Ext.Object.isEmpty(query.bool)){
            Ext.Object.each(query.bool, function(key, value, myself) {
                if(value){
                    if(Ext.isArray(value)){
                        Ext.Array.each(value, function(c, index, countriesItSelf) {
                            var conditionValue = {'query':key};
                            var k =  Ext.Object.getKeys(c)[0];
                            conditionValue.operator = k;
                            conditionValue.operatorValue = c[k];
                            me.organizationConditionData(conditionValue);
                            me.drawCondition(me.getView(), queryContext.type,isMulti,conditionValue);
                        });
                    }
                }else{
                    me.drawCondition(me.getView(), queryContext.type,isMulti);
                }
            });
            if(query.bool.minimum_should_match){
                me.getView().down('textfield[name=minimum_should_match]').setValue(query.bool.minimum_should_match);
            }

        }else{
            me.drawCondition(me.getView(), queryContext.type,isMulti);
        }
    },
    organizationConditionData : function(conditionValue){
        var operator = conditionValue.operator;
        var operatorValue = conditionValue.operatorValue;
        var key = '';
        var value = '';
        if(operatorValue){
            key =  Ext.Object.getKeys(operatorValue)[0];
            value = operatorValue[key];
        }
        if(operator=='range'){
            conditionValue.property = key;
            var rkeys =  Ext.Object.getKeys(value);
            var sOperator = rkeys[0];

            var hasFrom = Ext.Array.contains(['gt','gte'],sOperator);
            if(hasFrom){
                conditionValue.startOperator = rkeys[0];
                conditionValue.startValue = value[rkeys[0]];
            }else{
                conditionValue.endOperator = rkeys[0];
                conditionValue.endValue = value[rkeys[0]];
            }
            if(rkeys.length>1){
                var eOperator = rkeys[1];
                var hasEnd = Ext.Array.contains(['lt','lte'],eOperator);
                if(hasEnd){
                    conditionValue.endOperator = rkeys[1];
                    conditionValue.endValue =value[rkeys[1]];
                }
            }

        }else{
            conditionValue.property = key;
            conditionValue.value = value;
        }
    },
    loadConditionField: function (conditionObj, type,isMulti, index,conditionValue) {
        var me = this;

        var data = [{
            name: "name",
            displayName: "名称",
            type: "string"
        },{
            name: "tag",
            displayName: "标签",
            type: "string"
        },{
            name: "createdBy",
            displayName: "创建人",
            type: "string"
        },{
            name: "lastUpdatedBy",
            displayName: "修改人",
            type: "string"
        },{
            name: "createdOn",
            displayName: "创建时间",
            type: "date"
        },{
            name: "lastUpdatedOn",
            displayName: "修改时间",
            type: "date"
        }];
        if(isMulti){
            var pObj = conditionObj.down('combobox[name="property"]');
            if(pObj){
                pObj.bindStore(
                    Ext.create('Ext.data.Store', {
                        model: Ext.create('chemistry.model.Property'),
                        data: data
                    })
                );
                if(conditionValue&&conditionValue.property){
                    pObj.setValue(conditionValue.property);
                    conditionObj.down('combobox[name="operator"]').setValue(conditionValue.operator);
                    me.loadQueryValues(conditionValue,conditionObj);
                }
            }
        }else{
            Ext.Ajax.request({
                url: '/svc/types/' + type,
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if (response.responseText != '') {
                        var properties = Ext.decode(response.responseText);
                        if(properties.properties){
                            var newProperties  = Ext.Array.map(properties.properties,function(item,index,array){
                                var obj = item;
                                obj.displayName= item.name;
                                return obj;
                            });
                        }
                        data = Ext.Array.insert(newProperties?newProperties:[], 0, data);
                        var pObj = conditionObj.down('combobox[name="property"]');
                        if(pObj){
                            pObj.bindStore(
                                Ext.create('Ext.data.Store', {
                                    model: Ext.create('chemistry.model.Property'),
                                    data: data
                                })
                            );
                            if(conditionValue&&conditionValue.property){
                                pObj.setValue(conditionValue.property);
                                conditionObj.down('combobox[name="operator"]').setValue(conditionValue.operator);
                                me.loadQueryValues(conditionValue,conditionObj);
                            }
                        }
                    }
                }
            });
        }

    },
    refreshCondition:function(condition,n){
        var items = condition.items.items;
        var cLength = condition.items.length;
        var removeList = [];
        for (var i = n; i <= cLength; i++) {
            var obj = items[i];
            if (obj) {
                if (!obj.isXType('container')) {
                    removeList.push(obj);
                }
            }
        }
        Ext.Array.each(removeList, function (r, index, countriesItSelf) {
            condition.remove(r);
        });
    },
    drawOperatorField : function(type,condition){
        var data = [  { name: "range", value: "range" },
            { name: "term", value: "term" },
            { name: "fuzzy", value: "fuzzy"},
            { name: "wildcard", value: "wildcard" }];
        this.refreshCondition(condition,2);
        if(type=='string'){
            data.splice(0, 1);
        }
        if(type=='integer'||type=='float'){
            data.splice(2, 1);
            data.splice(2, 1);
        }
        if(type=='boolean'){
            data = [ { name: "term", value: "term" }];
        }
        if(type=='date'){
            data.splice(1, 1);
            data.splice(1, 1);
            data.splice(1, 1);
        }
        condition.insert(2,[{
                xtype: 'combobox',
                name : 'operator',
                displayField: 'name',
                valueField: 'value',
                maxWidth : 100,
                store : Ext.create('Ext.data.Store', {
                    fields: ["name", "value"],
                    data: data
                }),
                listeners: {
                    change:"changeOperator"
                }
            },{
                name: 'value'}]
        );

    },
    changeProperty :  function (e, newValue, oldValue, eOpts) {
        var record = e.getSelection();
        var condition = e.up('searchcondition');
        var type = record.get('type');


        if (newValue && newValue != oldValue) {
            this.drawOperatorField(type,condition);
        }
    },
    changeType: function (e, newValue, oldValue, eOpts) {
        var isMulti = false;
        if (newValue && newValue != oldValue) {
            if(newValue.length>1){
                isMulti = true;
            }
            var advPanel = this.getView();
            if (oldValue && oldValue != '') {
                this.initCondition();
                this.drawCondition(advPanel, newValue,isMulti);
            }
            this.loadConditionField(advPanel, newValue,isMulti, 0);
        }
    },
    initCondition: function () {
        var advPanel = this.getView();
        var fieldset = advPanel.child('fieldset');
        fieldset.removeAll();
    },
    drawOperatorDiv : function(type,newValue,condition){
        if(type=='boolean'){
            if (newValue == 'term') {
                condition.insert(3, {
                    xtype: 'combobox',
                    name: 'value',
                    editable : false,
                    minWidth: 100,
                    store: [true, false]
                });
            }
        }
        if(type=='string'){
            condition.insert(3, {
                name: 'value'
            });
        }
        if(type=='integer'||type=='float'){
            var validator ;
            if(type=='integer'){
                validator = function (val) {
                    var strP =  /^-?\d+$/;
                    if (val&&!strP.test(val)) return "该输入项必须是整型";
                    return true;
                }
            }
            if(type=='float'){
                validator = function (val) {
                    var strP =  /^-?\d+(\.\d+)?$/;
                    if (val&&!strP.test(val)) return "该输入项必须是浮点型";
                    return true;
                }
            }
            if (newValue == 'term') {
                condition.insert(3, {
                    name: 'value',
                    validator : validator
                });
            }
            if (newValue == 'range') {
                condition.insert(3, this.drawRangeCondition(validator));
            }
        }
        if(type=='date'){
            var validator = function (val) {
                if(val){
                    var date = Ext.Date.parse(val, "c")
                    if(date==null){
                        return "要求日期格式,如2001-01-01T23:59:59";
                    }

                }
                return true;
            }
            if (newValue == 'term') {
                condition.insert(3, {
                    name: 'value',
                    validator : validator
                });
            }
            if (newValue == 'range') {
                condition.insert(3, this.drawRangeCondition(validator));
            }
        }
    },
    changeOperator: function (e, newValue, oldValue, eOpts) {
        var condition = this.getView();
        var property = condition.down('combobox[name=property]');
        var type = property.getSelection().get('type');
        if (newValue != '' && newValue != oldValue) {
            this.refreshCondition(condition,3);
            this.drawOperatorDiv(type,newValue,condition);

        }

    },
    drawRangeCondition : function(validator){
        return [{
            xtype: 'combobox',
            name: 'startOperator',
            displayField: 'name',
            maxWidth: 100,
            valueField: 'value',
            store: Ext.create("Ext.data.Store", {
                fields: ["name", "value"],
                data: [
                    {name: ">", value: "gt"},
                    {name: ">=", value: "gte"}
                ]
            })
        },
            {
                name: 'startValue',
                validator : validator
            }, {
                xtype: 'combobox',
                name: 'endOperator',
                displayField: 'name',
                maxWidth: 70,
                valueField: 'value',
                store: Ext.create("Ext.data.Store", {
                    fields: ["name", "value"],
                    data: [
                        {name: "<", value: "lt"},
                        {name: "<=", value: "lte"}
                    ]
                })
            },
            {
                name: 'endValue',
                validator : validator
            }];
    },
    drawCondition: function (aPanel, type,isMulti,conditionValue) {

        var items = aPanel.child('fieldset');
        var index = items.items.length;
        var sc =  items.add(Ext.create('admin.view.main.view.SearchCondition'));
        this.loadConditionField(sc, type,isMulti, index,conditionValue);
        if(conditionValue&&conditionValue.query){
            sc.down('combobox[name=query]').setValue(conditionValue.query);
        }
    },
    loadQueryValues : function(conditionValue,obj){
        var operator = conditionValue.operator;
        if(operator=='range'){
            obj.down('combobox[name=startOperator]').setValue(conditionValue.startOperator);
            obj.down('textfield[name=startValue]').setValue(conditionValue.startValue);
            obj.down('textfield[name=endValue]').setValue(conditionValue.endValue);
            obj.down('combobox[name=endOperator]').setValue(conditionValue.endOperator);

        }else{
            obj.down('textfield[name=value]').setValue(conditionValue.value);
        }
    },
    checkQueryCondition: function(obj){
        var flag = true;
        var query =  obj.child('combobox[name=query]');
        var property =  obj.child('combobox[name=property]');
        var operator =  obj.child('combobox[name=operator]');
        if(query&&query.getValue()==null){
            query.setActiveError( '该输入项为必输项' );
            flag = false;
        }
        if(property&&property.getValue()==null){
            property.setActiveError( '该输入项为必输项' );
            flag = false;
        }
        if(operator&&operator.getValue()==null){
            operator.setActiveError( '该输入项为必输项' );
            flag = false;
        }
        return flag;
    },
    onAddButton: function (e) {
        var aPanel = this.getView().up('form');
        var type = aPanel.child('tagfield[name=type]').getValue();
        var type = aPanel.child('tagfield').getValue();
        var isMulti = false;
        if(this.checkQueryCondition(e.up('searchcondition'))) {
            if (type && type != '') {
                if (type.length > 1)
                    isMulti = true;
                this.drawCondition(aPanel, type, isMulti);
            }
        }
    },
    onDeleteButton: function (e) {
        var condtion = this.getView();
        var conditionContainer = this.getView().up('fieldset');
        if(conditionContainer.query('searchcondition').length>1){
            conditionContainer.remove(condtion);
        }
    },
    save : function(bt,e) {
        var me = this;
        var form = this.getView().getForm();
        var store = this.getView().store;
        var flag = true;
        var conditions = this.getView().query('searchcondition');
        if (form.isValid()) {
            var qmust = [];
            var qmust_not = [];
            var qshould = [];
            var type = form.findField('type').getValue();
            var viewObj = form.getValues();
            Ext.Array.each(conditions, function (condition, index, countriesItSelf) {
                var queryItem = {};
                var query = condition.child('combobox[name="query"]').getValue();
                var property = condition.child('combobox[name="property"]').getValue();
                var operator = condition.child('combobox[name="operator"]').getValue();
                if(query===null&&operator===null&&property===null){
                    return ;
                }
                if(!me.checkQueryCondition(condition)){
                    flag = false;
                    return ;
                }

                if (operator == 'range') {
                    var startValue = condition.child('textfield[name="startValue"]').getValue();
                    var endValue = condition.child('textfield[name="endValue"]').getValue();
                    var startOperator = condition.child('combobox[name="startOperator"]').getValue();
                    var endOperator = condition.child('combobox[name="endOperator"]').getValue();
                    if(startValue!=''||endValue!='') {
                        queryItem.range = {};
                    queryItem.range[property] = {};
                    if (startValue && startValue != '') {
                        if (!startOperator) {
                            startOperator = 'gt';
                        }
                        queryItem.range[property][startOperator] = startValue;
                    }
                    if (endValue && endValue != '') {
                        if (!endOperator) {
                            endOperator = 'lt';
                        }
                        queryItem.range[property][endOperator] = endValue;
                    }
                    }
                }else{
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    queryItem[operator] = {};
                    queryItem[operator][property] = pValue;
                }

                if(queryItem&&!Ext.Object.isEmpty(queryItem)){
                    if (query == 'must') {
                        qmust.push(queryItem);
                    }
                    if (query == 'must_not') {
                        qmust_not.push(queryItem);
                    }
                    if (query == 'should') {
                        qshould.push(queryItem);
                    }
                }
            });
            if(!flag){
                return ;
            }
            var query = {"bool": {}};
            if (qmust.length > 0) {
                query.bool.must = qmust;
            }
            if (qmust_not.length > 0) {
                query.bool.must_not = qmust_not;
            }
            if (qshould.length > 0) {
                query.bool.should = qshould;
            }
            if (qshould && qshould.length > 0) {
                query.bool.minimum_should_match = form.findField('minimum_should_match').getValue();
            }
            var queryContext = {'query':query,'type':type}
            viewObj.queryContext = encodeURIComponent(Ext.JSON.encode(queryContext));
            var permissionObj = form.findField('permissionObj').getValueRecords();
            viewObj.users = [];
            viewObj.groups = [];
            Ext.Array.each(permissionObj, function (permiss, index, countriesItSelf) {
                if (permiss.get('isUser')) {
                    viewObj.users.push(permiss.get('id'));
                }
                if (permiss.get('isGroup')) {
                    viewObj.groups.push(permiss.get('id'));
                }

            });
            var url = '';
            var msg = "";
            var method = "";
            if( this.getView().down('hiddenfield[name=_id]')&&this.getView().down('hiddenfield[name=_id]').getValue()!=''){
                url = '/svc/views/'+this.getView().down('hiddenfield[name=_id]').getValue();
                method = 'PATCH';
            }else{
                url = '/svc/views';
                method = 'POST';
            }
        var view = this.getView();
        var window = view.up('window');
        var doc = Ext.getBody();
        doc.mask('加载中...');
        view.mask('加载中...');
            Ext.Ajax.request({
                method: method,
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: url,
                params: Ext.JSON.encode(viewObj),
                callback: function (options, success, response) {
                    if (!success) {
                        var error =  Ext.decode(response.responseText);
                        view.unmask();
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
                        view.unmask();
                        doc.unmask();
                        window.close();
                        Ext.toast({
                            html: '操作成功',
                            title: '提示信息',
                            width: 200,
                            align: 't'
                        });
                        store.load();
                    },2000);
                }
            });
        }
    }, refreshView : function(){
        var me = this;
        var store = this.getViewModel().getStore('views');
        store.load();
        return;
    },
    deleteView: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }

        Ext.Msg.confirm("确认", "是否删除该视图 ?", function (r) {
            if(r==='yes'){
                var store = record[0].store;
                store.remove(record[0]);
            }
        });
    }

});
