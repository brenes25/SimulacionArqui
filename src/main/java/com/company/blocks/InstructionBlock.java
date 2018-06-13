package com.company.blocks;

import com.company.Instruction;

import java.util.ArrayList;
import java.util.List;

public class InstructionBlock {

    private List instructions;

    public InstructionBlock() {
        this.instructions = new ArrayList<Instruction>();
    }

    public List getInstructions() {
        return instructions;
    }

}
