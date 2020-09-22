Ext.define('chemistry.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'groupId', type: 'string'},
        {name: 'groupName', type: 'string'},
        {name: 'createdBy',  type: 'string'},
        {name: 'createdOn',   type: 'date'},
        {name: 'lastUpdatedBy', type: 'string'},
        {name: 'lastUpdatedOn', type: 'date'},
        {name: 'users', type: 'auto'}
    ],
    idProperty: 'groupId',
    proxy: {
        type: 'rest',
        url: '/svc/groups',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'groups',
            totalProperty: 'total'
        }
    }
});