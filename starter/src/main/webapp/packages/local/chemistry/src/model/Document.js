Ext.define('chemistry.model.Document', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'createdBy', type: 'string'},
        {name: 'tag', type: 'string'},
        {name: 'lastUpdatedBy', type: 'string'},
        {name: 'createdOn', type: 'date',dateFormat: 'c'},
        {name: 'lastUpdatedOn', type: 'date',dateFormat: 'c'},
        {name: 'score', type: 'float'},
        {name: 'highlight', type: 'auto'}
    ],
    idProperty: 'id'
});