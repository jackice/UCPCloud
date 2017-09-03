Ext.define('chemistry.model.Type', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name',  type: 'string'},
        {name: 'description',   type: 'string'},
        {name: 'displayName', type: 'string'},
        {name: 'properties', type: 'auto'}
    ],
    idProperty: 'name'
});