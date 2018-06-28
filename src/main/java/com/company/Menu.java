package com.company;

import java.io.File;
import java.util.Scanner;

public class Menu {

    private static final String PATH = "./src/main/java/resources/";
    private DataParser dataParser;
    private int quantum;

    public Menu (int quantum, DataParser dataParser){
        this.quantum = quantum;
        this.dataParser = dataParser;
    }

    public boolean userStart(){

        boolean start = true;
        Scanner input = new Scanner (System.in);

        System.out.print("Valor del quantum: ");
        this.quantum = Integer.parseInt(input.nextLine().trim());
        while (this.quantum == 0){
            System.out.println("El quantum no puede ser 0, digite un numero mayor: ");
            this.quantum = Integer.parseInt(input.nextLine().trim());
        }
        this.dataParser.getProcessor().setQuantum(quantum);

        while(start){
            System.out.println("\nCuales hilillos quiere correr? (Ej: 0 1 2 3 4 5)");

            String threads [] = input.nextLine().split("\\s+");

            for (int i = 0; i < threads.length ; i++) {
                File file = new File(PATH + threads[i] + ".txt");
                if (file.exists()){
                    this.dataParser.parseFile(PATH + threads[i] + ".txt");
                }
                else if (!threads[i].equals(" ")){
                    System.out.println("El archivo "+ threads[i] +".txt no está en la carpeta de resources");
                }
            }

            System.out.println("Desea agregar mas hilillos? (si - no)");

            if(input.nextLine().equals("no"))
                start = false;
        }

        System.out.println("Desea correrlo lento o rapido? \n 1. lento \n 2. rapido");
        return Integer.parseInt(input.nextLine()) == 1;
    }

}