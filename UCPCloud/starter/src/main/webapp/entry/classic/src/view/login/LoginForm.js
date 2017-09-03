Ext.define('entry.view.login.LoginForm', {
    extend: 'Ext.form.Panel',
    xtype: 'loginForm',

    title: '登录',
    frame: true,
	cls: 'logincar',
    width: 320,
    bodyPadding: 10,
    defaultType: 'textfield',
    //defaultFocus:'*[name=user]',

    buttons: [
        {
            itemId:'login',
            text: '登录',
            handler: 'login'
        }
    ],
    listeners:{
        afterrender:function(){
            this.down('textfield[name=user]').focus();
        }
    },

    initComponent: function () {
        this.defaults = {
            anchor: '100%',
            labelWidth: 120
        };

        var params = Ext.Object.fromQueryString(window.location.search);
        var dest = {
            xtype: 'combo',
            allowBlank: false,
            displayField: 'name',
            valueField: 'url',
            editable: false,
            fieldLabel: '模块',
            name: 'mo',
            bind: {
                store: '{modules}'
            }
        }
        if (params.url) {
            dest = undefined;
        }

        Ext.apply(this, {
            defaults:{
                listeners: {
                    specialkey: function(field, e){
                        if (e.getKey() == e.ENTER) {
                            var button = field.up('form').down('button[itemId=login]');
                            button.click(e);
                        }
                    }
                }
            },
            items: [{
                allowBlank: false,
                fieldLabel: '用户ID',
                name: 'user',
                emptyText: '用户ID',
                bind: '{userId}'
            }, {
                allowBlank: false,
                fieldLabel: '登录密码',
                name: 'pass',
                emptyText: '密码',
                inputType: 'password',
                bind: '{password}'
            }, dest
            ]
        });

        this.callParent();
    }
});