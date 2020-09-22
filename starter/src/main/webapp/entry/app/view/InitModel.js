Ext.define('starter.view.init.InitModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.init',
    data: {
        shards: 5,
        replicas: 1
    }

});