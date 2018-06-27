package com.company.cache;

import com.company.*;
import com.company.blocks.InstructionBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a la cache de instrucciones.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class InstructionCache {
    private List cache;

    public InstructionCache (){
       this.cache = new ArrayList<InstructionCacheBlock>();
        for (int i = 0; i < 4 ; i++) {
            List instructions = new ArrayList<Instruction>();
            for (int j = 0; j < 4; j++) {
                List instruction = new ArrayList<Integer>();
                for (int l = 0; l < 4; l++) {
                    instruction.add(-1);
                }
                Instruction instruction1 = new Instruction(instruction);
                instructions.add(j,instruction1);
            }
            InstructionBlock instructionBlock = new InstructionBlock(instructions);
            InstructionCacheBlock instructionCacheBlock = new InstructionCacheBlock(instructionBlock, -1);
            this.cache.add(i,instructionCacheBlock);
        }

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
