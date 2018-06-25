package com.company;

import com.company.blocks.InstructionBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataParser {

    private static final int INSTRUCTION_MEMORY_START = 384;
    private static final int INSTRUCTION_BLOCK_SIZE = 16;
    private Processor processor;
    private int id;

    public DataParser(Processor processor) {
        this.processor = processor;
        id = 0;
    }

    public void parseFile(String filename) {
        BufferedReader br = null;
        FileReader fr = null;

        int instructionCounter = 0;
        int pc = (this.processor.getInstructionMemory().size() * INSTRUCTION_BLOCK_SIZE) + INSTRUCTION_MEMORY_START;
        InstructionBlock instructionBlock = new InstructionBlock();
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
            String sCurrentLine;
            do{
                sCurrentLine = br.readLine();
                if (instructionCounter == 4 || sCurrentLine == null) {
                    this.processor.addNewInstruction(instructionBlock);
                    instructionBlock = new InstructionBlock();
                    instructionCounter = 0;
                }
                if (sCurrentLine != null){
                    List instructionList = new ArrayList<Integer>();
                    String currentLine[];
                    currentLine = sCurrentLine.split("\\s+");

                    for (int i = 0; i < currentLine.length; i++) {
                        instructionList.add(Integer.parseInt(currentLine[i]));
                    }
                    Instruction instruction = new Instruction(instructionList);
                    instructionBlock.getInstructions().add(instruction);
                    instructionCounter++;
                }
            }while (sCurrentLine != null);
            Context newContext = new Context(pc,this.processor.getQuantum());
            newContext.setId(id);
            id++;
            this.processor.getContextQueue().add(newContext);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
