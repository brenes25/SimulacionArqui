package com.company;

import java.util.List;

/**
 * Corresponde a una instruccion en la simulacion, tiene el codigo de la instruccion y los valores para resolverla/
 */
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
