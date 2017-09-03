Ext.define('explorer.overrides.Tag', {
    override: 'Ext.form.field.Tag',
    queryMode: 'local',
    forceSelection: true,
    typeAhead: true,
    minChars: 1
});