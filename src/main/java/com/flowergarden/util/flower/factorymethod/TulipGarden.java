package com.flowergarden.util.flower.factorymethod;

import com.flowergarden.model.flower.Flower;
import com.flowergarden.model.flower.Tulip;

public class TulipGarden implements Flowergarden {

    @Override
    public Flower grow() {
        return new Tulip();
    }
}
