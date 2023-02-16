package com.zwn.launcher.data.model;

import com.zeewain.base.model.LoadState;

public class PagedPrdListLoadState extends ProductListLoadState{
    public int pageNum;

    public PagedPrdListLoadState(String categoryId, int pageNum, LoadState loadState) {
        super(categoryId, loadState);
        this.pageNum = pageNum;
    }
}
