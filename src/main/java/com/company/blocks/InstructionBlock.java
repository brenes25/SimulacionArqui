package com.company.blocks;

import com.company.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a un bloque en la cache de instrucciones
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class InstructionBlock {

    private List instructions;

    public  InstructionBlock(){
        this.instructions = new ArrayList<Instruction>();
    }

    public InstructionBlock(List<Instruction> instructions) {
        this.instructions = instructions;
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
