package com.company;

import java.io.File;
import java.util.Scanner;

public class Menu {

    private static final String PATH = "./src/main/java/resources/";
    private DataParser dataParser;
    private int quantum;

    public Menu (int quantum){
        this.quantum = quantum;
    }

    public void userStart(){
        boolean start = true;
        Scanner input = new Scanner (System.in);
        while(start){
            System.out.println("Cuales hilillos quiere correr? (Ej: 0 1 2 3 4 5)");

            String threads [] = input.nextLine().split("\\s+");

            for (int i = 0; i < threads.length ; i++) {
                File file = new File(PATH + threads[i] + ".txt");
                if (file.exists()){
                    this.dataParser.parseFile(PATH + threads[i] + ".txt");
                }
                else if (!threads[i].equals("")){
                    System.out.println("El archivo "+ threads[i] +"no está en la carpeta de resources");
                }
            }

            System.out.println("Desea agregar mas hilillos? (si - no)");

            if(input.nextLine().equals("no"))
                start = false;
        }
        System.out.println("Cuantos ciclos de reloj quiere que dure el quantum");
        this.quantum = Integer.parseInt(input.nextLine());
    }

}
