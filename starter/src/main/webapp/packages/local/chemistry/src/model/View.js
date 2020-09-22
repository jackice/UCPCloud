Ext.define('chemistry.model.View', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id',  type: 'string'},
        {name: 'groups',  type: 'string'},
        {name: 'users',  type: 'string'},
        {name: 'viewName',  type: 'string'},
        {name: 'queryContext',   type: 'string'}
    ],
    idProperty: '_id'
});