package com.company.threads;

import java.util.concurrent.*;

public class MainThread implements Runnable{

    public CyclicBarrier cyclicBarrier;

    public MainThread(CyclicBarrier cyclicBarrier){
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        //aumentar ciclos de reloj
        //llama a los check status
        //poner dos barreras

        try {
            this.cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }
}
