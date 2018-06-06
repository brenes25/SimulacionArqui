package com.company;

public class Main {

    public static void main(String[] args) {

        Processor processor = new Processor();
        DataParser dataParser= new DataParser(processor);
        dataParser.parseFile("./src/resources/0.txt");
        processor.printInstructionMemory();

        /*sig cosas a hacer
          - crear las colas de context;
          - agregarle el PC
          - saber donde se empezo a guardar en la memoria
          */
    }
}
