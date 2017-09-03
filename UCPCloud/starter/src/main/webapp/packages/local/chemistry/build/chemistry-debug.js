Ext.define('chemistry.model.Document', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'createdBy',
            type: 'string'
        },
        {
            name: 'tag',
            type: 'string'
        },
        {
            name: 'lastUpdatedBy',
            type: 'string'
        },
        {
            name: 'createdOn',
            type: 'date',
            dateFormat: 'c'
        },
        {
            name: 'lastUpdatedOn',
            type: 'date',
            dateFormat: 'c'
        },
        {
            name: 'score',
            type: 'float'
        },
        {
            name: 'highlight',
            type: 'auto'
        }
    ],
    idProperty: 'id'
});

Ext.define('chemistry.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'groupId',
            type: 'string'
        },
        {
            name: 'groupName',
            type: 'string'
        },
        {
            name: 'createdBy',
            type: 'string'
        },
        {
            name: 'createdOn',
            type: 'date'
        },
        {
            name: 'lastUpdatedBy',
            type: 'string'
        },
        {
            name: 'lastUpdatedOn',
            type: 'date'
        },
        {
            name: 'users',
            type: 'auto'
        }
    ],
    idProperty: 'groupId',
    proxy: {
        type: 'rest',
        url: '/svc/groups',
        actionMethods: {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'groups',
            totalProperty: 'total'
        }
    }
});

Ext.define('chemistry.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: '_id',
            type: 'string'
        },
        {
            name: 'userName',
            type: 'string'
        },
        {
            name: 'timeInfo',
            type: 'auto'
        },
        {
            name: 'requestInfo',
            type: 'auto'
        },
        {
            name: 'responseInfo',
            type: 'auto'
        },
        {
            name: 'exceptionInfo',
            type: 'auto'
        },
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
        {
            name: 'logDate',
            type: 'string'
        }
    ],
    idProperty: '_id'
});

Ext.define('chemistry.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'defaultValue',
            type: 'string'
        },
        {
            name: 'pattern',
            type: 'string'
        },
        {
            name: 'promptMessage',
            type: 'string'
        },
        {
            name: 'order',
            type: 'int'
        },
        {
            name: 'required',
            type: 'boolean'
        },
        {
            name: 'analyzer',
            type: 'string'
        },
        //{name: 'indexAnalyzer', type: 'string'},
        //{name: 'searchAnalyzer', type: 'string'},
        {
            properties: 'index',
            type: 'string'
        }
    ]
});

Ext.define('chemistry.model.Tag', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: '_id',
            type: 'string'
        },
        {
            name: 'tagContext',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],
    idProperty: '_id',
    proxy: {
        type: 'rest',
        url: '/svc/tags',
        actionMethods: {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'tags',
            totalProperty: 'total'
        }
    }
});

Ext.define('chemistry.model.Type', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'displayName',
            type: 'string'
        },
        {
            name: 'properties',
            type: 'auto'
        }
    ],
    idProperty: 'name'
});

Ext.define('chemistry.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'userId',
            type: 'string'
        },
        {
            name: 'userName',
            type: 'string'
        },
        {
            name: 'email',
            type: 'string'
        },
        {
            name: 'password',
            type: 'string'
        },
        {
            name: 'createdBy',
            type: 'string'
        },
        {
            name: 'createdOn',
            type: 'date'
        },
        {
            name: 'lastUpdatedBy',
            type: 'string'
        },
        {
            name: 'lastUpdatedOn',
            type: 'date'
        }
    ],
    idProperty: 'userId',
    proxy: {
        type: 'rest',
        url: '/svc/users',
        actionMethods: {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'users',
            totalProperty: 'total'
        }
    }
});

Ext.define('chemistry.model.View', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: '_id',
            type: 'string'
        },
        {
            name: 'groups',
            type: 'string'
        },
        {
            name: 'users',
            type: 'string'
        },
        {
            name: 'viewName',
            type: 'string'
        },
        {
            name: 'queryContext',
            type: 'string'
        }
    ],
    idProperty: '_id'
});

Ext.define('chemistry.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: false,
    remoteSort: true,
    sorters: [
        {
            property: "createdOn",
            direction: "DESC"
        }
    ],
    proxy: {
        type: 'rest',
        url: '/svc',
        reader: {
            type: 'json',
            rootProperty: 'documents'
        }
    },
    model: 'chemistry.model.Document'
});

