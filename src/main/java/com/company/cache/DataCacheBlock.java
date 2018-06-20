package com.company.cache;

import com.company.blocks.DataBlock;
import com.company.blocks.State;

import java.util.concurrent.Semaphore;

public class DataCacheBlock {

    private DataBlock dataBlock;
    private int label;
    private State state;
    private Semaphore cacheLock;


    public DataCacheBlock(DataBlock dataBlock, State state,int label){
        this.dataBlock = dataBlock;
        this.label = label;
        this.state = state;
        this.cacheLock = new Semaphore(1);
    }

    public int getWordFromBlock(int word){
        return this.dataBlock.getWord(word);
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public DataBlock getDataBlock() {
        return dataBlock;
    }

    public void setDataBlock(DataBlock dataBlock) {
        this.dataBlock = dataBlock;
    }

    public Semaphore getCacheLock() {
        return cacheLock;
    }


}
