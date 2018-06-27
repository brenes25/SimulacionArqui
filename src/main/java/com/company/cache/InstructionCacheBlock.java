package com.company.cache;

import com.company.blocks.InstructionBlock;

/**
 * Corresponde a las entradas de la cache de instrucciones.
 * Tiene un bloque de cache de instrucciones y una etiqueta.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class InstructionCacheBlock {
    private InstructionBlock instructionBlock;
    private int label;

    public InstructionCacheBlock(){
        this.label = -1;
    }

    public InstructionCacheBlock(InstructionBlock instructionBlock, int label){
        this.instructionBlock = instructionBlock;
        this.label = label;
    }

    public InstructionBlock getWordFromInstructionCacheBlock(int word) {
        return (InstructionBlock) this.instructionBlock.getInstructions().get(word);

    }

    public int getLabel() {
        return label;
    }

    public void setInstructionBlock(InstructionBlock instructionBlock) {
        this.instructionBlock = instructionBlock;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public InstructionBlock getInstructionBlock() {
        return instructionBlock;
    }
}
