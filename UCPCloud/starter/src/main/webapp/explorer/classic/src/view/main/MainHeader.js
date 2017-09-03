Ext.define('explorer.view.main.MainHeader', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-header',
    controller: 'mainheader',
    layout: 'column',
	cls: 'mainHeaderBg',
	bodyCls: 'mainHeader-body',
	
	
	

    //initComponent: function () {
    //    this.callParent();
    //},
    items: [
        {
            width: 230,
			
            bind: {
                html: '<div class="main-logo"><img src="../../../../images/logo.png">{headerTitle}</div>'
            }
        },
        {

           
            xtype : 'toolbar',
			cls: 'mainHeader-toolbar',
            items : [
                {
                    xtype    : 'textfield',
                    name     : 'fullText',
					cls: 'mainHeader-toolbar-input',
                    width : 250,
                    emptyText: '检索词输入区',
                    listeners: {
                        specialkey: function(field, e){
                            if (e.getKey() == e.ENTER) {
                                var button = field.up('toolbar').down('button[itemId=fullText]');
                                button.click(e);
                            }
                        }
                    }
                },
                {
                    text: '全文搜索',
					cls: 'mainHeader-toolbar-btn mainHeader-toolbar-fullText',
					itemId:'fullText',
					iconCls: 'fa fa-search',                     
					handler: 'fullText'
                }
            ]
        },{
			columnWidth: 1,
            xtype: 'label',
			cls: 'mainHeader-username',
            text: Ext.util.Cookies.get('userName')
        },
        {
            width: 270,
            xtype : 'toolbar',
			cls: 'mainHeader-toolbar',
			
            items : [
                {
                    text   : '高级搜索',
					iconCls: 'fa fa-search-plus', 
					cls: 'mainHeader-toolbar-btn mainHeader-toolbar-rightbtn',
                    handler: 'advQuery'
                },
                {
                    text   : '新建文档',
					iconCls: 'fa fa-plus', 
					cls: 'mainHeader-toolbar-btn mainHeader-toolbar-rightbtn',
                    handler  : 'indexDoc'
                },
                {
                    text   : '退出',
					cls: 'mainHeader-toolbar-btn mainHeader-toolbar-rightbtn',
                    handler  : 'logout'
                }
            ]
        }
    ]
});
