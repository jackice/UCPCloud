Ext.define('chemistry.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'userName', type: 'string'},
        {name: 'timeInfo', type: 'auto'},
        {name: 'requestInfo', type: 'auto'},
        {name: 'responseInfo', type: 'auto'},
        {name: 'exceptionInfo', type: 'auto'},
        //{name: 'timeInfo.start_format',  type: 'string'},
        //{name: 'timeInfo.end_format',   type: 'string'},
        //{name: 'timeInfo.consume', type: 'string'},
        //{name: 'requestInfo.ipAddress', type: 'string'},
        //{name: 'requestInfo.url', type: 'string'},
        //{name: 'requestInfo.method', type: 'string'},
        //{name: 'requestInfo.params', type: 'string'},
        //{name: 'requestInfo.header', type: 'string'},
        //{name: 'responseInfo.statusCode', type: 'string'},
        //{name: 'responseInfo.header', type: 'string'},
        //{name: 'responseInfo.result', type: 'string'},
        //{name: 'exceptionInfo.msg', type: 'string'},
        //{name: 'exceptionInfo.statusCode', type: 'string'},
        //{name: 'exceptionInfo.stackTrace', type: 'string'},
        {name: 'logDate', type: 'string'}
    ],
    idProperty: '_id'
});