Ext.define('chemistry.store.Groups', {
    extend: 'Ext.data.Store',
    alias: 'store.groups',
    model: 'chemistry.model.Group',
    pageSize: 10,
    autoSync: false,
    remoteSort: true,
    sorters: [
        {
            property: "groupName",
            direction: "DESC"
        }
    ],
    proxy: {
        type: 'rest',
        url: '/svc/groups',
        actionMethods: {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'groups',
            totalProperty: 'total'
        }
    },
    //    transform: {
    //        fn: function(data) {
    //            return data;
    //       },
    //       scope: this
    //   }
    autoLoad: true
});

Ext.define('chemistry.store.Logs', {
    extend: 'Ext.data.Store',
    alias: 'store.logs',
    model: 'chemistry.model.Log',
    pageSize: 10,
    sorters: [
        {
            property: "logDate",
            direction: "DESC"
        }
    ],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/svc/logs',
        reader: {
            type: 'json',
            rootProperty: 'logInfos',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});

Ext.define('chemistry.store.Properties', {
    extend: 'Ext.data.ArrayStore',
    alias: 'store.properties',
    model: 'chemistry.model.Property'
});

Ext.define('chemistry.store.ReIndexs', {
    extend: 'Ext.data.Store',
    alias: 'store.reIndexs',
    fields: [
        {
            name: 'operationId',
            type: 'string'
        },
        {
            name: 'srcIndex',
            type: 'string'
        },
        {
            name: 'targetIndex',
            type: 'string'
        },
        {
            name: 'timestamp',
            type: 'date',
            dateFormat: 'time'
        },
        {
            name: 'numberOfActions',
            type: 'int'
        },
        {
            name: 'finished',
            type: 'int'
        },
        {
            name: 'total',
            type: 'int'
        },
        {
            name: 'rate',
            type: 'int'
        },
        {
            name: 'executionId',
            type: 'int'
        }
    ],
    pageSize: 10,
    proxy: {
        type: 'rest',
        headers: {
            'Content-Type': "application/json;charset=utf-8"
        },
        url: '/svc/_reindex',
        startParam: '',
        pageParam: '',
        limitParam: '',
        reader: {
            type: 'json',
            root: 'log'
        }
    }
});

Ext.define('chemistry.store.Tags', {
    extend: 'Ext.data.Store',
    alias: 'store.tags',
    model: 'chemistry.model.Tag',
    pageSize: 10,
    autoSync: false,
    remoteSort: true,
    sorters: [
        {
            property: "tagContext",
            direction: "DESC"
        }
    ],
    proxy: {
        type: 'rest',
        url: '/svc/tags',
        actionMethods: {
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

Ext.define('chemistry.store.Types', {
    extend: 'Ext.data.Store',
    alias: 'store.types',
    model: 'chemistry.model.Type',
    pageSize: 10,
    autoSync: true,
    autoLoad: true,
    proxy: {
        type: 'rest',
        headers: {
            'Content-Type': "application/json;charset=utf-8"
        },
        url: '/svc/types',
        startParam: '',
        pageParam: '',
        limitParam: '',
        reader: {
            type: 'json',
            root: 'documentTypes'
        }
    }
});

Ext.define('chemistry.store.Users', {
    extend: 'Ext.data.Store',
    alias: 'store.users',
    model: 'chemistry.model.User',
    pageSize: 10,
    //autoSync : true,
    remoteSort: true,
    sorters: [
        {
            property: "userId",
            direction: "DESC"
        }
    ],
    proxy: {
        type: 'rest',
        url: '/svc/users',
        headers: {},
        //'_method' : 'QUERY',
        //'Content-Type' : 'application/json;charset=UTF-8'
        //paramsAsJson :true,
        actionMethods: {
            update: 'PATCH'
        },
        //read : 'POST'
        reader: {
            type: 'json',
            rootProperty: 'users',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});

Ext.define('chemistry.store.Views', {
    extend: 'Ext.data.Store',
    alias: 'store.views',
    model: 'chemistry.model.View',
    autoSync: true,
    remoteSort: false,
    sorters: [
        {
            property: "viewName",
            direction: "DESC"
        }
    ],
    proxy: {
        type: 'rest',
        headers: {
            'Content-Type': "application/json;charset=utf-8"
        },
        url: '/svc/views',
        startParam: '',
        pageParam: '',
        limitParam: '',
        reader: {
            type: 'json',
            root: 'views'
        }
    },
    autoLoad: true
});

