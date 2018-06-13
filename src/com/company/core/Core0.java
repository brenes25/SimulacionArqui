package com.company.core;

import com.company.Context;
import javafx.util.Pair;

public class Core0 extends Core {
    private Context mainContext;
    private Context secondaryContext;

    private Pair reservedPosition;

    public Core0 (){
        this.reservedPosition = new Pair<String,Integer>("",-1);
    }
}
