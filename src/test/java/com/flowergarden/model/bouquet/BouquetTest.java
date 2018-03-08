package com.flowergarden.model.bouquet;

import com.flowergarden.model.flowers.Chamomile;
import com.flowergarden.model.flowers.Flower;
import com.flowergarden.model.flowers.Rose;
import com.flowergarden.model.properties.FreshnessInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static org.mockito.Mockito.mock;

public class BouquetTest {

    private Bouquet<Flower> bouquet;

    @Before
    public void initBouquet() {
        bouquet = new MarriedBouquet();
        bouquet.addFlower(new Rose());
        bouquet.addFlower(new Chamomile(0, 0, mock(BigDecimal.class), mock(FreshnessInteger.class)));
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