package com.company.cache;

import com.company.blocks.DataBlock;
import com.company.blocks.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a la cache de datos.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class DataCache {
    private List cache;
    private String name;

    public DataCache (String name){
        this.cache = new ArrayList<DataCacheBlock>();
        this.name = name;
        for (int i = 0; i < 4; i++) {
            List dataBlock = new ArrayList<Integer>();
            for (int j = 0; j < 4; j++) {
                dataBlock.add(-1);
            }
            DataBlock dataBlock1 = new DataBlock(dataBlock);
            DataCacheBlock dataCacheBlock = new DataCacheBlock(dataBlock1, State.I, -1);
            this.cache.add(i,dataCacheBlock);
        }
    }

    public void setBlockToCache(DataCacheBlock dataCacheBlock, int blockPosition){
        this.cache.add(blockPosition, dataCacheBlock);
    }

    public DataCacheBlock getBlockFromCache(int blockPosition){
        return (DataCacheBlock) this.cache.get(blockPosition);
    }


    @Override
    public String toString(){
        String cache = "-------------  CACHE " + name +"-------------------\n";
        for (int i = 0; i < this.cache.size(); i++) {
            DataCacheBlock dataCacheBlock = (DataCacheBlock) this.cache.get(i);
            DataBlock dataBlock= dataCacheBlock.getDataBlock();
            for (int j = 0; j < dataBlock.getWords().size(); j++) {
                cache += "  "+ dataBlock.getWord(j);
            }
            cache += " ("+ dataCacheBlock.getState()+") ";
            cache += "("+ dataCacheBlock.getLabel()+") ";
            cache += "| ";
        }
        return cache;
    }
}
