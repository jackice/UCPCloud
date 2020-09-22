Ext.define('admin.view.main.log.Log', {
    extend: 'Ext.form.Panel',
    xtype: 'log',
    controller: 'log',
    viewModel: 'log',
    buttons: [{
        text: '关闭',
        handler: function() {
            this.up('window').close();
        }
    }],
    bodyPadding: 5,
    width: 880,
    scrollable : true,
    height : 600,
    defaults: {
        anchor: '100%'
    },
    // The fields
    defaultType: 'textfield',
    items: [
        {
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
                {
                    fieldLabel: '操作用户',
                    name: 'userName'
                },{
                    fieldLabel: '开始时间',
                    name: 'timeInfo.start_format'
                },{
                    fieldLabel: '结束时间',
                    name: 'timeInfo.end_format'
                }
            ]
        }, {
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
                {
                    fieldLabel: '消耗时间',
                    name: 'timeInfo.consume_format'
                }, {
                    fieldLabel: '请求ip地址',
                    name: 'requestInfo.ipAddress'
                },{
                    fieldLabel: '请求地址',
                        name: 'requestInfo.url'
                }
            ]
        },{
            xtype: 'container',
            layout: 'hbox',
            defaultType : 'textfield',
            items: [
               {
                    fieldLabel: '请求方法',
                    name: 'requestInfo.method'
                },{
                    fieldLabel: '响应状态码',
                    name: 'responseInfo.statusCode'
                },{
                    fieldLabel: '异常编码',
                    name: 'exceptionInfo.statusCode'
                }
            ]
        },{
        xtype     : 'textfield',
        fieldLabel: '请求参数',
        name: 'requestInfo.params'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'requestInfo.header',
        fieldLabel: '请求的表头',
        scrollable : 'y',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'responseInfo.header',
        fieldLabel: '响应表头',
        scrollable : 'y',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'responseInfo.result',
        fieldLabel: '响应结果',
        maxHeight : 100,
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
        name      : 'exceptionInfo.msg',
        maxHeight : 100,
        scrollable : 'y',
        fieldLabel: '异常信息',
        anchor    : '100%'
    },{
        xtype     : 'textareafield',
        grow      : true,
            maxHeight : 100,
            scrollable : 'y',
        name      : 'exceptionInfo.stackTrace',
        fieldLabel: '异常结果',
        anchor    : '100%'
    },
        {xtype     : 'textfield',
        fieldLabel: '日志日期',
        name: 'logDate'
    }],
    listeners: {
        afterrender : 'loadData'
    }
});