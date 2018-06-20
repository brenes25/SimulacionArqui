package com.company.core;

import com.company.Context;
import com.company.Processor;
import com.company.cache.DataCache;
import com.company.cache.InstructionCache;
import com.company.threads.ThreadCore0;
import javafx.util.Pair;

import java.util.concurrent.*;


public class Core0 extends Core {
    private ThreadCore0 mainContext;
    private ThreadCore0 secondaryContext;

    public CyclicBarrier cyclicBarrier;

    private Pair reservedPosition;


    public Core0(Context context, Processor processor) {
        super(processor);
        this.reservedPosition = new Pair<String, Integer>("", -1);
        this.mainContext = new ThreadCore0(context);
        this.cyclicBarrier = processor.cyclicBarrier;
        Thread thread = new Thread(mainContext, "thread1");
        thread.start();
    }

    public void checkStatus() {

        if (this.mainContext.isStalled()) {                             //si el hilo principal entra en fallo
            if (this.secondaryContext.getContext() == null) {                        //no hay otro hilo en fallo
                this.secondaryContext = this.mainContext;               //hago el cambio de contexto.
                this.mainContext.setContext((Context) this.processor.getContextQueue().poll());    //y traigo otro hilo de la cola de contexto
                Thread thread = new Thread(secondaryContext, "thread2");                          //pongo el nuevo a correr.
                thread.start();
            }
            else{
                if (this.secondaryContext.isStalled()){
                    //cae en la barrera el mainContext porque el secondary tiene prioridad sobre mi.
                }
                else{
                    //pasa a ejecutar el secondary.
                }
            }
        }
        else{//cuando se acaba el ciclo y no estoy en fallo.

            if(this.secondaryContext.isFinishedStalled()){
                // el otro hilo resolvio el fallo y me tiene q quitar el procesador.
                //hay que hacer context switch, o sea me interrumpe.
            }
            else if(this.mainContext.getContext().getCurrentQuantum() == 0){                    //si se me acabo el quantum

                if(this.mainContext.isDone()){              //y ya termine
                    processor.getFinishedContexts().add(this.mainContext.getContext());         //guardo mi contexto en los terminados..
                }

                processor.getContextQueue().add(this.mainContext.getContext());                 // paso a la cola de contextos sin terminar.
                if(this.secondaryContext.isStalled()){
                    this.mainContext.setContext( (Context) processor.getContextQueue().poll());
                }
                else{
                    this.mainContext.setContext(this.secondaryContext.getContext());        ///como ya me guarde entonces el secundario pasa a primer plano y el secundario se pone en null.
                    this.secondaryContext.setContext(null);
                }
            }
            //sigo ejecutando porque no hubo cambios en mi contexto.
            //falta restar el quantum
        }
    }

}
