package com.company.cache;

import com.company.blocks.DataBlock;
import com.company.blocks.State;

import java.util.concurrent.Semaphore;

/**
 * Corresponde a las entradas de la cache de datos.
 * Tiene un bloque de cache de datos, una etiqueta y un estado. Ademas un semaforo para poder bloquearla.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
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
