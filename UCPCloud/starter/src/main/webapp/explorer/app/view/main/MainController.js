/**
 * This class is the controller for the main view for the application. It is specified as
 * the "controller" of the Main view class.
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('explorer.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',
    loadMenu : function(){
        var me = this;
        var tabPanel = this.getView().down('tabpanel');
        var userId = Ext.util.Cookies.get('userId');
        if(userId&&userId!=''){
            Ext.Ajax.request({
                url: '/svc/views/user/' + userId,
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if (response.responseText != '') {
                        var data = Ext.decode(response.responseText);
                        Ext.Array.each(data.views, function(record, index, countriesItSelf) {
                            var queryContext = record.queryContext;
                            var docQuery = '';
                            var qType = '';
                            if(queryContext&&queryContext!=''){
                                queryContext =  Ext.JSON.decode(decodeURIComponent(queryContext));
                                docQuery =queryContext.query;
                                qType =queryContext.type;
                            }
                            tabPanel.add({
                                title:record.viewName,
                                xtype: 'documents',
                                docQuery : encodeURIComponent(Ext.JSON.encode(docQuery)),
                                qType : qType,
                                index : index
                            });
                        });
                    }
                }
            });
        }

    },
    changeMenu : function(tabPanel, newCard, oldCard, eOpts){
        if (oldCard && oldCard.items.getAt(0))
            oldCard.items.getAt(0).fireEvent('deactivate', {})
        if (newCard && newCard.items.getAt(0))
            newCard.items.getAt(0).fireEvent('activate', {})

    }
});
