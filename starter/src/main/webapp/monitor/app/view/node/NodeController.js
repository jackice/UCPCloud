Ext.define('monitor.view.node.NodeController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.node',

    onDeactivate: function () {
        this.getView().items.each(function (item) {
            item.getController().stopTimeChart();
        });
    },

    onActivate: function () {
        this.getView().items.each(function (item) {
            item.getController().restartTimeChart();
        });
    },

    startTimeChart: function () {
        this.timeChartTask = Ext.TaskManager.start({
            run: this.refresh,
            interval: 5000,
            scope: this
        });
    },

    restartTimeChart: function () {
        if (this.timeChartTask)
            Ext.TaskManager.start(this.timeChartTask);
    },

    stopTimeChart: function () {
        if (this.timeChartTask) {
            Ext.TaskManager.stop(this.timeChartTask);
        }
    },

    refresh: function () {
        var me = this,
            chart = me.lookupReference('time-chart'),
            store = chart.getStore();
        var vm = me.getView();
        store.load({params: {node: vm.getTitle()}});
    }
});
