package com.company.threads;

import com.company.Context;

public class ThreadCore0  implements Runnable{

    private Context context;
    private boolean isStalled;
    private boolean finishedStalled;
    private boolean isDone;

    public ThreadCore0(Context context){
        this.context = context;
        this.isStalled = false;
    }

    public void run() {

    }

    public boolean isStalled() {
        return isStalled;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isFinishedStalled() {
        return finishedStalled;
    }

    public Context getContext() {
        return context;
    }

    public boolean isDone() {
        return isDone;
    }
}
