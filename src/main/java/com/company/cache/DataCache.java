package com.company.cache;

import com.company.blocks.DataBlock;
import com.company.blocks.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a la caché de datos.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class DataCache {
    private List cache;
    private String name;

    /**
     * Constructor
     */
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
        String cache = " -----  Cache de datos Core " + name +" ----- \n";
        for (int i = 0; i < this.cache.size(); i++) {
            DataCacheBlock dataCacheBlock = (DataCacheBlock) this.cache.get(i);
            DataBlock dataBlock= dataCacheBlock.getDataBlock();
            String data;
            for (int j = 0; j < dataBlock.getWords().size(); j++) {
                data = String.valueOf(dataBlock.getWord(j));
                if(data.length() == 1)
                    data += " ";
                cache += " " + data;
            }
            cache += " ("+ dataCacheBlock.getState()+") ";
            cache += "("+ dataCacheBlock.getLabel()+") ";
            cache += "| ";
        }
        cache += "\n";
        return cache;
    }
}
