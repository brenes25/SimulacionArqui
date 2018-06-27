package com.company.threads;

import com.company.Context;
import com.company.Instruction;
import com.company.blocks.DataBlock;
import com.company.blocks.InstructionBlock;
import com.company.blocks.State;
import com.company.cache.DataCacheBlock;
import com.company.cache.InstructionCacheBlock;
import com.company.core.Core;

import java.util.Collections;

public class ThreadCore0 implements Runnable {

    private Context context;
    private Core core0;


    public ThreadCore0(Context context, Core core0) {
        this.context = context;
        this.core0 = core0;
    }

    public void run() {
        while (this.core0.getProcessor().getContextInitialQueueSize() != this.core0.getProcessor().getFinishedContexts().size()) {
            if(this.context != null){
                int instructionMemoryBlockPos = this.context.getPc() / 16;          //saca el numero de bloque
                int instructionCacheBlockPos = instructionMemoryBlockPos % 4;       //saca la posicion en cache
                //va y trae ese bloque de la cache de instrucciones
                InstructionCacheBlock instructionCacheBlock = this.core0.getInstructionCacheCore0().getBlockFromCache(instructionCacheBlockPos);

                if (instructionCacheBlock.getLabel() != instructionMemoryBlockPos) {  //miss
                    while (this.isInstructionCachePositionReserved(instructionCacheBlockPos)) {
                        this.core0.changeCycle();
                    }
                    //reserva la posicion de cache
                    this.tryToReserveInstructionCachePosition(instructionCacheBlockPos);
                    this.core0.askForInstructionBus();

                    //resolver fallo.
                    this.context.setStalled(true);
                    this.core0.goToMemory();
                    //trae bloque de instruccion de memoria.
                    InstructionBlock instructionBlock = (InstructionBlock) this.core0.getProcessor().getInstructionMemory().get(instructionMemoryBlockPos - 24);
                    instructionCacheBlock.setInstructionBlock(instructionBlock);
                    instructionCacheBlock.setLabel(instructionMemoryBlockPos);

                    this.context.setStalled(false);
                    this.core0.getProcessor().getInstructionBus().release(); //suelto el bus despues de resolver el fallo.
                    //libera la posicion en cache
                    this.core0.setInstructionCacheReservedPosition(-1);
                }

                int instructionCacheWord = this.context.getPc() % 16;
                //se trae el bloque de cache de instrucciones
                Instruction instruction = this.core0.getCacheInstruction(instructionCacheBlock.getInstructionBlock(), instructionCacheWord);
                this.context.setPc(this.context.getPc() + 4);
                this.context.setPcRead(true);
                int opCode = this.core0.decodeInstruction(instruction, this.context); //decodificar y resolver instruction
                if (opCode == 35) {               //LW
                    this.solveLW(instruction);
                } else if (opCode == 43) {          //SW
                    this.solveSW(instruction);
                }
                this.context.setCurrentQuantum(this.context.getCurrentQuantum() - 1);
                this.context.setPcRead(false);
                //fin de ciclo, espera al resto de hilos a llegar a este punto
                this.core0.changeCycle();
            }
            else
            {
                this.core0.changeCycle();
            }
        }

        System.out.println("SALI COre0");

    }


