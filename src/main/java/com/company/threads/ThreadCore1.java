package com.company.threads;

import com.company.*;
import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.blocks.State;
import com.company.cache.*;
import com.company.core.*;

import java.util.concurrent.BrokenBarrierException;

public class ThreadCore1 implements Runnable {

    private Context context;
    private Core1 core1;

    public ThreadCore1(Context context, Core1 core1) {
        this.context = context;
        this.core1 = core1;
    }

    public void run() {
        while (!this.core1.getProcessor().isFinishAll()) {
            int instructionMemoryBlockPos = this.context.getPc() / 16;          //saca el numero de bloque
            int instructionCacheBlockPos = instructionMemoryBlockPos % 4;       //saca la posicion en cache
            //va y trae ese bloque de la cache
            InstructionCacheBlock instructionCacheBlock = this.core1.getInstructionCacheCore1().getBlockFromCache(instructionCacheBlockPos);

            //va y trae el bloque de memoria para hacer la comparacion
            InstructionBlock instructionBlock = (InstructionBlock) this.core1.getProcessor().getInstructionMemory().get(instructionMemoryBlockPos - 24);

            if (instructionCacheBlock.getLabel() != instructionMemoryBlockPos) {  //miss

                this.core1.askForInstructionBus();
                //resolver fallo.
                this.context.setStalled(true);
                this.core1.goToMemory();
                instructionCacheBlock.setInstructionBlock(instructionBlock);
                instructionCacheBlock.setLabel(instructionMemoryBlockPos);

                this.context.setStalled(false);
                this.core1.getProcessor().getInstructionBus().release(); //suelto el bus despues de resolver el fallo.
            }
            int instructionCacheWord = this.context.getPc() % 16;
            Instruction instruction = this.core1.getCacheInstruction(instructionBlock, instructionCacheWord);
            this.context.setPc(this.context.getPc() + 4);
            int opCode = this.core1.decodeInstruction(instruction, this.context); //decodificar y resolver instruction
            if (opCode == 35) {               //LW
                solveLW(instruction);
            } else if (opCode == 43) {          //SW
                solveSW(instruction);
            }
            this.context.setCurrentQuantum(this.context.getCurrentQuantum()-1);
            //fin de ciclo, espera al resto de hilos a llegar a este punto
            this.core1.changeCycle();
            //esperando a finalizar los cambios del tiempo 0
            this.core1.changeCycle();
        }

    }

    private void solveSW(Instruction instruction) {
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int cachePos = memoryPos % 4;
        int numBlock = memoryPos / 16;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore1 = this.core1.getCacheCore1().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore0 = this.core1.getCacheCore0().getBlockFromCache(cachePos); //obtener bloque de la otra cache

        if (dataCacheBlockCore1.getLabel() == numBlock) {
            // El bloque está en la caché
            if (dataCacheBlockCore1.getState().equals(State.M)) {   //Si el estado es modificado
                //Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

            } else if (dataCacheBlockCore1.getState().equals(State.C)) {  //Si el estado es compartido
                dataCacheBlockCore1.setState(State.M);
                this.tryToLockBlock(dataCacheBlockCore0); //intento bloquear el bus y luego el bloque de la otra cache
                //Voy a la otra cache a invalidar el bloque.
                if(dataCacheBlockCore0.getLabel() == numBlock){
                    dataCacheBlockCore0.setState(State.I);
                }
                // Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

            } else {   //Si el estado es invalido
                dataCacheBlockCore1.setState(State.M);
                this.tryToLockBlock(dataCacheBlockCore0); //intento bloquear el bus y luego el bloque de la otra cache

                //revisar etiqueta de la otra cache
                if (dataCacheBlockCore0.getLabel() == numBlock){
                    // El estado siempre va a estar modificado
                    // escribo el bloque modificado de la otra cache en memoria
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    dataBlock.setWords(dataCacheBlockCore1.getDataBlock().getWords());

                    //se invalida el bloque de la otra cache
                    dataCacheBlockCore0.setState(State.I);
                }

                //guardar bloque de memoria en la cache del nucleo 1
                this.core1.goToMemory();
                DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);

                //guardar bloque a cache
                dataCacheBlockCore1.setDataBlock(dataBlock);

                // Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

                //Libera los candados
                dataCacheBlockCore0.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();

            }
        }
        else{
            // el bloque no está en cache
            if (dataCacheBlockCore1.getState().equals(State.M)) {
                this.core1.goToMemory();
                // escribo el bloque modificado de la otra cache en memoria
                this.core1.getProcessor().saveInMemory(dataCacheBlockCore1.getLabel(),dataCacheBlockCore1.getDataBlock());
            }

            this.tryToLockBlock(dataCacheBlockCore0);
            this.core1.goToMemory();

            this.checkOtherCacheStatus(dataCacheBlockCore0,dataCacheBlockCore1,numBlock);
            DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);

