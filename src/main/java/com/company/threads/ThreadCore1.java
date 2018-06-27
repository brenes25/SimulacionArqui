package com.company.threads;

import com.company.*;
import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.blocks.State;
import com.company.cache.*;
import com.company.core.*;

import java.util.Collections;

public class ThreadCore1 implements Runnable {

    private Context context;
    private Core1 core1;

    public ThreadCore1(Context context, Core1 core1) {
        this.context = context;
        this.core1 = core1;

    }

    public void run() {
        while (this.core1.getProcessor().getContextInitialQueueSize() != this.core1.getProcessor().getFinishedContexts().size()){
            if(this.context != null){
                int instructionMemoryBlockPos = this.context.getPc() / 16;          //saca el numero de bloque
                int instructionCacheBlockPos = instructionMemoryBlockPos % 4;       //saca la posicion en cache
                //va y trae ese bloque de la cache de instrucciones
                InstructionCacheBlock instructionCacheBlock = this.core1.getMyInstructionCache().getBlockFromCache(instructionCacheBlockPos);

                if (instructionCacheBlock.getLabel() != instructionMemoryBlockPos) {  //miss

                    this.core1.askForInstructionBus();
                    //resolver fallo.
                    this.context.setStalled(true);
                    this.core1.goToMemory();
                    //trae bloque de instruccion de memoria.
                    InstructionBlock instructionBlock = (InstructionBlock) this.core1.getProcessor().getInstructionMemory().get(instructionMemoryBlockPos - 24);
                    instructionCacheBlock.setInstructionBlock(instructionBlock);
                    instructionCacheBlock.setLabel(instructionMemoryBlockPos);

                    this.context.setStalled(false);
                    this.core1.getProcessor().getInstructionBus().release(); //suelto el bus despues de resolver el fallo.
                }

                int instructionCacheWord = this.context.getPc() % 16;
                //se trae el bloque de cache de instrucciones
                Instruction instruction = this.core1.getCacheInstruction(instructionCacheBlock.getInstructionBlock(), instructionCacheWord);
                this.context.setPc(this.context.getPc() + 4);
                int opCode = this.core1.decodeInstruction(instruction, this.context); //decodificar y resolver instruction
                if (opCode == 35) {               //LW
                    this.solveLW(instruction);
                } else if (opCode == 43) {          //SW
                    this.solveSW(instruction);
                }
                this.context.setCurrentQuantum(this.context.getCurrentQuantum()-1);
                //fin de ciclo, espera al resto de hilos a llegar a este punto
                this.core1.changeCycle();
            }
            else
            {
                this.core1.changeCycle();
            }

        }

    }

    private void solveSW(Instruction instruction) {
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int numBlock = memoryPos / 16;
        int cachePos = numBlock % 4;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore1 = this.core1.getMyDataCache().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore0 = this.core1.getOtherDataCache().getBlockFromCache(cachePos); //obtener bloque de la otra cache

        if (dataCacheBlockCore1.getLabel() == numBlock) {
            // El bloque está en la caché
            if (dataCacheBlockCore1.getState().equals(State.M)) {   //Si el estado es modificado
                //Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

            } else if (dataCacheBlockCore1.getState().equals(State.C)) {  //Si el estado es compartido

                this.core1.tryToLockBlock(dataCacheBlockCore0,dataCacheBlockCore1); //intento bloquear el bus y luego el bloque de la otra cache

//                // Bloquear mi cache
//                this.core1.blockMyCachePos(dataCacheBlockCore1);
                dataCacheBlockCore1.setState(State.M);

                //Voy a la otra cache a invalidar el bloque.
                if(dataCacheBlockCore0.getLabel() == numBlock)
                    dataCacheBlockCore0.setState(State.I);

                dataCacheBlockCore0.getCacheLock().release();
                // Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
                //libero candados
                dataCacheBlockCore1.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();

            } else {   //Si el estado es invalido

                this.core1.tryToLockBlock(dataCacheBlockCore0,dataCacheBlockCore1); //intento bloquear el bus y luego el bloque de la otra cache
//                // Bloquear mi cache
//                this.core1.blockMyCachePos(dataCacheBlockCore1);

                dataCacheBlockCore1.setState(State.M);
                this.context.setStalled(true);
                //guarda bloque de memoria en la cache del nucleo 1
                this.core1.goToMemory();

                //revisar etiqueta de la otra cache
                if (dataCacheBlockCore0.getLabel() == numBlock){
                    // El estado siempre va a estar modificado
                    // escribo el bloque modificado de la otra cache en memoria
                    this.core1.getProcessor().saveInMemory(numBlock, dataCacheBlockCore0.getDataBlock());

                    //se invalida el bloque de la otra cache
                    dataCacheBlockCore0.setState(State.I);
                }
                dataCacheBlockCore0.getCacheLock().release(); //libera candado de la otra cache

                DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                DataBlock dataBlock1 = new DataBlock();
                Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

                //guarda bloque a cache
                dataCacheBlockCore1.setDataBlock(dataBlock1);

                // Ejecucion del store
                dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
                this.context.setStalled(false);
                //Libera los candados
                dataCacheBlockCore1.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();
            }
        }
        else{
            // el bloque no está en cache
            if (dataCacheBlockCore1.getState().equals(State.M)) {
                //mi cache tiene un bloque en estado modificado
                this.core1.saveModifiedBlock(dataCacheBlockCore1);
            }
            this.core1.tryToLockBlock(dataCacheBlockCore0,dataCacheBlockCore1);
//            // Bloquear mi cache
//            this.core1.blockMyCachePos(dataCacheBlockCore1);
            this.core1.goToMemory();
            this.context.setStalled(true);
            //revisa si el bloque de la otra cache estaba modificado y si estaba modificado lo guarda a memoria
            if(this.core1.checkOtherCacheStatus(dataCacheBlockCore0,dataCacheBlockCore1,numBlock))
                dataCacheBlockCore0.setState(State.I);

            dataCacheBlockCore0.getCacheLock().release();
            //sacar el bloque de memoria
            DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
            DataBlock dataBlock1 = new DataBlock();
            Collections.copy(dataBlock1.getWords(), dataBlock.getWords());
            //guardar bloque a cache
            dataCacheBlockCore1.setDataBlock(dataBlock1);

            // Ejecucion del store
            dataCacheBlockCore1.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
            dataCacheBlockCore1.setLabel(numBlock);
            dataCacheBlockCore1.setState(State.M);

            this.context.setStalled(false);

            //Libera los candados
            dataCacheBlockCore1.getCacheLock().release();

            this.core1.getProcessor().getDataBus().release();
        }
    }


