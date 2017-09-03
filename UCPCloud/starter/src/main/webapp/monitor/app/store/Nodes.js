Ext.define('monitor.store.Nodes', {
    extend: 'Ext.data.JsonStore',
    alias: 'store.nodes',
    proxy: {
        type: 'rest',
        url: '/cat/nodes'
    },

    fields: ['name', 'transport_address', 'host', 'ip', 'version', 'build']
});