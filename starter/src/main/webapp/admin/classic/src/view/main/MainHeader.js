Ext.define('admin.view.main.MainHeader', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-header',
    controller: 'main',
    layout: 'column',
	cls: 'mainHeaderBg',
	bodyCls: 'mainHeader-body',

    //initComponent: function () {
    //    this.callParent();
    //},
    items: [
        {
            width: 230,
            bind: {
                html: '<div class="main-logo"><img src="../../../../images/logo.png">{headerTitle}</div>'
            }
        },{
            columnWidth: 1,
			xtype: 'label',
			cls: 'mainHeader-username',
            text: Ext.util.Cookies.get('userName')
        },
        {
			width: 70,
            xtype : 'toolbar',
			cls: 'mainHeader-toolbar',
            items : [
                {
                    text   : '退出',
					cls: 'mainHeader-toolbar-btn exit',
                    handler  : 'logout'
                }
            ]
        }
    ]
});
