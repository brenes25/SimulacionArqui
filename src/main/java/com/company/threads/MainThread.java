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
            processor.getCore0().checkStatus();
            processor.getCore1().checkStatus();

            //poner dos barreras

            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
