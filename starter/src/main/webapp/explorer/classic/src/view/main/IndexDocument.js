Ext.define('explorer.view.main.IndexDocument', {
    extend: 'Ext.form.Panel',
    xtype: 'indexdocument',

    controller: 'indexdoc',
    scrollable : 'y',
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
                anchor: '100%'
            },
            items: [
                {
                    xtype: 'combo',
                    fieldLabel: '类型',
                    name: 'type',
                    displayField: 'displayName',
                    allowBlank: false,
                    valueField: 'name',
                    bind: {
                        store :  '{types}'
                    },
                    listeners: {
                        change:"changeTypeForIndex"
                    }
                },{
                    name : 'name',
                    itemId: 'documentName',
                    fieldLabel: '名称',
                    allowBlank: false
                },{
                    name : 'tag',
                    xtype: 'tagfield',
                    fieldLabel: '标签',
                    displayField: 'tagContext',
                    valueField: 'tagContext',
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
                    })
                },{
                    xtype: 'hiddenfield',
                    name: '_acl'
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
                margin :  '2 5 2 5',
                items: [
                    {
                        fieldLabel: '访问控制项',
                        xtype: 'tagfield',
                        name : 'operationObj',
                        displayField: 'name',
                        valueField: 'id',
                        queryMode: 'local',
                        forceSelection: true,
                        typeAhead: true,
                        minChars: 1
                    }, {
                        xtype: 'textfield',
                        name : 'permission',
                        value : 'read',
                        readOnly : true
                    }
                ]
            },{
                xtype: 'container',
                title: '访问控制列表',
                layout: 'hbox',
                margin :  '2 5 2 5',
                items: [
                    {
                        fieldLabel: '访问控制项',
                        xtype: 'tagfield',
                        name : 'operationObj',
                        displayField: 'name',
                        valueField: 'id',
                        forceSelection: true
                    }, {
                        xtype: 'textfield',
                        name : 'permission',
                        value : 'write',
                        readOnly : true
                    }
                ]
            }]
			
			}]
        }, {
            xtype: 'fieldset',
            itemId: 'stream',
            title: '附件信息',

            defaults: {
                anchor: '100%'
            },

            items: [{
                xtype: 'multifile',
                name: 'file',
                fieldLabel: '附件',
                msgTarget: 'side',
                anchor: '100%',
                buttonText: '浏览...'
            }]
        }];
        me.callParent();
    },

    buttons: [ {
        text: '保存',
        handler : 'save'
    },{
        text: '关闭',
        handler : 'closeWin'
    }],

    listeners: {
        beforerender: 'loadAclOperationObj'
    }

});