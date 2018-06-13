package com.company;

import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.core.Core;
import com.company.core.Core0;
import com.company.core.Core1;

import java.util.*;

public class Processor {

    private static final String PATH = "./src/main/java/resources/";

    private List mainMemory;
    private List instructionMemory;
    private int clock;
    private int quantum;
    private Queue contextQueue;
    private List finishedContexts;
    private Core core0;
    private Core core1;
    private boolean start;
    private DataParser dataParser;

    public Processor(){
        this.mainMemory = new ArrayList<DataBlock>();
        this.instructionMemory = new ArrayList<InstructionBlock>();
        this.start = true;
        this.clock = 0;
        this.quantum = 0;
        this.contextQueue  = new ArrayDeque<Context>();
        this.finishedContexts = new ArrayList<Context>();
        dataParser = new DataParser(this);
        this.start();
        this.core0 = new Core0();
        this.core1 = new Core1();
    }

    public void start(){
        Scanner input = new Scanner (System.in);
        while(start){
            System.out.println("Cual hilillo quiere agregar en el sistema");
            this.dataParser.parseFile(PATH + input.nextLine());
            this.printInstructionMemory();
            System.out.println("Desea agregar mas hilillos?");
            if(input.nextLine().equals("no"))
                this.start = false;
        }
        System.out.println("Cuantos ciclos de reloj quiere que dure el quantum");
        this.quantum = Integer.parseInt(input.nextLine());
    }

    public void addNewInstruction(InstructionBlock instructionBlock){
        this.instructionMemory.add(instructionBlock);
    }

    public void printInstructionMemory (){
        for (int i = 0; i < instructionMemory.size(); i++) {
            InstructionBlock instructionBlock = (InstructionBlock) instructionMemory.get(i);
            List instructionBlockList = instructionBlock.getInstructions();
            for (int j = 0; j < instructionBlockList.size(); j++) {
                Instruction instruction = (Instruction) instructionBlockList.get(j);
                List instructionList = instruction.getInstruction();
                for (int k = 0; k < instructionList.size(); k++) {
                    System.out.print(instructionList.get(k) + " ");
                }
                System.out.print(" - ");
            }
            System.out.println();
        }
    }

    public List getMainMemory() {
        return mainMemory;
    }

    public int getClock() {
        return clock;
    }

    public int getQuantum() {
        return quantum;
    }

    public Queue getContextQueue() {
        return contextQueue;
    }

    public List getFinishedContexts() {
        return finishedContexts;
    }

    public Core getCore0() {
        return core0;
    }

    public Core getCore1() {
        return core1;
    }

    public List getInstructionMemory() {
        return instructionMemory;
    }
}
