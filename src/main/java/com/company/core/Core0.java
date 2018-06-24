package com.company.core;

import com.company.Context;
import com.company.Processor;
import com.company.threads.ThreadCore0;

import java.util.concurrent.*;


public class Core0 extends Core {
    private ThreadCore0 mainContext;
    private ThreadCore0 secondaryContext;

    public CyclicBarrier cyclicBarrier;



    public Core0(Context context, Processor processor) {
        super(processor);
        context.setPrincipal(true);

        this.mainContext = new ThreadCore0(context, processor.getCore0());
        this.secondaryContext = new ThreadCore0(null, processor.getCore0());
        this.cyclicBarrier = processor.cyclicBarrier;
        Thread thread = new Thread(mainContext, "thread1");
        thread.start();
        Thread thread2 = new Thread(secondaryContext, "thread2");
        thread2.start();
    }

    public void checkStatus() {
        if (this.mainContext.getContext().isDone()) {                          //si ya termino
            this.processor.getFinishedContexts().add(this.mainContext.getContext());  //lo guardo en la cola de contextos terminados
            this.changeSecondaryToPrincipal();
        }
        //si el hilo principal entra en fallo
        else if (this.mainContext.getContext().isStalled()) {
            //no hay otro hilo en fallo
            if (this.secondaryContext.getContext() == null) {
                //hago el cambio de contexto.
                this.mainContext.getContext().setFinishedStalled(false);
                this.secondaryContext = this.mainContext;
                //y traigo otro hilo de la cola de contexto
                this.mainContext.setContext((Context) this.processor.getContextQueue().poll());
                //pongo el nuevo a correr.

            }
            //le doy el procesador al hilillo secundario
            else if(!this.secondaryContext.getContext().isStalled()){
                Context auxContext = this.secondaryContext.getContext();
                this.secondaryContext.setContext(this.mainContext.getContext());
                this.mainContext.setContext(auxContext);
            }
        }
        // no tuve fallo
        else if(this.mainContext.getContext().getCurrentQuantum() == 0){
            //lo guarda en la cola de contextos
            this.processor.getContextQueue().add(this.mainContext.getContext());
            this.changeSecondaryToPrincipal();

        }
        //caso en el que por ser el mas viejo tengo derecho de tener el procesador
        else {
            if(this.secondaryContext.getContext().isPrincipal() && !this.mainContext.getContext().isStalled()){
                Context auxContext = this.secondaryContext.getContext();
                this.secondaryContext.setContext(this.mainContext.getContext());
                this.mainContext.setContext(auxContext);
            }
        }


    }

    /**
     * como el principal dejo de usar el procesador, yo tomo el procesador por ser el mas viejo
     */
    private void changeSecondaryToPrincipal(){
        if(this.secondaryContext.getContext() != null){
            this.secondaryContext.getContext().setPrincipal(true);
            this.mainContext.setContext(this.secondaryContext.getContext());
            this.secondaryContext.setContext(null);
        }
        else{
            if(!this.processor.getContextQueue().isEmpty()) {
                this.mainContext.setContext(this.processor.getNextContext());
                this.mainContext.getContext().setPrincipal(true);
            }
            else{
                this.mainContext.setContext(null);
                this.bothThreadsFinished = true;
                System.out.println("what");
            }

        }
    }


}
