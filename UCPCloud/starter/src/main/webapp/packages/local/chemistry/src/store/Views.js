Ext.define('chemistry.store.Views', {
    extend: 'Ext.data.Store',
    alias : 'store.views',
    model: 'chemistry.model.View',
    autoSync : true,
    remoteSort : false,
    sorters  : [{
        property : "viewName",
        direction: "DESC"
    }],
    proxy: {
        type: 'rest',
        headers: {'Content-Type': "application/json;charset=utf-8" },
        url: '/svc/views',
        startParam : '',
        pageParam : '',
        limitParam : '',
        reader: {
            type: 'json',
            root :'views'
        }
    },
    autoLoad: true
});