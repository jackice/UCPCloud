Ext.define('chemistry.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: false,
    remoteSort : true,
    sorters  : [{
        property : "createdOn",
        direction: "DESC"
    }],
    proxy: {
        type: 'rest',
        url: '/svc',
        reader: {
            type: 'json',
            rootProperty: 'documents'
        }
    },
    model:'chemistry.model.Document'
});