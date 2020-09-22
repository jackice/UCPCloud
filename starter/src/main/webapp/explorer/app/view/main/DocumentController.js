Ext.define('explorer.view.main.DocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.document',
    detail : function(){
        var me = this;
        var records = this.getView().getSelectionModel().getSelection();
        if (records && records.length > 0) {
            if(records.length > 1 ){
                Ext.Msg.alert('提示信息', '同时只能查看一个文档');
                return;
            }
            var record = records[0];
            Ext.create('Ext.window.Window', {
                title: '修改文档',
                layout: 'center',
        		width: '75%',
       			height: '95%',
                items: [{
                    xtype: 'modifydocument',
					width: '100%',
					height: '100%',
                	bodyPadding: '10',
                    docData : record,
                    docStore :me.getView().store
                }]
            }).show();
        } else {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
    },
    deleteDoc : function(e){
        var me = this;
        var records = this.getView().getSelectionModel().getSelection();
        var store = e.up('grid').store;
        if (records && records.length > 0) {
            var result = [];
            Ext.Msg.confirm("确认", "是否删除该文档？", function (r) {
                if (r != 'yes')return;
                var invalidDelete = [];
                Ext.Array.each(records, function(record, index, countriesItSelf) {
                   var _allowableActions =  record.get("_allowableActions");
                    if(!Ext.Array.contains(_allowableActions,"write")){
                        invalidDelete.push(record);
                    }else{
                        result.push({"type":record.get("_type").name,"id":record.get("_id")});
                    }
                });
                if(invalidDelete.length==0){
                    var doc = Ext.getBody( );
                    doc.mask('加载中...');
                    Ext.Ajax.request({
                        method: 'DELETE',
                        url: '/svc',
                        headers: {'Content-Type': 'application/json;charset=utf-8'},
                        params: Ext.JSON.encode(result),
                        callback: function (options, success, response) {
                            if (!success) {
                                doc.unmask();
                                return;
                            }
                            setTimeout(function(){
                                var errors = Ext.decode(response.responseText);
                                var flag = true;
                                if(Array.isArray(errors)){
                                    Ext.Array.each(errors, function(error, index, countriesItSelf) {
                                        if(!error.success){
                                            flag = false;
                                        }
                                    });
                                }
                                doc.unmask();
                                if(flag){
                                    Ext.toast({
                                        html: '删除成功',
                                        title: '提示信息',
                                        width: 200,
                                        align: 't'
                                    });
                                }else{
                                    Ext.toast({
                                        html: '内容部分删除失败!请核对删除文档',
                                        title: '提示信息',
                                        width: 300,
                                        align: 't'
                                    });
                                }
                            store.load();
                            },2000);
                        }
                    });
                }else{
                    me.getView().getSelectionModel().deselect(invalidDelete);
                    Ext.Msg.alert('提示信息', '没有操作权限.');
                    return;
                }

            });

        } else {
            Ext.Msg.alert('提示信息', '至少选中一行');
            return;
        }
    },

    loadData : function(e, eOpts ){
        var me = this;
        var query = this.getView().docQuery;
        var params = {};
        if(this.getView().limit&&this.getView().limit!=''){
            params ={
                highlight:true,
                query:query,
                allowableActions : true,
                limit: this.getView().limit
            };
        }else{
            params ={
                highlight:false,
                query:query,
                allowableActions : true,
                types: this.getView().qType
            };
        }
        var store =  Ext.create('chemistry.store.Documents');
        store.getProxy().extraParams =params;
        e.bindStore(store);
        e.down('pagingtoolbar').bindStore(store);
        store.load();

    },
    showImage: function (grid, record, item, index, e, eOpts) {
        if(record.get('_streams')&&record.get('_streams').length>0){
            var _streams = record.get('_streams');
            var sContentType =  _streams[0].contentType;
            if(Ext.Array.contains(['application/pdf','image/tiff','image/jpeg','image/png'],sContentType)){
                Ext.create('Ext.window.Window', {
                    layout: 'fit',
                    title: '图像浏览',
                    maximized: true,
                    items: [{
                        xtype: 'pdf',
                        record: record
                    }]
                }).show();
            }else{
                Ext.Msg.alert('提示信息', '不支持该格式的文档浏览');
                return;
            }
        }else{
            Ext.Msg.alert('提示信息', '无附件可以浏览');
            return;

        }


    }
});
