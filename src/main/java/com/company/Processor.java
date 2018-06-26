package com.company;

import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.cache.*;
import com.company.core.Core;
import com.company.core.Core0;
import com.company.core.Core1;
import com.company.threads.MainThread;

import java.util.*;
import java.util.concurrent.*;

public class Processor {

    private static final String PATH = "./src/main/java/resources/";

    private DataCache dataCacheCore0;
    private InstructionCache instructionCacheCore0;

    private DataCache dataCacheCore1;
    private InstructionCache instructionCacheCore1;

    private List mainMemory;
    private List instructionMemory;
    private int clock;
    private int quantum;
    private Queue contextQueue;
    private List finishedContexts;

    private Core core0;
    private Core core1;
    private Core core12;

    private boolean start;
    private DataParser dataParser;

    public CyclicBarrier cyclicBarrier;
    //public boolean bothCoresFinished;

    private Semaphore instructionBus;
    private Semaphore dataBus;

    private Object lock1;

    private MainThread mainThread;

    public int contextQueueInitialSize;

    public Processor() {
        lock1 = new Object();
        this.mainMemory = new ArrayList<DataBlock>();
        this.fillMainMemory();
        this.instructionMemory = new ArrayList<InstructionBlock>();
        this.start = true;

        //this.bothCoresFinished = false;

        this.clock = 0;
        this.quantum = 0;
        this.instructionBus = new Semaphore(1, true);
        this.dataBus = new Semaphore(1, true);
        this.contextQueue  = new ArrayDeque<Context>();
        this.finishedContexts = new ArrayList<Context>();

        this.cyclicBarrier = new CyclicBarrier(3);

        this.dataParser = new DataParser(this);
        this.dataCacheCore0 = new DataCache();
        this.dataCacheCore1 = new DataCache();
        this.instructionCacheCore0 = new InstructionCache();
        this.instructionCacheCore1 = new InstructionCache();
        this.userStart();
        this.contextQueueInitialSize = this.contextQueue.size();
        this.mainThread = new MainThread(this);
        Thread controllerThread = new Thread(mainThread);
        controllerThread.start();

        //this.core0 = new Core0((Context) this.contextQueue.poll(), this);
        this.core1 = new Core1((Context) this.contextQueue.poll(), this,this.dataCacheCore0,this.instructionCacheCore0,"1");
        this.core12 = new Core1((Context) this.contextQueue.poll(), this,this.dataCacheCore1,this.instructionCacheCore1,"2");
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

    public void printMainMemory (){
        System.out.println("----------- SOY LA MEMORIA!!!! -----------");
        for (int i = 0; i < mainMemory.size(); i++) {
            DataBlock dataBlock = (DataBlock) mainMemory.get(i);
            List data = dataBlock.getWords();
            for (int j = 0; j < data.size(); j++) {
                System.out.print(data.get(j));
                System.out.print(" - ");
            }
            System.out.println();
        }
    }

    public void saveInMemory(int index,DataBlock dataBlock){
        DataBlock dataBlock1 = new DataBlock();
        Collections.copy(dataBlock1.getWords(), dataBlock.getWords());
        this.mainMemory.set(index,dataBlock1);
    }

    public void userStart(){
        Scanner input = new Scanner (System.in);
        System.out.println("Cuantos ciclos de reloj quiere que dure el quantum");
        this.quantum = Integer.parseInt(input.nextLine());
        while(start){
            System.out.println("Cual hilillo quiere agregar en el sistema");
            this.dataParser.parseFile(PATH + input.nextLine());
            this.printInstructionMemory();
            System.out.println("Desea agregar mas hilillos?");
            if(input.nextLine().equals("no"))
                this.start = false;
        }
    }

    private void fillMainMemory(){
        for (int i = 0; i < 24; i++) {
            this.mainMemory.add(new DataBlock());
        }
    }

    public void addNewInstruction(InstructionBlock instructionBlock){
        this.instructionMemory.add(instructionBlock);
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
        synchronized (lock1) {
            return contextQueue;
        }
    }

    public List getFinishedContexts() {
        synchronized (lock1) {
            return finishedContexts;
        }
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

    public Context getNextContext(){
        Context context = (Context) this.contextQueue.poll();
        context.setCurrentQuantum(this.quantum);
        return context;
    }

    public Semaphore getInstructionBus() {
        return instructionBus;
    }

    public Semaphore getDataBus() {
        return dataBus;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public DataCache getDataCacheCore0() {
        return dataCacheCore0;
    }

    public InstructionCache getInstructionCacheCore0() {
        return instructionCacheCore0;
    }

    public DataCache getDataCacheCore1() {
        return dataCacheCore1;
    }

    public InstructionCache getInstructionCacheCore1() {
        return instructionCacheCore1;
    }

    public int getContextInitialQueueSize() {
        return contextQueueInitialSize;
    }

    public Core getCore12() {
        return core12;
    }
}
