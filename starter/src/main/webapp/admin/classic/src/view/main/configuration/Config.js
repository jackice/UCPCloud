Ext.define('admin.view.main.configuartion.Config', {
    extend: 'Ext.panel.Panel',
    xtype: 'config',

    default: {
    },
    controller: 'config',
    viewModel: 'config',
    bind: {
        title: '{title}'
    },
	bodyPadding: 10,
    items: [
        {xtype: 'analyzer'}
    ]

});