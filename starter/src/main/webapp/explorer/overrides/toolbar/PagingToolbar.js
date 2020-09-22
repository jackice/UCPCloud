Ext.define('admin.overrides.Paging', {
    override: 'Ext.toolbar.Paging',

    onLoad : function(){
        var me = this,
            pageData,
            currPage,
            pageCount,
            afterText,
            count,
            isEmpty,
            item;

        count = me.store.getCount();
        isEmpty = count === 0;
        if(isEmpty && me.store.getTotalCount()>0){
                me.store.previousPage();
            return;
        }

        if (!isEmpty) {
            pageData = me.getPageData();
            currPage = pageData.currentPage;
            pageCount = pageData.pageCount;

            // Check for invalid current page.
            if (currPage > pageCount) {
                // If the surrent page is beyond the loaded end,
                // jump back to the loaded end if there is a valid page count.
                if (pageCount > 0) {
                    me.store.loadPage(pageCount);
                }
                // If no pages, reset the page field.
                else {
                    me.getInputItem().reset();
                }
                return;
            }

            afterText = Ext.String.format(me.afterPageText, isNaN(pageCount) ? 1 : pageCount);
        } else {
            currPage = 0;
            pageCount = 0;
            afterText = Ext.String.format(me.afterPageText, 0);
        }

        Ext.suspendLayouts();
        item = me.child('#afterTextItem');
        if (item) {
            item.setText(afterText);
        }
        item = me.getInputItem();
        if (item) {
            item.setDisabled(isEmpty).setValue(currPage);
        }
        me.setChildDisabled('#first', currPage === 1 || isEmpty);
        me.setChildDisabled('#prev', currPage === 1 || isEmpty);
        me.setChildDisabled('#next', currPage === pageCount  || isEmpty);
        me.setChildDisabled('#last', currPage === pageCount  || isEmpty);
        me.setChildDisabled('#refresh', false);
        me.updateInfo();
        Ext.resumeLayouts(true);


        if (!me.calledInternal) {
            me.fireEvent('change', me, pageData || me.emptyPageData);
        }
    }

});