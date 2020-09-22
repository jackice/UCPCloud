Ext.define('admin.view.component.SearchTeamSet', {
    extend: 'Ext.form.FieldSet',
    xtype: 'searchteamset',
    title: '条件',
    layout: 'column',
    defaults: {
        anchor: '100%'
    },
    initComponent: function () {
        Ext.apply(this, {
            items: [{
                xtype: 'searchteam',
                properties: this.properties
            }]
        });
        this.callParent();

    }

});