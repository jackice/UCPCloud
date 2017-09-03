Ext.define('entry.view.init.InitFrame', {
    extend: 'Ext.panel.Panel',
    controller: 'init',
    viewModel: 'init',
    layout: 'center',
    items: [
        {xtype: 'initForm'}
    ]
});