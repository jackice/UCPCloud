Ext.define('explorer.view.main.ModifyDocument', {
    extend: 'Ext.form.Panel',
    xtype: 'modifydocument',
    layout:'vbox',
    controller: 'indexdoc',
    scrollable : 'y',
    defaults: {
        width: '100%'
    },
    fieldDefaults: {
        labelAlign: 'right',
        labelWidth: 115,
        msgTarget: 'side'
    },
    initComponent: function () {
        var me = this;
        this.items = [{
            xtype: 'fieldset',
            title: '文档信息',
            defaultType: 'textfield',
            defaults: {
                anchor: '100%',
                readOnly : true
            },
            items: [
                {
                    fieldLabel: '类型',
                    name: 'typeName',
                    readOnly : true
                },{
                    name : 'name',
                    itemId: 'documentName',
                    fieldLabel: '文档名称',
                    allowBlank: false
                },{
                    name : 'tag',
                    xtype: 'tagfield',
                    fieldLabel: '标签',
                    displayField: 'tagContext',
                    valueField: 'tagContext',
                    publishes: 'value',
                    forceSelection: true,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['_id','tagContext'],
                        proxy: {
                            type: 'ajax',
                            url: '/svc/tags',
                            reader: {
                                type: 'json',
                                rootProperty: 'tags'
                            }
                        },
                        autoLoad: true
                    }),
                    readOnly : false
                }, {
                    fieldLabel: '创建人',
                    name: 'createdBy'
                },{
                    fieldLabel: '创建时间',
                    format : 'Y-m-d H:i:s',
                    xtype: 'datefield',
                    name: 'createdOn'
                },{
                    fieldLabel: '修改人',
                    name: 'lastUpdatedBy'
                },{
                    fieldLabel: '修改时间',
                    format : 'Y-m-d H:i:s',
                    xtype: 'datefield',
                    name: 'lastUpdatedOn'
                },{
                    xtype: 'hiddenfield',
                    name: '_acl'
                },{
                    xtype: 'hiddenfield',
                    name: '_id'
                },{
                    xtype: 'hiddenfield',
                    name: 'type'
                },{
                    xtype:'hidden',
                    name:'_method',
                    value:'PUT'
                }
            ]
        }, {
            xtype: 'fieldset',
            itemId: 'propertyList',
            title: '属性信息',

            defaultType: 'textfield',
            defaults: {
                anchor: '100%'
            },

            items: []
        }, {
            xtype: 'fieldset',
            title: '权限信息',

            defaults: {
                anchor: '100%',
                scrollable : true
            },
            items:[{
                itemId: 'aclList',
                xtype: 'container',
                layout: 'vbox',
                items: [{
                    xtype: 'container',
                    title: '访问控制列表',
                    layout: 'hbox',
                    scrollable: 'y',
                    margin: '2 5 2 5',
                    items: [
                        {
                            fieldLabel: '访问控制项',
                            xtype: 'tagfield',
                            name: 'operationObj',
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true
                        }, {
                            xtype: 'textfield',
                            minWidth: 150,
                            name: 'permission',
                            value: 'read',
                            readOnly: true
                        }
                    ]
                }, {
                    xtype: 'container',
                    title: '访问控制列表',
                    layout: 'hbox',
                    margin: '2 5 2 5',
                    items: [
                        {
                            fieldLabel: '访问控制项',
                            xtype: 'tagfield',
                            name: 'operationObj',
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true
                        }, {
                            xtype: 'textfield',
                            minWidth: 150,
                            name: 'permission',
                            value: 'write',
                            readOnly: true
                        }
                    ]
                }]
            }]
        }, {
            xtype: 'fieldset',
            itemId: 'stream',
            title: '附件信息',

            items: [{
                xtype: 'multifile',
                name: 'file',
                fieldLabel: '附件',
                msgTarget: 'side',
                anchor: '100%',
                buttonText: '浏览...'
            },{
                xtype: 'hiddenfield',
                name: '_removeStreamIds',
                itemId: 'removeStreamIds'
            }]
        }];
        me.callParent();
    },

    buttons: [ {
        text: '保存',
        itemId : 'updateBtn',
        handler : 'save'
    },{
        text: '关闭',
        handler : 'closeWin'
    }],

    listeners: {
        beforerender: 'loadData'
    }

});