    private void solveLW(Instruction instruction) {
        System.out.println("ejecuta load core 0");
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int numBlock = memoryPos / 16;
        int cachePos = numBlock % 4;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore0 = this.core0.getCacheCore0().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore1 = this.core0.getCacheCore1().getBlockFromCache(cachePos);

        if (dataCacheBlockCore0.getLabel() == numBlock) {
            // El bloque está en la caché
            if (!dataCacheBlockCore0.getState().equals(State.I)) {
                //Estado es modificado o compartido, resuelvo el load

                //Reviso si la posicion de cache que esta reservada es igual a la que quiero usar
                this.isDataCachePositionReservedEqualsToMine(cachePos);

                //Ejecuta el load
                this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore0.getWordFromBlock(word));
            } else {                //Estado es inválido, miss

                //trato de reservar la posicion
                this.tryToReserveDataCachePosition(cachePos);
                //cuando ya la pude reservar, me declaro en fallo
                this.context.setStalled(true);
                // Bloqueo mi cache
                this.core0.blockMyCachePos(dataCacheBlockCore0);
                //pido el bus y bloqueo la otra cache
                this.core0.tryToLockBlock(dataCacheBlockCore1);

                //resuelvo el fallo
                this.core0.goToMemory();

                if (dataCacheBlockCore1.getLabel() == numBlock && dataCacheBlockCore1.getState().equals(State.M)) {
                    // La otra cache tiene el bloque también y el bloque está modificado
                    //guarda el bloque actualizado en memoria
                    this.core0.getProcessor().saveInMemory(numBlock, dataCacheBlockCore1.getDataBlock());
                    dataCacheBlockCore1.setState(State.C);

                    //guarda bloque a cache
                    DataBlock dataBlock1 = new DataBlock();
                    DataBlock dataBlock2 = new DataBlock();
                    dataBlock2 = (DataBlock) this.core0.getProcessor().getMainMemory().get(numBlock);
                    Collections.copy(dataBlock1.getWords(), dataBlock2.getWords());
                    dataCacheBlockCore0.setDataBlock(dataBlock1);
                    dataCacheBlockCore0.setState(State.C);
                    // si entre en fallo y no soy el principal
                    while (!this.context.isPrincipal()) {
                        this.core0.changeCycle();
                    }
                    //Ejecucion del load
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore0.getWordFromBlock(word));

                } else {
                    // La otra caché no tiene el bloque o lo tiene I/C
                    DataBlock dataBlock = (DataBlock) this.core0.getProcessor().getMainMemory().get(numBlock);
                    DataBlock dataBlock1 = new DataBlock();
                    Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

                    //guardar bloque a cache
                    dataCacheBlockCore0.setDataBlock(dataBlock1);
                    dataCacheBlockCore0.setState(State.C);
                    // si entre en fallo y no soy el principal
                    while (!this.context.isPrincipal()) {
                        this.core0.changeCycle();
                    }
                    //Ejecucion del load
                    this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore0.getWordFromBlock(word));

                }
                this.context.setStalled(false);
                this.core0.setDataCacheReservedPosition(-1);
                dataCacheBlockCore0.getCacheLock().release();
                dataCacheBlockCore1.getCacheLock().release();
                this.core0.getProcessor().getDataBus().release();
            }

        } else {
            //el bloque no estaba en mi cache

            //trato de reservar la posicion
            this.tryToReserveDataCachePosition(cachePos);
            //cuando ya la pude reservar, me declaro en fallo
            this.context.setStalled(true);

            if (dataCacheBlockCore0.getState().equals(State.M)) {
                //pido el bus y voy a guardar a memoria
                this.core0.saveModifiedBlock(dataCacheBlockCore0);
            }
            // el bloque no estaba modificado
            this.core0.tryToLockBlock(dataCacheBlockCore1);
            // Bloquear mi cache
            this.core0.blockMyCachePos(dataCacheBlockCore0);

            this.core0.goToMemory();

            //revisa si el bloque de la otra cache estaba modificado
            if (this.core0.checkOtherCacheStatus(dataCacheBlockCore1, dataCacheBlockCore0, numBlock))
                dataCacheBlockCore1.setState(State.C);

            dataCacheBlockCore1.getCacheLock().release();//libero cache del otro nucleo
            DataBlock dataBlock = (DataBlock) this.core0.getProcessor().getMainMemory().get(numBlock);
            DataBlock dataBlock1 = new DataBlock();
            Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

            //guarda bloque a cache
            dataCacheBlockCore0.setDataBlock(dataBlock1);
            dataCacheBlockCore0.setLabel(numBlock);
            dataCacheBlockCore0.setState(State.C);

            // si entre en fallo y no soy el principal
            while (!this.context.isPrincipal()) {
                System.out.println("no soy en principal");
                this.core0.changeCycle();
            }
            //Ejecucion del load
            this.context.setRegisterValue(instruction.getInstructionValue(2), dataCacheBlockCore0.getWordFromBlock(word));
            this.context.setStalled(false);
            this.core0.setDataCacheReservedPosition(-1);
            //Libera los candados
            dataCacheBlockCore0.getCacheLock().release();
            this.core0.getProcessor().getDataBus().release();
        }
    }

    private void solveSW(Instruction instruction) {
        System.out.println("ejecuta store core 0");
        int memoryPos = instruction.getInstructionValue(3) + this.context.getRegisterValue(instruction.getInstructionValue(1));
        int numBlock = memoryPos / 16;
        int cachePos = numBlock % 4;
        int word = (memoryPos % 16) / 4;
        DataCacheBlock dataCacheBlockCore0 = this.core0.getCacheCore0().getBlockFromCache(cachePos);
        DataCacheBlock dataCacheBlockCore1 = this.core0.getCacheCore1().getBlockFromCache(cachePos); //obtener bloque de la otra cache

        if (dataCacheBlockCore0.getLabel() == numBlock) {
            // El bloque está en la caché
            if (dataCacheBlockCore0.getState().equals(State.M)) {   //Si el estado es modificado

                //Reviso si la posicion de cache que esta reservada es igual a la que quiero usar
                this.isDataCachePositionReservedEqualsToMine(cachePos);

                //Ejecucion del store
                dataCacheBlockCore0.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

            } else if (dataCacheBlockCore0.getState().equals(State.C)) {  //Si el estado es compartido
                //Reviso si la posicion de cache que esta reservada es igual a la que quiero usar
                this.isDataCachePositionReservedEqualsToMine(cachePos);

                dataCacheBlockCore0.setState(State.M);
                this.core0.tryToLockBlock(dataCacheBlockCore1); //intento bloquear el bus y luego el bloque de la otra cache
                // Bloquear mi cache
                this.core0.blockMyCachePos(dataCacheBlockCore0);

                //Voy a la otra cache a invalidar el bloque.
                if (dataCacheBlockCore1.getLabel() == numBlock)
                    dataCacheBlockCore1.setState(State.I);

                dataCacheBlockCore1.getCacheLock().release();
                // si entre en fallo y no soy el principal
                while (!this.context.isPrincipal()) {
                    this.core0.changeCycle();
                }
                // Ejecucion del store
                dataCacheBlockCore0.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
                //libero candados
                dataCacheBlockCore0.getCacheLock().release();
                this.core0.getProcessor().getDataBus().release();

            } else {   //Si el estado es invalido

                //trato de reservar la posicion
                this.tryToReserveDataCachePosition(cachePos);
                //cuando ya la pude reservar, me declaro en fallo
                this.context.setStalled(true);

                this.core0.tryToLockBlock(dataCacheBlockCore1); //intento bloquear el bus y luego el bloque de la otra cache
                // Bloquear mi cache
                this.core0.blockMyCachePos(dataCacheBlockCore0);

                dataCacheBlockCore0.setState(State.M);
                //guarda bloque de memoria en la cache del nucleo 1
                this.core0.goToMemory();

                //revisar etiqueta de la otra cache
                if (dataCacheBlockCore1.getLabel() == numBlock) {
                    // El estado siempre va a estar modificado
                    // escribo el bloque modificado de la otra cache en memoria
                    this.core0.getProcessor().saveInMemory(numBlock, dataCacheBlockCore1.getDataBlock());

                    //se invalida el bloque de la otra cache
                    dataCacheBlockCore1.setState(State.I);
                }
                dataCacheBlockCore1.getCacheLock().release(); //libera candado de la otra cache

                DataBlock dataBlock = (DataBlock) this.core0.getProcessor().getMainMemory().get(numBlock);
                DataBlock dataBlock1 = new DataBlock();
                Collections.copy(dataBlock1.getWords(), dataBlock.getWords());

                //guarda bloque a cache
                dataCacheBlockCore0.setDataBlock(dataBlock1);

                // si entre en fallo y no soy el principal
                while (!this.context.isPrincipal()) {
                    this.core0.changeCycle();
                }
                // Ejecucion del store
                dataCacheBlockCore0.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));

                this.context.setStalled(false);
                this.core0.setDataCacheReservedPosition(-1);
                //Libera los candados
                dataCacheBlockCore0.getCacheLock().release();
                this.core0.getProcessor().getDataBus().release();
            }
        } else {
            //el bloque que ocupo no esta en mi cache
            //trato de reservar la posicion
            this.tryToReserveDataCachePosition(cachePos);
            //cuando ya la pude reservar, me declaro en fallo
            this.context.setStalled(true);

            // el bloque no está en cache
            if (dataCacheBlockCore0.getState().equals(State.M)) {
                //mi cache tiene un bloque en estado modificado
                this.core0.saveModifiedBlock(dataCacheBlockCore0);
            }
            this.core0.tryToLockBlock(dataCacheBlockCore1);
            // Bloquear mi cache
            this.core0.blockMyCachePos(dataCacheBlockCore0);

            this.core0.goToMemory();

            //revisa si el bloque de la otra cache estaba modificado y si estaba modificado lo guarda a memoria
            if (this.core0.checkOtherCacheStatus(dataCacheBlockCore1, dataCacheBlockCore0, numBlock))
                dataCacheBlockCore1.setState(State.I);

            dataCacheBlockCore1.getCacheLock().release();
            //sacar el bloque de memoria
            DataBlock dataBlock = (DataBlock) this.core0.getProcessor().getMainMemory().get(numBlock);
            DataBlock dataBlock1 = new DataBlock();
            Collections.copy(dataBlock1.getWords(), dataBlock.getWords());
            //guardar bloque a cache
            dataCacheBlockCore0.setDataBlock(dataBlock1);
            // si entre en fallo y no soy el principal
            while (!this.context.isPrincipal()) {
                this.core0.changeCycle();
            }
            // Ejecucion del store
            dataCacheBlockCore0.getDataBlock().setWord(word, this.context.getRegisterValue(instruction.getInstructionValue(2)));
            dataCacheBlockCore0.setLabel(numBlock);
            dataCacheBlockCore0.setState(State.M);

            this.context.setStalled(false);
            this.core0.setDataCacheReservedPosition(-1);
            //Libera los candados
            dataCacheBlockCore0.getCacheLock().release();

            this.core0.getProcessor().getDataBus().release();
        }
    }

    public Context getContext() {
        try {
            return context;
        } catch (Exception e) {
            return null;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Core getCore0() {
        return core0;
    }

    public void setCore0(Core core0) {
        this.core0 = core0;
    }

    private boolean isInstructionCachePositionReserved(int position) {
        if (position != this.core0.getInstructionCacheReservedPosition()) {
            return false;
        }
        return true;
    }

    private void isDataCachePositionReservedEqualsToMine(int position) {
        while (this.core0.getDataCacheReservedPosition() == position) {
            this.core0.changeCycle();
        }
    }

    private void tryToReserveDataCachePosition(int cachePos) {
        while (this.core0.getDataCacheReservedPosition() != -1) {
            this.core0.changeCycle();
        }
        this.core0.setDataCacheReservedPosition(cachePos);
    }

    private void tryToReserveInstructionCachePosition(int cachePos) {
        while (this.core0.getInstructionCacheReservedPosition() != -1) {
            this.core0.changeCycle();
        }
        this.core0.setInstructionCacheReservedPosition(cachePos);
    }
}
