Ext.define('admin.view.main.reindex.ReIndex', {
    extend: 'Ext.panel.Panel',
    xtype: 'reIndex',
    controller: 'reIndex',
    viewModel: 'reIndex',
    bind: {
        title: '{listTitle}'
    },
    width: 650,
    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: ['->',
            { xtype: 'button',
            text: '重建索引',
            handler: 'openWin',
            reference: 'reIndexButton',
            disabled : false }
        ]
    }],
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [
                //    {
                //    xtype: 'box',
                //    bind: {html: '<div class="fa fa-info-circle" style="font-size: 18px;margin: 20px 0 0 20px;"> 类型：{reindexType}</div>'}
                //},
                {
                    xtype: 'cartesian',
                    width: '100%',
                    bind : {
                        store : '{reIndexs}'
                    },
                    insetPadding: '40 40 20 20',
                    height: 500,
                    axes: [{
                        type: 'numeric',
                        minimum: 0,
                        maximum: 100,
                        grid: true,
                        position: 'left',
                        title: '百分比',
                        renderer: 'onAxisLabelRender'
                    }, {
                        type: 'time',
                        dateFormat: 'G:i:s',
                        segmenter: {
                            type: 'time',
                            step: {
                                unit: Ext.Date.SECOND,
                                step: 5
                            }
                        },
                        label: {
                            fontSize: 10
                        },
                        grid: true,
                        position: 'bottom',
                        title: '时间',
                        fields: ['timestamp']
                    }],
                    series: [{
                        type: 'line',
                        marker: {
                            type: 'cross',
                            size: 5
                        },
                        style: {
                            miterLimit: 0
                        },
                        xField: 'timestamp',
                        yField: 'rate',
                        tooltip: {
                            trackMouse: true,
                            showDelay: 0,
                            dismissDelay: 0,
                            hideDelay: 0,
                            renderer: 'onSeriesTooltipRender'
                        }
                    }],
                    listeners: {
                        afterrender: 'onTimeChartRendered',
                        destroy: 'onTimeChartDestroy'
                    }
                }]
        });

        this.callParent();
    },
    listeners: {
        afterrender: 'loadData'
    }
});