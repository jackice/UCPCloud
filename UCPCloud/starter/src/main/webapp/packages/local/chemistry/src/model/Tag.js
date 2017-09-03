Ext.define('chemistry.model.Tag', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'tagContext',  type: 'string'},
        {name: 'description',   type: 'string'}
    ],
    idProperty: '_id',
    proxy: {
        type: 'rest',
        url: '/svc/tags',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'tags',
            totalProperty: 'total'
        }
    }
});