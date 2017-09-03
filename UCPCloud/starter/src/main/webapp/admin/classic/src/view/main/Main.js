/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('admin.view.main.Main', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-main',

    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',

        'admin.view.main.MainController',
        'admin.view.main.MainModel'
    ],

    controller: 'main',
    viewModel: 'main',


    layout: 'border',

    bodyBorder: false,

    defaults: {
        split: false
    },
    items: [ {
        region: 'north',
        height: 75,
        minHeight: 75,
        maxHeight: 150,
		
        xtype : 'app-header'
    },
        {
            region: 'center',
            xtype: 'tabpanel',
            ui: 'navigation',
            tabPosition: 'left',
            tabRotation: 0,
            tabBar: {
                border: false,
				cls: 'tabbar-menu'
            },
            tabWidth: 205,
            minTabWidth: 205,
            defaults: {
                textAlign: 'left',
                //scrollable: true,
                margin: 5
            },
            items: [
                {
                title: '用户',
                iconCls: 'fa fa-user',
                xtype: 'users'
            }, {
                title: '组',
                iconCls: 'fa fa-users',
                xtype: 'groups'
            }, {
                    title: '标签',
                    iconCls: 'fa fa-bookmark',
                    xtype: 'tags'
            }, {
                    title: '视图',
                    iconCls: 'fa fa-list-ul',
                    xtype: 'views'
                }, {
                title: '类型',
                iconCls: 'fa fa-file-text-o',
                xtype: 'types'
            }, {
                    title: '日志',
                    iconCls: 'fa fa-calendar',
                    xtype: 'logs'
            },  {
                    title: '重建索引',
                    iconCls: 'fa fa-area-chart',
                    xtype: 'reIndex'
                }, {
                title: '配置',
                iconCls: 'fa fa-cog',
                xtype: 'config'
            }
            ]
        }]
     });