package com.company.cache;

import java.util.ArrayList;
import java.util.List;

public class InstructionCache {
    private List cache;

    public InstructionCache (){
        this.cache = new ArrayList<InstructionCacheBlock>();
    }
}