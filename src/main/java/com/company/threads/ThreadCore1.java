package com.company.threads;

import com.company.*;
import com.company.cache.*;
import com.company.core.*;

public class ThreadCore1 implements Runnable {

    private Context context;
    private boolean isStalled;
    private boolean isDone;
    private Core1 core1;

    public ThreadCore1(Context context, Core1 core1){
        this.context = context;
        this.isStalled = false;
        this.core1 = core1;
    }

    public void run() {
        while (true){
            int instructionMemoryBlock = this.context.getPc() / 16;          //saca la posicion en cache donde deberia estar la info.
            int instructionCacheBlockPos = instructionMemoryBlock % 4;
            InstructionCacheBlock instructionCacheBlock = (InstructionCacheBlock) this.core1.getInstructionCache().getCache().get(instructionCacheBlockPos);
            if (instructionCacheBlock.getLabel() !=  instructionMemoryBlock){  //miss
                
            }

        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isStalled() {
        return isStalled;
    }

    public void setStalled(boolean stalled) {
        isStalled = stalled;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
