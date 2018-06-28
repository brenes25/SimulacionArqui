package com.company.threads;

import com.company.*;

import java.util.Scanner;
import java.util.concurrent.*;

public class MainThread implements Runnable{

    private Processor processor;
    private int cycleCount;

    public MainThread(Processor processor){
        this.processor = processor;
        this.cycleCount = 1;
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

            if (this.processor.isSlowRun() && cycleCount == 20){
                // imprimir información
                System.out.println("Reloj: " + this.processor.getClock());
                if (this.processor.getCore1().getContext() != null)
                    System.out.println("El hilillo " + this.processor.getCore1().getContext().getId()+ " está corriendo, core 1.");
                if (this.processor.getCore0().getContext() != null)
                    System.out.println("El hilillo " + this.processor.getCore0().getContext().getId()+ " está corriendo, core 0.");

                this.cycleCount = 0;
                Scanner input = new Scanner (System.in);
                System.out.println("Presione una tecla para continuar");
                input.nextLine();
            }
            if (this.processor.getCore1().getContext() != null)
                this.processor.getCore1().getContext().setCyclesCount(this.processor.getCore1().getContext().getCyclesCount() +1);

            if (this.processor.getCore0().getContext() != null)
                this.processor.getCore0().getContext().setCyclesCount(this.processor.getCore0().getContext().getCyclesCount() +1);
            //llama a los check status
            //processor.getCore0().checkStatus();
            processor.getCore1().checkStatus();
            processor.getCore0().checkStatus();

            try {
                this.processor.cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            this.cycleCount ++;
        }
        System.out.println("La simulacion duro un total de: " + this.processor.getClock() + " ciclos. \n ");
        for (int i = 0; i < this.processor.getFinishedContexts().size(); i++) {
            Context context = (Context) this.processor.getFinishedContexts().get(i);
            context.printRegisters();
            System.out.println("Cantidad de ciclos del hilillo "+ context.getId() +": "+context.getCyclesCount() + "\n ");
        }
        this.processor.printMainMemory();
        System.out.println(this.processor.getCore1().getCacheCore0().toString());
        System.out.println(this.processor.getCore0().getCacheCore1().toString());
    }
}
