package com.company.core;

import com.company.cache.DataCache;
import com.company.cache.InstructionCache;

public abstract class Core {

    private DataCache dataCache;
    private InstructionCache instructionCache;

    public DataCache getDataCache() {
        return dataCache;
    }

    public InstructionCache getInstructionCache() {
        return instructionCache;
    }

}
