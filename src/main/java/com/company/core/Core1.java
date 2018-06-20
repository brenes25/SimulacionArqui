package com.company.core;

import com.company.Context;
import com.company.Processor;
import com.company.cache.*;
import com.company.threads.ThreadCore1;

import java.util.concurrent.*;

public class Core1 extends Core{

    private ThreadCore1 mainContext;
    private Processor processor;
    public CyclicBarrier cyclicBarrier;

    public Core1(Context context, Processor processor){
        this.processor = processor;
        this.cyclicBarrier = processor.cyclicBarrier;
        this.mainContext = new ThreadCore1(context, this);
        Thread thread = new Thread(this.mainContext, "threadC1");
        thread.start();
    }

    public void checkStatus(){
        if (this.mainContext.getContext().isDone()){                                                //si ya termino
            this.processor.getFinishedContexts().add(this.mainContext.getContext());                //lo guardo en la cola de contextos terminados
            if(!this.processor.getContextQueue().isEmpty()){                                        //si hay hilillos en la cola
                this.mainContext.setContext((Context) this.processor.getContextQueue().poll());     //saco uno
            }
        }
        else if(this.mainContext.getContext().getCurrentQuantum() == 0){                            //si se le acabo el quantum y no ha terminado
            this.processor.getContextQueue().add(this.mainContext.getContext());                    //lo guarda en la cola de contextos
            this.mainContext.setContext(this.processor.getNextContext());                           //trae el sig
        }
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
