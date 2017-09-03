Ext.define('explorer.overrides.ComboBox', {
    override: 'Ext.form.field.ComboBox',
    queryMode: 'local',
    forceSelection: true,
    typeAhead: true,
    minChars: 1
});