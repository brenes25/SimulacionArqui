package com.company.blocks;

import java.util.*;

/**
 * Corresponde a un bloque de la cache de datos, contiene una lista de palabras.
 *@author Silvia Brenes
 *@author María José Cubero
 *@author Hernán Madrigal
 */
public class DataBlock {
   private List words;

   public DataBlock(){
       this.words = new ArrayList<Integer>();
       for (int i = 0; i < 4; i++) {
           this.words.add(1);
       }
   }

    public DataBlock(List<Integer> words) {
        this.words = words;
    }

    public int getWord(int word){
        return (Integer) this.words.get(word);
    }

    public List getWords() {
        return words;
    }

    public void setWord(int word, int value){
        this.words.set(word,value);
    }

    public void setWords(List words) {
        this.words = words;
    }
}
