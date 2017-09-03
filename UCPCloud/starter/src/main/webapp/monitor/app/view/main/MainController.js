Ext.define('monitor.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',

    onItemSelected: function (sender, record) {
        Ext.Msg.confirm('Confirm', 'Are you sure?', 'onConfirm', this);
    },

    onConfirm: function (choice) {
        if (choice === 'yes') {
            //
        }
    },

    loadNodes: function () {
        var tab = this.getView();
        var nodes = Ext.create('monitor.store.Nodes');
        nodes.load({
            callback: function (records, operation, success) {
                Ext.each(records, function (node) {
                    tab.add({
                        xtype: 'monitorNode',
                        title: node.get('name'),
                        viewmodel: {
                            data: {
                                name: node.get('name'),
                                version: node.get('version')
                            }
                        },
                        iconCls: 'fa fa-server'
                    })
                });
            }
        });
    }
})
;
