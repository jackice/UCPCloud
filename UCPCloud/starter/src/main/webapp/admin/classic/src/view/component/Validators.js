Ext.define('admin.view.component.Validators', {
    singleton: true,


    string:function(val){
        return true;
    },

    bool:function(val){
        return true;
    },

    date: function (val) {
        var date = Ext.Date.parse(val, "c")
        if (!date) return "要求日期格式,如2001-01-01T23:59:59";
        return true;
    },

    float: function (val) {
        var strP = /^-?\d+$/;
        if (val && !strP.test(val)) return "该输入项必须是浮点型";
        return true;
    },

    integer: function (val) {
        var strP = /^-?\d+$/;
        if (val && !strP.test(val)) return "该输入项必须是整型";
        return true;
    }

});