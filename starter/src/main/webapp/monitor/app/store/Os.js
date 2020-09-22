Ext.define('monitor.store.Os', {
    extend: 'Ext.data.JsonStore',
    alias: 'store.os',
    proxy: {
        type: 'rest',
        url: '/cat/os'
    },
    fields: [
        {name: 'node'},
        {name: 'timestamp', type: 'date', dateFormat: 'time'},
        {name: 'os_cpu_usage', type: 'int'},
        {name: 'os_mem_usage', type: 'int'},
        {name: 'jvm_mem_usage', type: 'int'}
    ]

});