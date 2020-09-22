Ext.define('chemistry.store.ReIndexs', {
    extend: 'Ext.data.Store',
    alias : 'store.reIndexs',
    fields: [
        {name: 'operationId',type : 'string'},
        {name: 'srcIndex',type : 'string'},
        {name: 'targetIndex',type : 'string'},
        {name: 'timestamp', type: 'date', dateFormat: 'time'},
        {name: 'numberOfActions', type: 'int'},
        {name: 'finished', type: 'int'},
        {name: 'total', type: 'int'},
        {name: 'rate', type: 'int'},
        {name: 'executionId', type: 'int'}
    ],
    pageSize: 10,
    proxy: {
        type: 'rest',
        headers: {'Content-Type': "application/json;charset=utf-8" },
        url: '/svc/_reindex',
        startParam : '',
        pageParam : '',
        limitParam : '',
        reader: {
            type: 'json',
            root :'log'
        }
    }
});