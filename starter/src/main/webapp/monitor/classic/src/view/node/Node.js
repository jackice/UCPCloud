Ext.define('monitor.view.node.Node', {
    extend: 'Ext.panel.Panel',
    xtype: 'monitorNode',
    controller: 'node',

    layout: 'vbox',

    items: [{
        xtype: 'box',
        bind: {html: '<div class="fa fa-info-circle" style="font-size: 18px;margin: 20px 0 0 20px;"> {name} : {version}</div>'}
    }, {
        xtype: 'cartesian',
        legend: {
            docked: 'bottom'
        },
        reference: 'time-chart',
        insetPadding: '40 40 20 20',
        width: '100%',
        height: 500,
        store: {
            type: 'os'
        },
        axes: [{
            type: 'numeric',
            minimum: 0,
            maximum: 100,
            grid: true,
            position: 'left',
            title: '百分比'
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
            yField: 'os_cpu_usage'
        }, {
            type: 'line',
            marker: {
                type: 'cross',
                size: 5
            },
            style: {
                miterLimit: 0
            },
            xField: 'timestamp',
            yField: 'os_mem_usage'
        }, {
            type: 'line',
            marker: {
                type: 'cross',
                size: 5
            },
            style: {
                miterLimit: 0
            },
            xField: 'timestamp',
            yField: 'jvm_mem_usage'
        }],
        listeners: {
            afterrender: 'startTimeChart'
        }
    }]

});