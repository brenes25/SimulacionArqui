package com.company;

import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.cache.*;
import com.company.core.Core;
import com.company.core.Core1;
import com.company.threads.MainThread;

import java.util.*;
import java.util.concurrent.*;

/**
 * Corresponde al procesador de la simulación
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class Processor {

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

    private Core core1;
    private Core core0;

    private DataParser dataParser;

    public CyclicBarrier cyclicBarrier;

    private Semaphore instructionBus;
    private Semaphore dataBus;

    private Object lock1;

    private MainThread mainThread;

    private boolean isSlowRun;

    private Menu menu;

    public int contextQueueInitialSize;

    /**
     * Constructor
     */
    public Processor() {
        this.lock1 = new Object();
        this.mainMemory = new ArrayList<DataBlock>();
        this.fillMainMemory();
        this.instructionMemory = new ArrayList<InstructionBlock>();

        this.clock = 0;
        this.quantum = 0;
        this.instructionBus = new Semaphore(1, true);
        this.dataBus = new Semaphore(1, true);
        this.contextQueue  = new ArrayDeque<Context>();
        this.finishedContexts = new ArrayList<Context>();

        this.cyclicBarrier = new CyclicBarrier(3);

        this.dataParser = new DataParser(this);
        this.menu = new Menu(this.quantum, this.dataParser);
        this.isSlowRun = this.menu.userStart();
        this.dataCacheCore0 = new DataCache("0");
        this.dataCacheCore1 = new DataCache("1");
        this.instructionCacheCore0 = new InstructionCache();
        this.instructionCacheCore1 = new InstructionCache();

        this.contextQueueInitialSize = this.contextQueue.size();
        this.mainThread = new MainThread(this);
        Thread controllerThread = new Thread(mainThread);
        controllerThread.start();

        this.core1 = new Core1((Context) this.contextQueue.poll(), this,
                this.dataCacheCore0, this.dataCacheCore1,
                this.instructionCacheCore0,"1");
        this.core0 = new Core1((Context) this.contextQueue.poll(), this,
                this.dataCacheCore1, this.dataCacheCore0,
                this.instructionCacheCore1,"0");
    }

    /**
     * Imprime los valores de la memoria de instrucciones.
     */
    private void printInstructionMemory (){
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

    /**
     * Imprime los valores de la memoria principal.
     */
    public void printMainMemory (){

        System.out.println(" ----- Memoria Principal ----- ");
        int counter = 0;
        for (int i = 0; i < mainMemory.size(); i++) {
            DataBlock dataBlock = (DataBlock) mainMemory.get(i);
            List data = dataBlock.getWords();
            for (int j = 0; j < data.size(); j++) {
                System.out.print(data.get(j));
                if(data.get(j).toString().length() == 1)
                    System.out.print(" ");
                System.out.print(" ");
            }
            System.out.print("|  ");
            counter++;
            if(counter == 4){
                System.out.println();
                counter = 0;
            }
        }
        System.out.println(" ");
    }

    /**
     * Guarda un bloque en una posicion de memoria.
     * @param index posicion de memoria
     * @param dataBlock el bloque que se quiere guardar.
     */
    public void saveInMemory(int index,DataBlock dataBlock){
        DataBlock dataBlock1 = new DataBlock();
        Collections.copy(dataBlock1.getWords(), dataBlock.getWords());
        this.mainMemory.set(index,dataBlock1);
    }

    /**
     * Llena la memoria con bloques de memoria llenos de -1
     */
    private void fillMainMemory(){
        for (int i = 0; i < 24; i++) {
            this.mainMemory.add(new DataBlock());
        }
    }

    /**
     * Agrega un nuevo bloque de instrucciones a la memoria de instrucciones.
     * @param instructionBlock
     */
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

    public void setQuantum(int quantum) {
        this.quantum = quantum;
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

    public Core getCore1() {
        return core1;
    }

    public List getInstructionMemory() {
        return instructionMemory;
    }

    /**
     * Trae el siguiente contexto a ejecutar de la cola de contextos, y le asigna el quantum
     * @return el contexto con el quantum actualizado. 
     */
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

    public int getContextInitialQueueSize() {
        return contextQueueInitialSize;
    }

    public Core getCore0() {
        return core0;
    }

    public boolean isSlowRun() {
        return isSlowRun;
    }


}
