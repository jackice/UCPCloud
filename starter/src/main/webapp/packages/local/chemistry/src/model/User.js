Ext.define('chemistry.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'userId', type: 'string'},
        {name: 'userName', type: 'string'},
        {name: 'email', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'createdBy',  type: 'string'},
        {name: 'createdOn',   type: 'date'},
        {name: 'lastUpdatedBy', type: 'string'},
        {name: 'lastUpdatedOn', type: 'date'}
    ],
    idProperty: 'userId',
    proxy: {
        type: 'rest',
        url: '/svc/users',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'users',
            totalProperty: 'total'
        }
    }
});