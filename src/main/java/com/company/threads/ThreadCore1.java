package com.company.threads;

import com.company.*;
import com.company.blocks.InstructionBlock;
import com.company.cache.*;
import com.company.core.*;

import java.util.concurrent.BrokenBarrierException;

public class ThreadCore1 implements Runnable {

    private Context context;
    private Core1 core1;

    public ThreadCore1(Context context, Core1 core1){
        this.context = context;
        this.core1 = core1;
    }

    public void run() {
        while (true) {
            int instructionMemoryBlockPos = this.context.getPc() / 16;          //saca el numero de bloque
            int instructionCacheBlockPos = instructionMemoryBlockPos % 4;       //saca la posicion en cache
            //va y trae ese bloque de la cache
            InstructionCacheBlock instructionCacheBlock = this.core1.getInstructionCache().getBlockFromCache(instructionCacheBlockPos);

            //va y trae el bloque de memoria para hacer la comparacion
            InstructionBlock instructionBlock = (InstructionBlock) this.core1.getProcessor().getInstructionMemory().get(instructionMemoryBlockPos - 24);

            if (instructionCacheBlock.getLabel() != instructionMemoryBlockPos) {  //miss
                while (!this.core1.getProcessor().getInstructionBus().tryAcquire()) {     // pido el bus y mientras no lo agarro caigo en la barrera
                    try {
                        this.core1.getProcessor().cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
                //resolver fallo.
                this.context.setStalled(true);
                instructionCacheBlock.setInstructionBlock(instructionBlock);
                instructionCacheBlock.setLabel(instructionMemoryBlockPos);
                for (int i = 0; i < 40; i++) {
                    try {
                        this.core1.getProcessor().cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
                this.context.setStalled(false);
                this.core1.getProcessor().getInstructionBus().release(); //suelto el bus despues de resolver el fallo.
            }
            int instructionCacheWord = this.context.getPc() % 16;
            Instruction instruction = this.core1.getCacheInstruction(instructionBlock,instructionCacheWord);
            this.context.setPc(this.context.getPc()+4);
            int opCode = this.core1.decodeInstruction(instruction,this.context); //decodificar y resolver instruction
            if(opCode == 35){               //LW
                solveLW(instruction);
            }
            else if(opCode == 45){          //SW
                solveSW(instruction);
            }
            //fin de ciclo, espera al resto de hilos a llegar a este punto
            try {
                this.core1.getProcessor().cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            //esperando a finalizar los cambios del tiempo 0
            try {
                this.core1.getProcessor().cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }


        }

    }

    private void solveSW(Instruction instruction){

    }

    private void solveLW(Instruction instruction){

    }




    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
