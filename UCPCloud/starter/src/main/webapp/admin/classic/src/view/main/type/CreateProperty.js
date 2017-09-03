Ext.define('admin.view.main.type.CreateProperty', {
    extend: 'Ext.form.Panel',
    xtype: 'createProperty',
    controller: 'type',
    width : 500,
    viewModel: 'type',
    layout: 'anchor',
    bodyPadding: 5,
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [{
        fieldLabel: '属性名称',
        name: 'name',
        allowBlank: false
    },{
        fieldLabel: '属性类型',
        name: 'type',
        xtype : 'combo',
        displayField: 'name',
        valueField: 'value',
        store: Ext.create("Ext.data.Store", {
            fields: ["name", "value"],
            data: [
                { name: "string", value: "string" },
                { name: "integer", value: "integer" },
                { name: "float", value: "float" },
                { name: "boolean", value: "boolean" },
                { name: "date", value: "date"}
            ]
        }),
        listeners: {
            change : 'changeType'
        },
        allowBlank: false
    },{
        fieldLabel: '默认值',
        itemId : 'defaultValue',
        name: 'defaultValue'
    },{
        fieldLabel: '约束条件',
        name: 'pattern'
    },{
        fieldLabel: '约束提示',
        name: 'promptMessage'
    },{
        fieldLabel: '必填',
        name: 'required',
        uncheckedValue : false,
        xtype : 'checkboxfield',
        inputValue: true
    },
        {
        fieldLabel: '索引',
        name: 'index',
        xtype : 'checkboxfield',
        allowBlank: false,
        checked:true,
        inputValue: "not_analyzed",
        uncheckedValue : "no"
    },
        //{
    //    fieldLabel: 'IndexAnalyzer',
    //    name: 'indexAnalyzer',
    //    xtype: 'combo',
    //    displayField: 'name',
    //    valueField: 'value',
    //    store: Ext.create("Ext.data.Store", {
    //        fields: ["name", "value"],
    //        data: [
    //            { name: "ansj_index", value: "ansj_index" }
    //        ]
    //    })
    //},{
    //    fieldLabel: 'SearchAnalyzer',
    //    name: 'searchAnalyzer',
    //    xtype: 'combo',
    //    displayField: 'name',
    //    valueField: 'value',
    //    store: Ext.create("Ext.data.Store", {
    //        fields: ["name", "value"],
    //        data: [
    //            { name: "ansj_query", value: "ansj_query" }
    //        ]
    //    })
    //},
        {
        fieldLabel: '排序',
        name: 'order',
        allowBlank: false,
        validator : function (val) {
                if(!val) return true;
                var strP =  /^-?\d+$/;
                if (!strP.test(val)) return "该输入项必须是整型";
                if(val > 2147483647||val < -2147483647) return "该输入项必须是整型";
                return true;
        }
    }
        //,{
        //    name: 'index',
        //    xtype: 'hiddenfield',
        //    value : 'not_analyzed'
        //},{
        //    name: 'analyzer',
        //    xtype: 'hiddenfield'
        //
        //}
    ],

    // Reset and Submit buttons
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }, {
        text: '保存',
        formBind: true, //only enabled once the form is valid
        disabled: true,
        handler: 'saveProperty'
    }],
    listeners: {
        afterrender:'loadProperty'
    }
});