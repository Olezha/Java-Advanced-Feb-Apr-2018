package com.flowergarden.run;

import java.math.BigDecimal;
import java.util.Collection;

import com.flowergarden.flowers.Flower;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.flowergarden.bouquet.*;
import com.flowergarden.flowers.Rose;

public class BouquetTest {

    private Bouquet<Flower> bouquet;

    @Before
    public void initBouquet() {
        bouquet = new MarriedBouquet();
        bouquet.addFlower(new Rose());
    }

    @Test
    public void getPriceTest() {
        BigDecimal price = bouquet.getPrice();
        Assert.assertEquals(new BigDecimal(120), price);
    }

    @Test
    public void searchFlowersByLengthTest() {
        Collection<Flower> flowers = bouquet.searchFlowersByLength(0, 0);
        Assert.assertEquals(flowers.isEmpty(), false);
    }
}