    private void solveLW(Instruction instruction) {
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int numBlock = memoryPos / 16;
        int cachePos = numBlock % 4;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore1 = this.core1.getMyDataCache().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore0 = this.core1.getOtherDataCache().getBlockFromCache(cachePos);

        if (dataCacheBlockCore1.getLabel() == numBlock) {
            // El bloque está en la caché
            if (!dataCacheBlockCore1.getState().equals(State.I)) {
                //Estado es modificado o compartido, resuelvo el load
                this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));
            } else {
                //Estado es inválido, miss
                this.core1.tryToLockBlock(dataCacheBlockCore0,dataCacheBlockCore1);
//                // Bloquear mi cache
//                this.core1.blockMyCachePos(dataCacheBlockCore1);
                this.context.setStalled(true);
                this.core1.goToMemory();

                if (dataCacheBlockCore0.getLabel() == numBlock && dataCacheBlockCore0.getState().equals(State.M)) {
                    // La otra cache tiene el bloque también y el bloque está modificado
                    //guarda el bloque actualizado en memoria
                    this.core1.getProcessor().saveInMemory(numBlock, dataCacheBlockCore0.getDataBlock());
                    dataCacheBlockCore0.setState(State.C);

                    //guarda bloque a cache
                    DataBlock dataBlock1 = new DataBlock();
                    DataBlock dataBlock2 = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    Collections.copy(dataBlock1.getWords(),  dataBlock2.getWords());
                    dataCacheBlockCore1.setDataBlock(dataBlock1);
                    dataCacheBlockCore1.setState(State.C);
                    //Ejecucion del load
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));

                } else {
                    // La otra caché no tiene el bloque o lo tiene I/C
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    DataBlock dataBlock1 = new DataBlock();
                    Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

                    //guardar bloque a cache
                    dataCacheBlockCore1.setDataBlock(dataBlock1);
                    dataCacheBlockCore1.setState(State.C);

                    //Ejecucion del load
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));

                }
                this.context.setStalled(false);
                dataCacheBlockCore1.getCacheLock().release();
                dataCacheBlockCore0.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();
            }

        } else {
            //el bloque no estaba en mi cache
            if (dataCacheBlockCore1.getState().equals(State.M)) {
                this.core1.saveModifiedBlock(dataCacheBlockCore1);
            }
            // el bloque no estaba modificado
            this.core1.tryToLockBlock(dataCacheBlockCore0,dataCacheBlockCore1);
//            // Bloquear mi cache
//            this.core1.blockMyCachePos(dataCacheBlockCore1);
            this.context.setStalled(true);
            this.core1.goToMemory();

            //revisa si el bloque de la otra cache estaba modificado
            if (this.core1.checkOtherCacheStatus(dataCacheBlockCore0,dataCacheBlockCore1,numBlock))
                dataCacheBlockCore0.setState(State.C);

            dataCacheBlockCore0.getCacheLock().release();//libero cache del otro nucleo
            DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
            DataBlock dataBlock1 = new DataBlock();
            Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

            //guarda bloque a cache
            dataCacheBlockCore1.setDataBlock(dataBlock1);
            dataCacheBlockCore1.setLabel(numBlock);
            dataCacheBlockCore1.setState(State.C);

            //Ejecucion del load
            this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));
            this.context.setStalled(false);
            //Libera los candados
            dataCacheBlockCore1.getCacheLock().release();
            this.core1.getProcessor().getDataBus().release();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
