Ext.define('explorer.view.main.AdvSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.advsearch',
    loadConditionField: function (aPanel, type,isMulti, index) {
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
            aPanel.query('combobox[name="property"]')[index].bindStore(
                Ext.create('Ext.data.Store', {
                    model: Ext.create('chemistry.model.Property'),
                    data: data
                })
            );
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
                        aPanel.query('combobox[name="property"]')[index].bindStore(
                            Ext.create('Ext.data.Store', {
                                model: Ext.create('chemistry.model.Property'),
                                data: data
                            })
                        );
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
    changeProperty :  function (e, newValue, oldValue, eOpts) {
        var record = e.getSelection();
        var condition = e.up('searchcondition');
        var type = record.get('type');

        var data = [  { name: "range", value: "range" },
            { name: "term", value: "term" },
            { name: "fuzzy", value: "fuzzy"},
            { name: "wildcard", value: "wildcard" }];
        if (newValue && newValue != oldValue) {
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
    changeOperator: function (e, newValue, oldValue, eOpts) {
        var condition = this.getView();
        var property = condition.down('combobox[name=property]');
        var type = property.getSelection().get('type');
        if (newValue != '' && newValue != oldValue) {
            this.refreshCondition(condition,3);
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
    drawCondition: function (aPanel, type,isMulti) {

        var items = aPanel.child('fieldset');
        var index = items.items.length;
        items.add(Ext.create('explorer.view.main.SearchCondition'));
        this.loadConditionField(aPanel, type,isMulti, index);
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
            property.setActiveError( '该输入项为必输项');
            flag = false;
        }
        if(operator&&operator.getValue()==null){
            operator.setActiveError( '该输入项为必输项' );
            flag = false;
        }
        return flag;
    },
    onAddButton: function (e) {
        var aPanel = this.getView().up('advancedsearch');
        var type = aPanel.child('tagfield').getValue();
        var isMulti = false;
        if(this.checkQueryCondition(e.up('searchcondition'))) {
            if (type && type != '') {
                if(type.length>1)
                    isMulti = true;
                this.drawCondition(aPanel, type,isMulti);
            }
        }
    },
    onDeleteButton: function (e) {
        var condtion = this.getView();
        var conditionContainer = this.getView().up('fieldset');
        if(conditionContainer.query('searchcondition').length>1) {
            conditionContainer.remove(condtion);
        }

    },
    search: function (bt,e) {
        var me = this;
        var form = this.getView().getForm();
        var conditions = this.getView().query('searchcondition');
        if (form.isValid()) {
            var qmust = [];
            var qmust_not = [];
            var qshould = [];
            var flag = true;
            var type = form.findField('type').getValue();

           //var flag =  Ext.Array.map(conditions,function(condition){
           //     var query = condition.child('combobox[name="query"]').getValue();
           //     var property = condition.child('combobox[name="property"]').getValue();
           //     var operator = condition.child('combobox[name="operator"]').getValue();
           //     return me.checkQueryCondition(condition);
           // });

            //if(Ext.Array.contains(flag,false)) return ;
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
                            queryItem.range[property][startOperator] =  startValue;
                        }
                        if (endValue && endValue != '') {
                            if (!endOperator) {
                                endOperator = 'lt';
                            }
                            queryItem.range[property][endOperator] = endValue;

                        }
                    }
                }
                if (operator == 'term') {
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    queryItem.term = {};
                    queryItem.term[property] = pValue;
                }
                if (operator == 'wildcard') {
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    if(pValue&&pValue!=''){
                        queryItem.wildcard = {};
                        queryItem.wildcard[property] = pValue;
                    }
                }
                if (operator == 'fuzzy') {
                    var pValue = condition.child('textfield[name="value"]').getValue();
                    if (pValue && pValue != '') {
                        queryItem.fuzzy = {};
                        queryItem.fuzzy[property] = {'value': pValue};
                        //var fkey = condition.child('combobox[name="setting"]').getValue();
                        //var fvalue = condition.child('textfield[name="sValue"]').getValue();
                        //if (fkey && fkey != '' && fvalue && fvalue != '') {
                        //    queryItem.fuzzy[property][fkey] = fvalue;
                        //}
                    }
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

            var tabPanel = this.getViewModel().getView().down('tabpanel');
            var index = tabPanel.items.length;
            tabPanel.add({
                title:this.getViewModel().get('advQueryTitle'),
                xtype:'documents',
                closable : true,
                docQuery : encodeURIComponent(Ext.JSON.encode(query)),
                qType :type ,
                index : index
            });
            tabPanel.setActiveTab(index);
            this.getView().up('window').close();

        }
    }
});
