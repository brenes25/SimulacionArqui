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
        while (true) {
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
                instructionCacheBlock.setInstructionBlock(instructionBlock);
                instructionCacheBlock.setLabel(instructionMemoryBlockPos);

                this.core1.goToMemory();

                this.context.setStalled(false);
                this.core1.getProcessor().getInstructionBus().release(); //suelto el bus despues de resolver el fallo.
            }
            int instructionCacheWord = this.context.getPc() % 16;
            Instruction instruction = this.core1.getCacheInstruction(instructionBlock, instructionCacheWord);
            this.context.setPc(this.context.getPc() + 4);
            int opCode = this.core1.decodeInstruction(instruction, this.context); //decodificar y resolver instruction
            if (opCode == 35) {               //LW
                solveLW(instruction);
            } else if (opCode == 45) {          //SW
                solveSW(instruction);
            }
            //fin de ciclo, espera al resto de hilos a llegar a este punto
            this.core1.changeCycle();
            //esperando a finalizar los cambios del tiempo 0
            this.core1.changeCycle();
        }

    }

    private void solveSW(Instruction instruction) {

    }


    private void solveLW(Instruction instruction) {
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int cachePos = memoryPos % 4;
        int numBlock = memoryPos / 16;
        DataCacheBlock dataCacheBlockCore1 = this.core1.getCacheCore1().getBlockFromCache(cachePos);

        if (dataCacheBlockCore1.getLabel() == numBlock) {
            // El bloque está en la caché
            if (!dataCacheBlockCore1.getState().equals(State.I)) {
                //Estado es modificado o compartido
                int word = (memoryPos % 16) / 4;
                this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore1.getWordFromBlock(word));
            } else {
                //Estado es inválido, miss
                this.core1.goToMemory();

                DataCacheBlock dataCacheBlockCore0 = this.core1.getCacheCore0().getBlockFromCache(cachePos);
                boolean blockLocked = false;
                do {
                    this.core1.askForDataBus();
                    blockLocked = this.core1.getBlockLock(dataCacheBlockCore0);  //pide candado sobre bloque de la otra cache.
                    if (!blockLocked) {
                        this.core1.getProcessor().getDataBus().release();
                        this.core1.changeCycle();
                    }
                } while (!blockLocked);

                if (dataCacheBlockCore0.getLabel() == numBlock && dataCacheBlockCore0.getState().equals(State.M)) {
                    // La otra cache tiene el bloque también
                    // bloque está modificado
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    dataBlock.setWords(dataCacheBlockCore0.getDataBlock().getWords()); //guarda el bloque actualizado en memoria
                    dataCacheBlockCore0.setState(State.C);

                    dataCacheBlockCore1.setDataBlock(dataBlock);
                    dataCacheBlockCore1.setState(State.C);

                } else {
                    // La otra caché no tiene el bloque o lo tiene I/C
                    DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
                    dataCacheBlockCore1.setDataBlock(dataBlock);
                    dataCacheBlockCore1.setState(State.C);
                }
                dataCacheBlockCore0.getCacheLock().release();
                this.core1.getProcessor().getDataBus().release();
            }

        } else {
            //el bloque no estaba
            this.core1.goToMemory();
            DataBlock dataBlock = (DataBlock) this.core1.getProcessor().getMainMemory().get(numBlock);
            dataCacheBlockCore1.setDataBlock(dataBlock);
            dataCacheBlockCore1.setState(State.C);
        }
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
