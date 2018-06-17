package com.company.blocks;

import java.util.List;

public class DataBlock {
   private List words;

    public DataBlock(List<Integer> words) {
        this.words = words;
    }

    public int getWord(int word){
        return (Integer) this.words.get(word);
    }
}
