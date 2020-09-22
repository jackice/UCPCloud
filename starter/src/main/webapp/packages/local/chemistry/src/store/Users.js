Ext.define('chemistry.store.Users', {
    extend: 'Ext.data.Store',
    alias : 'store.users',
    model: 'chemistry.model.User',
    pageSize: 10,
    //autoSync : true,
    remoteSort : true,
    sorters  : [{
        property : "userId",
        direction: "DESC"
    }],
    proxy: {
        type: 'rest',
        url: '/svc/users',
        headers: {
            //'_method' : 'QUERY',
            //'Content-Type' : 'application/json;charset=UTF-8'
        },
        //paramsAsJson :true,

        actionMethods : {
            update: 'PATCH'
            //read : 'POST'
        },
        reader: {
            type: 'json',
            rootProperty: 'users',
            totalProperty: 'total'
        }
    },
    autoLoad: true

});