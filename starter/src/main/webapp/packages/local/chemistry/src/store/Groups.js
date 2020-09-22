Ext.define('chemistry.store.Groups', {
    extend: 'Ext.data.Store',
    alias : 'store.groups',
    model: 'chemistry.model.Group',
    pageSize: 10,
    autoSync : false,
    remoteSort : true,
    sorters  : [{
            property : "groupName",
            direction: "DESC"
        }],
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
        //    transform: {
        //        fn: function(data) {
        //            return data;
         //       },
         //       scope: this
         //   }
        }
    },
    autoLoad: true
});