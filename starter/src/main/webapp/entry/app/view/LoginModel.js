Ext.define('entry.view.LoginModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.login',
    data: {
        userId: '',
        password: ''
    },
    stores: {
        modules: {
            fields: ['name', 'url'],
            data: [
                {name: '文档管理', url: '/explorer/index.html'},
                {name: '系统管理', url: '/admin/index.html'}
            ]
        }
    }

});