Ext.define('admin.view.main.UserModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.user',
    stores: {
        users: {type: 'users'}
    },
    /*formulas: {
        size: {
            get : function(get){
                return get('pageSize');
            },
            set : function(size){
                this.set('pageSize', size);
                this.getStore('users').setPageSize(size);
                this.getStore('users').load();
            }
        }
    },*/
    data: {
        pageSize : 10,
        listTitle: '用户管理',
        createTitle : '创建用户',
        modifyTitle : '修改用户'
    }

});
