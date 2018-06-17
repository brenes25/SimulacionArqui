package com.company.core;

import com.company.Context;
import com.company.Instruction;
import com.company.blocks.InstructionBlock;
import com.company.cache.DataCache;
import com.company.cache.InstructionCache;

public abstract class Core {

    protected DataCache dataCache;
    protected InstructionCache instructionCache;

    public DataCache getDataCache() {
        return dataCache;
    }

    public InstructionCache getInstructionCache() {
        return instructionCache;
    }

    public Instruction getCacheInstruction(InstructionBlock instructionBlock, int word) {
        int newWord = word / 4;
        return (Instruction) instructionBlock.getInstructions().get(newWord);
    }

    public int decodeInstruction(Instruction instruction, Context context) {
        int opCode = (Integer)instruction.getInstruction().get(0) ;
        switch(opCode){
            case 8:                 //DADDI
                context.setRegisterValue(instruction.getInstructionValue(2),  instruction.getInstructionValue(1)+ instruction.getInstructionValue(3));                      //se le setea al registro
                break;                                                                                              //se devuelve el valor.
            case 32:                //DADD
                context.setRegisterValue(instruction.getInstructionValue(3),  instruction.getInstructionValue(1)+ instruction.getInstructionValue(2));                     //se le setea al registro
                break;
            case 34:                //DSUB
                context.setRegisterValue(instruction.getInstructionValue(3),  instruction.getInstructionValue(1) - instruction.getInstructionValue(2));                    //se le setea al registro
                break;
            case 12:                //DMUL
                context.setRegisterValue(instruction.getInstructionValue(3),  instruction.getInstructionValue(1) * instruction.getInstructionValue(2));
                break;
            case 14:                //DDIV
                context.setRegisterValue(instruction.getInstructionValue(3),  instruction.getInstructionValue(1) / instruction.getInstructionValue(2));
                break;
            case 4: {                 //BEQZ
                if (instruction.getInstructionValue(0) == 0) {
                    context.setPc(context.getPc() + 4 * instruction.getInstructionValue(3));
                }
                break;
            }
            case 5: {                //BNEZ
                if(instruction.getInstructionValue(0) != 0){
                    context.setPc(context.getPc() + 4 * instruction.getInstructionValue(3));
                }
                break;
            }
            case 3: {                 //JAL
                context.setRegisterValue(31, context.getPc());
                context.setPc(context.getPc() + instruction.getInstructionValue(3));
                break;
            }
            case 2:                 //JR
                context.setPc(instruction.getInstructionValue(1));
                break;
            case 63:
                context.setDone(true);      //FIN
                break;
            default:
                break;
        }
        return opCode;

    }

}
