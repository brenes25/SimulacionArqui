package com.company.threads;

import com.company.*;

import java.util.concurrent.*;

public class MainThread implements Runnable{

    private Processor processor;

    public MainThread(Processor processor){
        this.processor = processor;
    }

    @Override
    public void run() {
        while(this.processor.getContextInitialQueueSize() != this.processor.getFinishedContexts().size()) {
            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            //aumentar ciclos de reloj
            processor.setClock(processor.getClock() + 1);
            //llama a los check status
            //processor.getCore0().checkStatus();
            processor.getCore1().checkStatus();
            processor.getCore12().checkStatus();

            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        System.out.println("SALI");
        for (int i = 0; i < this.processor.getFinishedContexts().size(); i++) {
            Context context = (Context) this.processor.getFinishedContexts().get(i);
            context.printRegisters();
        }
        this.processor.printMainMemory();
        System.out.println(this.processor.getCore1().getCacheCore0().toString());
        System.out.println(this.processor.getCore12().getCacheCore1().toString());
    }
}
