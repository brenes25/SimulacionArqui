package com.company.cache;

import com.company.blocks.InstructionBlock;

public class InstructionCacheBlock {
    private InstructionBlock instructionBlock;
    private int label;

    public InstructionCacheBlock(){
        this.label = -1;
    }

    public InstructionBlock getInstructionBlock() {
        return instructionBlock;
    }

    public int getLabel() {
        return label;
    }

}
