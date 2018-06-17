package com.company;

import java.util.List;

public class Instruction {
    private List instruction;

    public Instruction(List instruction) {
        this.instruction = instruction;
    }

    public List getInstruction() {
        return instruction;
    }

    public int getInstructionValue(int position){
        return (Integer) this.instruction.get(position);

    }
}
