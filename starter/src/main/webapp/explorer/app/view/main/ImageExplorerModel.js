Ext.define('explorer.view.main.ImageExplorerModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.imageexplorer',

    data: {},
    stores: {
        images: {
            fields: ['streamId', 'size', 'contentType', 'pageIndex']
        }
    }

});