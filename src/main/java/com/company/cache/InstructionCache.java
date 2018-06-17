package com.company.cache;

import com.company.blocks.InstructionBlock;

import java.util.ArrayList;
import java.util.List;

public class InstructionCache {
    private List cache;

    public InstructionCache (){
        this.cache = new ArrayList<InstructionCacheBlock>();
    }

    public List getCache() {
        return cache;
    }

    public void setCache(List cache) {
        this.cache = cache;
    }

    public void setBlockToCache(InstructionBlock instructionBlock, int block){
        InstructionCacheBlock newBlock = new InstructionCacheBlock(instructionBlock, block);
        this.cache.add(block%4, newBlock);
    }

    public InstructionCacheBlock getBlockFromCache(int block){
        return (InstructionCacheBlock) this.cache.get(block%4);
    }
}
