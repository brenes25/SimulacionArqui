package com.company.core;

import com.company.Context;
import com.company.Processor;
import com.company.cache.*;
import com.company.threads.ThreadCore1;

import java.util.concurrent.*;

public class Core1 extends Core {

    private ThreadCore1 mainContext;
    private Context context;
    private DataCache myDataCache;
    private DataCache otherDataCache;
    private InstructionCache myInstructionCache;

    public Core1(Context context, Processor processor,DataCache myDataCache,DataCache otherDataCache,
                 InstructionCache myInstructionCache,String name) {
        super(context, processor);
        this.myDataCache = myDataCache;
        this.otherDataCache = otherDataCache;
        this.myInstructionCache = myInstructionCache;
        this.mainContext = new ThreadCore1(context, this);
        Thread thread = new Thread(this.mainContext, name);
        thread.start();
    }

    public void checkStatus() {
        if(this.mainContext.getContext() != null){
            if (this.mainContext.getContext().isDone()) {                                                //si ya termino
                this.processor.getFinishedContexts().add(this.mainContext.getContext());                //lo guardo en la cola de contextos terminados
                if (!this.processor.getContextQueue().isEmpty()) {                                        //si hay hilillos en la cola
                    this.context = this.processor.getNextContext();
                    this.mainContext.setContext(this.context);     //saco uno
                } else {
                    this.context =  null;
                    this.mainContext.setContext(null);
                }
            } else if (this.mainContext.getContext().getCurrentQuantum() == 0) {                            //si se le acabo el quantum y no ha terminado
                this.processor.getContextQueue().add(this.mainContext.getContext());                    //lo guarda en la cola de contextos
                if (!this.processor.getContextQueue().isEmpty()) {
                    this.context = this.processor.getNextContext();
                    this.mainContext.setContext(this.context);                           //trae el sig
                } else{
                    this.context =  null;
                    this.mainContext.setContext(null);
                }
            }
        }
        else{
            if (!this.processor.getContextQueue().isEmpty()) {
                this.context = this.processor.getNextContext();
                this.mainContext.setContext(this.context);                           //trae el sig
            }
        }
    }

    public Processor getProcessor() {
        return processor;
    }

    public DataCache getMyDataCache() {
        return myDataCache;
    }

    public DataCache getOtherDataCache() {
        return otherDataCache;
    }

    public InstructionCache getMyInstructionCache() {
        return myInstructionCache;
    }

}
