package com.company.core;

import com.company.Context;
import com.company.Instruction;
import com.company.Processor;
import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.blocks.State;
import com.company.cache.DataCache;
import com.company.cache.DataCacheBlock;
import com.company.cache.InstructionCache;
import com.company.threads.ThreadCore1;

import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;

public abstract class Core {

    Processor processor;
    private Context context;
    private int instructionCacheReservedPosition;

    private int dataCacheReservedPosition;

    public boolean bothThreadsFinished;

    public Core(Context context, Processor processor) {
        this.processor = processor;
        this.context = context;
        this.instructionCacheReservedPosition = -1;
        this.dataCacheReservedPosition = -1;
        this.bothThreadsFinished = false;
    }

    //TODO: algo pasa aca, agregue un if de prueba para que sirviera por el momento
    public Instruction getCacheInstruction(InstructionBlock instructionBlock, int word) {
        int newWord = word / 4;
        return (Instruction) instructionBlock.getInstructions().get(newWord);
    }

    public int decodeInstruction(Instruction instruction, Context context) {
        int opCode = (Integer) instruction.getInstruction().get(0);
        switch (opCode) {
            case 8:                 //DADDI
                context.setRegisterValue(instruction.getInstructionValue(2), context.getRegisterValue(instruction.getInstructionValue(1)) + instruction.getInstructionValue(3));                      //se le setea al registro
                break;                                                                                              //se devuelve el valor.
            case 32:                //DADD
                context.setRegisterValue(instruction.getInstructionValue(3), context.getRegisterValue(instruction.getInstructionValue(1)) + context.getRegisterValue(instruction.getInstructionValue(2)));                     //se le setea al registro
                break;
            case 34:                //DSUB
                context.setRegisterValue(instruction.getInstructionValue(3), context.getRegisterValue(instruction.getInstructionValue(1)) - context.getRegisterValue(instruction.getInstructionValue(2)));                    //se le setea al registro
                break;
            case 12:                //DMUL
                context.setRegisterValue(instruction.getInstructionValue(3), context.getRegisterValue(instruction.getInstructionValue(1)) * context.getRegisterValue(instruction.getInstructionValue(2)));
                break;
            case 14:                //DDIV
                context.setRegisterValue(instruction.getInstructionValue(3), context.getRegisterValue(instruction.getInstructionValue(1)) / context.getRegisterValue(instruction.getInstructionValue(2)));
                break;
            case 4: {                 //BEQZ
                if (context.getRegisterValue(instruction.getInstructionValue(1)) == 0) {
                    context.setPc(context.getPc() + 4 * instruction.getInstructionValue(3));
                }
                break;
            }
            case 5: {                //BNEZ
                if (context.getRegisterValue(instruction.getInstructionValue(1)) != 0) {
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
                context.setPc(context.getRegisterValue(instruction.getInstructionValue(1)));
                break;
            case 63:
                context.setDone(true);      //FIN
                System.out.println("termino el hilillo " + context.getId());
                break;
            default:
                break;
        }
        return opCode;

    }

    public void askForInstructionBus() {
        while (!this.processor.getInstructionBus().tryAcquire()) {     // pido el bus y mientras no lo agarro caigo en la barrera
            this.changeCycle();
        }
    }

    /**
     * Intento adquirir el el bus, si lo logro intento adquirir el candado de la posicion de la otra cache, si no se logra suelto todos
     *
     * @param dataCacheBlock
     */
    public void tryToLockBlock(DataCacheBlock dataCacheBlock) {
        boolean blockLocked;
        do {
            this.askForDataBus();
            blockLocked = this.getBlockLock(dataCacheBlock);  //pide candado sobre bloque de la otra cache.
            if (!blockLocked) {
                this.getProcessor().getDataBus().release();
                this.changeCycle();
            }
        } while (!blockLocked);
    }

    public void askForDataBus() {
        while (!this.processor.getDataBus().tryAcquire()) {     // pido el bus y mientras no lo agarro caigo en la barrera
            this.changeCycle();
        }
    }

    public boolean getBlockLock(DataCacheBlock dataCacheBlock) {
        return dataCacheBlock.getCacheLock().tryAcquire();
    }

    public void blockMyCachePos(DataCacheBlock dataCacheBlock){
        while(!dataCacheBlock.getCacheLock().tryAcquire()){
            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void goToMemory() {
        for (int i = 0; i < 40; i++) {
            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeCycle() {
        try {
            this.processor.cyclicBarrier.await();
            this.processor.cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void saveModifiedBlock(DataCacheBlock dataCacheBlockCore) {
        //el bloque de mi cache estaba modificado
        this.askForDataBus(); //pido bus
        this.goToMemory();
        // escribo el bloque modificado de la otra cache en memoria
        this.getProcessor().saveInMemory(dataCacheBlockCore.getLabel(), dataCacheBlockCore.getDataBlock());
        this.getProcessor().getDataBus().release(); //libero bus
    }

    /**
     * Guardar el bloque de la otra cache en caso de que este modificado.
     *
     * @param target
     * @param actual
     * @param numBlock
     */
    public boolean checkOtherCacheStatus(DataCacheBlock target, DataCacheBlock actual, int numBlock) {
        if (target.getLabel() == numBlock) {
            // el bloque estaba en la otra cache
            if (target.getState().equals(State.M)) {
                // escribo el bloque modificado de la otra cache en memoria
                DataBlock dataBlock = (DataBlock) this.getProcessor().getMainMemory().get(numBlock);
                DataBlock dataBlock1 = new DataBlock();

                Collections.copy(dataBlock1.getWords(), dataBlock.getWords());
                actual.setDataBlock(dataBlock1);
                return true;
            }
        }
        return false;
    }

    public abstract void checkStatus();

    public DataCache getCacheCore1() {
        return this.processor.getDataCacheCore1();
    }

//    public InstructionCache getInstructionCacheCore1() {
//        return this.processor.getInstructionCacheCore1();
//    }

    public DataCache getCacheCore0() {
        return this.processor.getDataCacheCore0();
    }

    public InstructionCache getInstructionCacheCore0() {
        return this.processor.getInstructionCacheCore0();
    }

    public Processor getProcessor() {
        return processor;
    }

    public int getInstructionCacheReservedPosition() {
        return instructionCacheReservedPosition;
    }

    public void setInstructionCacheReservedPosition(int instructionCacheReservedPosition) {
        this.instructionCacheReservedPosition = instructionCacheReservedPosition;
    }

    public int getDataCacheReservedPosition() {
        return dataCacheReservedPosition;
    }

    public void setDataCacheReservedPosition(int dataCacheReservedPosition) {
        this.dataCacheReservedPosition = dataCacheReservedPosition;
    }

    public boolean isBothThreadsFinished() {
        return bothThreadsFinished;
    }

    public Context getContext() {
        return context;
    }

}
