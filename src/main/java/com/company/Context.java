package com.company;

import java.util.ArrayList;
import java.util.List;

public class Context {

    private static final int REGISTER_NUMBER = 32;
    private List registers;
    private int pc;
    private int currentQuantum;

    public Context(int pc, int currentQuantum){
        this.registers = new ArrayList<Integer>();
        this.pc = pc;
        this.currentQuantum = currentQuantum;
    }

    public void fillRegisters(){
        for (int i = 0; i < REGISTER_NUMBER; i++) {
            registers.add(0);
        }
    }

    public int getPc() {
        return pc;
    }
}
