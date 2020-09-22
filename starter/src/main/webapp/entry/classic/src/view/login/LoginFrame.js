Ext.define('entry.view.login.LoginFrame', {
    extend: 'Ext.panel.Panel',
    controller: 'login',
    viewModel: 'login',
    layout: 'center',
	bodyCls: 'loginbg',
    items: [
        {xtype: 'loginForm'}
    ]
});