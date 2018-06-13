package com.company.cache;

import com.company.cache.DataCacheBlock;

import java.util.ArrayList;
import java.util.List;

public class DataCache {
    private List cache;

    public DataCache (){
        this.cache = new ArrayList<DataCacheBlock>();
    }
}
