Ext.define('admin.view.main.configuartion.Analyzer', {
    extend: 'Ext.panel.Panel',
    xtype: 'analyzer',

    items: [
        {
            xtype: 'fieldset',
            title: '分词器测试',
            layout: 'hbox',
            items: [{
                xtype: 'textfield',
                fieldLabel: '文本',
                flex: 1,
                name: 'text',
                bind: '{analyzerText}'
            }, {
                xtype: 'combo',
                store: ['standard', 'ansj_index'],
                bind: '{analyzer}'
            }, {
                xtype: 'button',
                text: '分析',
                handler: 'analyze'
            }
            ]
        }
    ]


});