package com.company;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private List registers;
    private int pc;
    private int currentQuantum;

    public Context(int pc, int currentQuantum){
        this.registers = new ArrayList<Integer>();
        this.pc = pc;
        this.currentQuantum = currentQuantum;
    }

    public int getPc() {
        return pc;
    }

    public int getCurrentQuantum() {
        return currentQuantum;
    }
}
