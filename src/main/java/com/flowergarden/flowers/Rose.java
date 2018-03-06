package com.flowergarden.flowers;

import javax.xml.bind.annotation.XmlRootElement;

import com.flowergarden.properties.FreshnessInteger;

import java.math.BigDecimal;

@XmlRootElement
public class Rose extends GeneralFlower {

    private boolean spike;

    public Rose() {
    }

    public Rose(boolean spike, int length, BigDecimal price, FreshnessInteger fresh) {
        this.spike = spike;
        this.length = length;
        this.price = price;
        this.freshness = fresh;
    }

    public boolean getSpike() {
        return spike;
    }
}
