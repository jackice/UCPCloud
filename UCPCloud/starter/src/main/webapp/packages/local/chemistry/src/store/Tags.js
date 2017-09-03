Ext.define('chemistry.store.Tags', {
    extend: 'Ext.data.Store',
    alias : 'store.tags',
    model: 'chemistry.model.Tag',
    pageSize: 10,
    autoSync : false,
    remoteSort : true,
    sorters  : [{
        property : "tagContext",
        direction: "DESC"
    }],
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
    },
    autoLoad: true
});