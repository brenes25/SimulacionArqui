package com.company.core;

import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;

import java.util.ArrayList;
import java.util.List;

public abstract class Core {
    private List instructionsCache;
    private List dataCache;

    public Core() {
        this.instructionsCache = new ArrayList<InstructionBlock>();
        this.dataCache = new ArrayList<DataBlock>();
    }

    public List getInstructionsCache() {
        return instructionsCache;
    }

    public List getDataCache() {
        return dataCache;
    }
}