            //guardar bloque a cache
            dataCacheBlockCore1.setDataBlock(dataBlock);

            // Ejecucion del store
            dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
            dataCacheBlockCore1.setLabel(numBlock);
            dataCacheBlockCore1.setState(State.M);

            //Libera los candados
            dataCacheBlockCore0.getCacheLock().release();
            this.core1.getProcessor().getDataBus().release();
        }
    }


    private void solveLW(Instruction instruction) {
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int cachePos = memoryPos % 4;
        int numBlock = memoryPos / 16;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore1 = this.core1.getCacheCore1().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore0 = this.core1.getCacheCore0().getBlockFromCache(cachePos);

        if (dataCacheBlockCore1.getLabel() == numBlock) {
            // El bloque está en la caché
            if (!dataCacheBlockCore1.getState().equals(State.I)) {
                //Estado es modificado o compartido
                this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));
            } else {
                //Estado es inválido, miss
                this.core1.goToMemory();

                this.tryToLockBlock(dataCacheBlockCore0);

                if (dataCacheBlockCore0.getLabel() == numBlock && dataCacheBlockCore0.getState().equals(State.M)) {
                    // La otra cache tiene el bloque también
                    // bloque está modificado
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    dataBlock.setWords(dataCacheBlockCore0.getDataBlock().getWords()); //guarda el bloque actualizado en memoria
                    dataCacheBlockCore0.setState(State.C);

                    //guardar bloque a cache
                    dataCacheBlockCore1.setDataBlock(dataBlock);
                    dataCacheBlockCore1.setState(State.C);
                    //Ejecucion del load
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));

                } else {
                    // La otra caché no tiene el bloque o lo tiene I/C
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);

                    //guardar bloque a cache
                    dataCacheBlockCore1.setDataBlock(dataBlock);
                    dataCacheBlockCore1.setState(State.C);

                    //Ejecucion del laod
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));
                }
                dataCacheBlockCore0.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();
            }

        } else {
            //el bloque no estaba
            if (dataCacheBlockCore1.getState().equals(State.M)) {
                this.core1.goToMemory();
                // escribo el bloque modificado de la otra cache en memoria
                this.core1.getProcessor().saveInMemory(dataCacheBlockCore1.getLabel(),dataCacheBlockCore1.getDataBlock());
            }
            this.tryToLockBlock(dataCacheBlockCore0);
            this.core1.goToMemory();

            this.checkOtherCacheStatus(dataCacheBlockCore0,dataCacheBlockCore1,numBlock);
            DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);

            //guardar bloque a cache
            dataCacheBlockCore1.setDataBlock(dataBlock);

            //guardar bloque a cache
            dataCacheBlockCore1.setDataBlock(dataBlock);
            dataCacheBlockCore1.setLabel(numBlock);
            dataCacheBlockCore1.setState(State.C);

            //Ejecucion del load
            this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));

            //Libera los candados
            dataCacheBlockCore0.getCacheLock().release();
            this.core1.getProcessor().getDataBus().release();
        }
    }

    public void tryToLockBlock(DataCacheBlock dataCacheBlock){
        boolean blockLocked;
        do {
            this.core1.askForDataBus();
            blockLocked = this.core1.getBlockLock(dataCacheBlock);  //pide candado sobre bloque de la otra cache.
            if (!blockLocked) {
                this.core1.getProcessor().getDataBus().release();
                this.core1.changeCycle();
            }
        } while (!blockLocked);
    }

    public void checkOtherCacheStatus(DataCacheBlock dataCacheBlockCore0, DataCacheBlock dataCacheBlockCore1, int numBlock){
        if (dataCacheBlockCore0.getLabel() == numBlock){
            // el bloque estaba en la otra cache
            if (dataCacheBlockCore0.getState().equals(State.M)){
                // escribo el bloque modificado de la otra cache en memoria
                DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                dataBlock.setWords(dataCacheBlockCore1.getDataBlock().getWords());
            }
            dataCacheBlockCore0.setState(State.I);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
