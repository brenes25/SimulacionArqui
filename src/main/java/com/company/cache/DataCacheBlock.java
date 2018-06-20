package com.company.cache;

import com.company.blocks.DataBlock;
import com.company.blocks.State;

public class DataCacheBlock {

    private DataBlock dataBlock;
    private int label;
    private State state;


    public DataCacheBlock(DataBlock dataBlock, State state,int label){
        this.dataBlock = dataBlock;
        this.label = label;
        this.state = state;


    }

    public int getWordFromBlock(int word){
        return this.dataBlock.getWord(word);
    }

    public int getLabel() {
        return label;
    }

    public State getState() {
        return state;
    }
}
