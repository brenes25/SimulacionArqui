package com.company.blocks;

import com.company.Instruction;

import java.util.ArrayList;
import java.util.List;

public class InstructionBlock {

    private List instructions;

    public InstructionBlock() {
        this.instructions = new ArrayList<Instruction>();
    }

    public void addInstruction(Instruction instruction, int word){
        this.instructions.add(word, instruction);
    }

    public Instruction getInstruction(int word){
        return (Instruction) this.instructions.get(word);
    }

    public List getInstructions() {
        return instructions;
    }

}
