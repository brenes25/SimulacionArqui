package com.company.core;

import com.company.Context;
import com.company.Processor;
import com.company.cache.*;
import com.company.threads.ThreadCore1;

import java.util.concurrent.*;

public class Core1 extends Core {

    private ThreadCore1 mainContext;
    public CyclicBarrier cyclicBarrier;
    private DataCache myDataCache;
    private DataCache otherDataCache;
    private InstructionCache myinstructionCache;

    public Core1(Context context, Processor processor,DataCache myDataCache,DataCache otherDataCache,
                 InstructionCache myInstructionCache,String name) {
        super(processor);
        this.cyclicBarrier = processor.cyclicBarrier;
        this.myDataCache = myDataCache;
        this.otherDataCache = otherDataCache;
        this.myinstructionCache = myInstructionCache;
        this.mainContext = new ThreadCore1(context, this);
        Thread thread = new Thread(this.mainContext, name);
        thread.start();
    }

    public void checkStatus() {
        if(this.mainContext.getContext() != null){
            if (this.mainContext.getContext().isDone()) {                                                //si ya termino
                this.processor.getFinishedContexts().add(this.mainContext.getContext());                //lo guardo en la cola de contextos terminados
                System.out.println("contexts terminados nucleo 1: " + this.processor.getFinishedContexts().size());
                if (!this.processor.getContextQueue().isEmpty()) {                                        //si hay hilillos en la cola
                    this.mainContext.setContext(this.processor.getNextContext());     //saco uno
                } else {
                    this.mainContext.setContext(null);
                }
            } else if (this.mainContext.getContext().getCurrentQuantum() == 0) {                            //si se le acabo el quantum y no ha terminado
                this.processor.getContextQueue().add(this.mainContext.getContext());                    //lo guarda en la cola de contextos
                if (!this.processor.getContextQueue().isEmpty()) {
                    this.mainContext.setContext(this.processor.getNextContext());                           //trae el sig
                }
                else{
                    this.mainContext.setContext(null);
                }
            }
        }
    }

    public Processor getProcessor() {
        return processor;
    }


    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public DataCache getMyDataCache() {
        return myDataCache;
    }

    public DataCache getOtherDataCache() {
        return otherDataCache;
    }

    public InstructionCache getMyinstructionCache() {
        return myinstructionCache;
    }

}
