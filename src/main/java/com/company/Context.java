package com.company;

import java.util.ArrayList;
import java.util.List;

/**
 * Corresponde a un hilillo dentro de la simulacion.
 * @author Silvia Brenes
 * @author María José Cubero
 * @author Hernán Madrigal
 */
public class Context {

    private static final int REGISTER_NUMBER = 32;
    private List registers;
    private int pc;
    private int currentQuantum;

    private boolean isStalled;
    private boolean isDone;
    private int id;
    private int cyclesCount;

    /**
     * Constructor
     */
    public Context(int pc, int currentQuantum){
        this.registers = new ArrayList<Integer>();
        this.pc = pc;
        this.currentQuantum = currentQuantum;
        this.isDone = false;
        this.isStalled = false;
        this.fillRegisters();
        this.id = -1;
        this.cyclesCount = 0;
    }

    /**
     * Inicializa los registros con 0.
     */
    public void fillRegisters(){
        for (int i = 0; i < REGISTER_NUMBER; i++) {
            registers.add(0);
        }
    }

    /**
     * Devuelve el valor de un registro.
     * @param register corresponde al registro al cual se le saca el valor.
     * @return el valor del registro.
     */
    public int getRegisterValue(int register){
        return (Integer) this.registers.get(register);
    }

    /**
     * Guarda un valor en registro.
     * @param register
     * @param value
     */
    public void setRegisterValue(int register, int value){
        this.registers.set(register, value);
    }

    /**
     * Imprime los registros del contexto.
     */
    public void printRegisters(){
        System.out.println(" ----- Registros del contexto " + this.id + " -----");
        for (int i = 0; i < registers.size(); i++) {
            System.out.print(registers.get(i));
            System.out.print("  ");
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getCyclesCount() {
        return cyclesCount;
    }

    public void setCyclesCount(int cyclesCount) {
        this.cyclesCount = cyclesCount;
    }

}

