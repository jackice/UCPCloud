Ext.define('admin.overrides.Date', {
    override: 'Ext.picker.Date',
    //clearText: 'Clear',
    renderTpl: [
        '<div id="{id}-innerEl" data-ref="innerEl" role="presentation">',
        '<div class="{baseCls}-header">',
        '<div id="{id}-prevEl" data-ref="prevEl" class="{baseCls}-prev {baseCls}-arrow" role="presentation" title="{prevText}"></div>',
        '<div id="{id}-middleBtnEl" data-ref="middleBtnEl" class="{baseCls}-month" role="heading">{%this.renderMonthBtn(values, out)%}</div>',
        '<div id="{id}-nextEl" data-ref="nextEl" class="{baseCls}-next {baseCls}-arrow" role="presentation" title="{nextText}"></div>',
        '</div>',
        '<table role="grid" id="{id}-eventEl" data-ref="eventEl" class="{baseCls}-inner" cellspacing="0" tabindex="0">',
        '<thead>',
        '<tr role="row">',
        '<tpl for="dayNames">',
        '<th role="columnheader" class="{parent.baseCls}-column-header" aria-label="{.}">',
        '<div role="presentation" class="{parent.baseCls}-column-header-inner">{.:this.firstInitial}</div>',
        '</th>',
        '</tpl>',
        '</tr>',
        '</thead>',
        '<tbody>',
        '<tr role="row">',
        '<tpl for="days">',
        '{#:this.isEndOfWeek}',
        '<td role="gridcell">',
        '<div hidefocus="on" class="{parent.baseCls}-date"></div>',
        '</td>',
        '</tpl>',
        '</tr>',
        '</tbody>',
        '</table>',
        '<tpl if="showToday">',
        '<div id="{id}-footerEl" data-ref="footerEl" role="presentation" class="{baseCls}-footer">{%this.renderClearBtn(values, out)%}{%this.renderTodayBtn(values, out)%}</div>',
        '</tpl>',
        // These elements are used with Assistive Technologies such as screen readers
        '<div id="{id}-todayText" class="' + Ext.baseCSSPrefix + 'hidden-clip">{todayText}.</div>',
        '<div id="{id}-ariaMinText" class="' + Ext.baseCSSPrefix + 'hidden-clip">{ariaMinText}.</div>',
        '<div id="{id}-ariaMaxText" class="' + Ext.baseCSSPrefix + 'hidden-clip">{ariaMaxText}.</div>',
        '<div id="{id}-ariaDisabledDaysText" class="' + Ext.baseCSSPrefix + 'hidden-clip">{ariaDisabledDaysText}.</div>',
        '<div id="{id}-ariaDisabledDatesText" class="' + Ext.baseCSSPrefix + 'hidden-clip">{ariaDisabledDatesText}.</div>',
        '</div>',
        {
            firstInitial: function(value) {
                return Ext.picker.Date.prototype.getDayInitial(value);
            },
            isEndOfWeek: function(value) {
                // convert from 1 based index to 0 based
                // by decrementing value once.
                value--;
                var end = value % 7 === 0 && value !== 0;
                return end ? '</tr><tr role="row">' : '';
            },
            renderClearBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.clearBtn.getRenderTree(), out);
            },
            renderTodayBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.todayBtn.getRenderTree(), out);
            },

            renderMonthBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.monthBtn.getRenderTree(), out);
            }
        }
    ],
    getRefItems: function() {
        var results = [],
            monthBtn = this.monthBtn,
            todayBtn = this.todayBtn;
            clearBtn = this.clearBtn;

        if (monthBtn) {
            results.push(monthBtn);
        }

        if (todayBtn) {
            results.push(todayBtn);
        }
        if (clearBtn) {
            results.push(clearBtn);
        }
        return results;
    },
    beforeRender: function() {
        /*
         * days array for looping through 6 full weeks (6 weeks * 7 days)
         * Note that we explicitly force the size here so the template creates
         * all the appropriate cells.
         */
        var me = this,
            encode = Ext.String.htmlEncode,
            days = new Array(me.numDays),
            today = Ext.Date.format(new Date(), me.format);

        if (me.padding && !me.width) {
            me.cacheWidth();
        }

        me.monthBtn = new Ext.button.Split({
            ownerCt: me,
            ownerLayout: me.getComponentLayout(),
            text: '',
            tooltip: me.monthYearText,
            tabIndex: -1,
            ariaRole: 'presentation',
            listeners: {
                click: me.doShowMonthPicker,
                arrowclick: me.doShowMonthPicker,
                scope: me
            }
        });
        me.clearBtn = new Ext.button.Button({
            ui: me.footerButtonUI,
            ownerCt: me,
            ownerLayout: me.getComponentLayout(),
            text: me.clearText,
            tooltip: me.clearText,
            tooltipType: 'title',
            ariaRole: 'presentation',
            handler: me.clearValue,
            scope: me
        });
        if (me.showToday) {
            me.todayBtn = new Ext.button.Button({
                ui: me.footerButtonUI,
                ownerCt: me,
                ownerLayout: me.getComponentLayout(),
                text: Ext.String.format(me.todayText, today),
                tooltip: Ext.String.format(me.todayTip, today),
                tooltipType: 'title',
                tabIndex: -1,
                ariaRole: 'presentation',
                handler: me.selectToday,
                scope: me
            });
        }


        me.callParent();

        Ext.applyIf(me, {
            renderData: {}
        });

        Ext.apply(me.renderData, {
            dayNames: me.dayNames,
            showToday: me.showToday,
            prevText: encode(me.prevText),
            nextText: encode(me.nextText),
            clearText: encode(me.clearText),
            todayText: encode(me.todayText),
            ariaMinText: encode(me.ariaMinText),
            ariaMaxText: encode(me.ariaMaxText),
            ariaDisabledDaysText: encode(me.ariaDisabledDaysText),
            ariaDisabledDatesText: encode(me.ariaDisabledDatesText),
            days: days
        });

        me.protoEl.unselectable();
    },
    clearValue : function(){
        var me = this,
            btn = me.clearBtn;

            me.setValue('');
            me.fireEvent('select', me, '');
            me.onSelect();
        return me;
    },
    beforeDestroy: function() {
        var me = this;
        if (me.rendered) {
            Ext.destroy(
                me.keyNav,
                me.monthPicker,
                me.monthBtn,
                me.nextRepeater,
                me.prevRepeater,
                me.todayBtn,
                me.clearBtn,
                me.todayElSpan
            );
            delete me.textNodes;
            delete me.cells.elements;
        }
        me.callParent();
    },

    finishRenderChildren: function () {
        var me = this;
        me.callParent();
        me.clearBtn.finishRender();
    }

});