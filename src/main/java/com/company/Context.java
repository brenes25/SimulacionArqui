package com.company;

import java.util.ArrayList;
import java.util.List;

public class Context {

    private static final int REGISTER_NUMBER = 32;
    private List registers;
    private int pc;
    private int currentQuantum;

    private boolean isStalled;
    private boolean finishedStalled;
    private boolean isDone;
    private boolean isStopped;
    private boolean isPrincipal;
    private int id;

    public Context(int pc, int currentQuantum){
        this.registers = new ArrayList<Integer>();
        this.pc = pc;
        this.currentQuantum = currentQuantum;
        this.isDone = false;
        this.isStalled = false;
        this.fillRegisters();
        this.id = -1;
    }

    public void fillRegisters(){
        for (int i = 0; i < REGISTER_NUMBER; i++) {
            registers.add(0);
        }
    }

    public int getRegisterValue(int value){
        return (Integer) this.registers.get(value);
    }

    public void setRegisterValue(int register, int value){
        this.registers.set(register, value);
    }

    public void printRegisters(){
        System.out.println("-------- REGISTERS -------------");
        for (int i = 0; i < registers.size(); i++) {
            System.out.print(registers.get(i));
            System.out.print(" - ");
        }
        System.out.println();
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getCurrentQuantum() {
        return currentQuantum;
    }

    public void setCurrentQuantum(int currentQuantum) {
        this.currentQuantum = currentQuantum;
    }

    public boolean isStalled() {
        return isStalled;
    }

    public void setStalled(boolean stalled) {
        isStalled = stalled;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }

    public boolean isFinishedStalled() {
        return finishedStalled;
    }

    public void setFinishedStalled(boolean finishedStalled) {
        this.finishedStalled = finishedStalled;
    }

    public boolean isPrincipal() {
        return isPrincipal;
    }

    public void setPrincipal(boolean principal) {
        isPrincipal = principal;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

