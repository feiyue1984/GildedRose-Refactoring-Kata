package com.gildedrose;

import static com.gildedrose.Constants.DEPRECIATION_RATE;
import static com.gildedrose.Constants.SELLIN_MOVE;
import static com.gildedrose.Constants.SULFURAS_QUALITY;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class GildedRoseTest {

    private GildedRose app;
    private Item[] control;

    private static void iterateBy(GildedRose app, int times) {
        IntStream.range(0, times).forEach(i -> app.updateQuality());
    }

    @Test
    public void foo() {
        Item[] items = new Item[] { new Item("foo", 0, 0) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals("foo", app.items[0].name);
    }

    @Test
    public void qualityDegradesTillSellIn() {
        control = new Item[]{
            new Item("ABC", 10, 20), //
            new Item("DEF", 1, 10), //
            new Item("X Y Z", 5, 7)}; //

        app = new GildedRose(copyControl());
        app.updateQuality();
        Item[] processed = app.items;
        assertThat(processed).hasSameSizeAs(control);
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(SELLIN_MOVE);
            assertThat(before.quality - after.quality).isEqualTo(SELLIN_MOVE);
        });
    }

    @Test
    public void qualityDegradesTwiceFastOnceSellInPassed() {
        control = new Item[]{
            new Item("ABC", -3, 20), //
            new Item("DEF", -1, 10), //
            new Item("X Y Z", -4, 7)}; //
        app = new GildedRose(copyControl());
        app.updateQuality();
        Item[] processed = app.items;
        assertThat(processed).hasSize(control.length);
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(SELLIN_MOVE);
            assertThat(before.quality - after.quality).isEqualTo(DEPRECIATION_RATE * 2);
        });
    }

    @Test
    public void qualityCannotBeNegative() {
        control = new Item[]{
            new Item("ABC", -3, 1), //
            new Item("DEF", -1, 2), //
            new Item(Constants.CONJURED, -4, 3)}; //
        app = new GildedRose(copyControl());
        iterateBy(app, 20);
        Item[] processed = app.items;
        assertThat(processed).noneMatch(item -> item.quality < 0);
    }

    @Test
    public void agedBrieGainsQualityAsGetOlder() {
        control = new Item[]{
            new Item(Constants.AGED_BRIE, 10, 1), //
            new Item(Constants.AGED_BRIE, 12, 2), //
        }; //
        app = new GildedRose(copyControl());
        int days = 5;
        iterateBy(app, days);
        Item[] processed = app.items;
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(days);
            assertThat(after.quality - before.quality).isEqualTo(days);
        });
    }

    @Test
    public void agedBrieGainsQualityTwiceAfterExpiration() {
        control = new Item[]{
            new Item(Constants.AGED_BRIE, 0, 1), //
            new Item(Constants.AGED_BRIE, -3, 2), //
        }; //
        app = new GildedRose(copyControl());
        int days = 5;
        iterateBy(app, days);
        Item[] processed = app.items;
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(days);
            assertThat(after.quality - before.quality).isEqualTo(days * 2);
        });
    }

    @Test
    public void qualityCannotExceed50() {
        control = new Item[]{
            new Item(Constants.AGED_BRIE, 10, 48),
            new Item(Constants.AGED_BRIE, 8, 50)
        };
        app = new GildedRose(control);
        int days = 100;
        iterateBy(app, days);
        Item[] processed = app.items;
        assertThat(processed).noneMatch(item -> item.quality > Constants.UPPER_BOUND);
    }

    @Test
    public void sulfurasHasConstantSellInAndQuality() {
        control = new Item[]{
            new Item(Constants.SULFURAS, 1, SULFURAS_QUALITY),
            new Item(Constants.SULFURAS, 2, SULFURAS_QUALITY),
            new Item(Constants.SULFURAS, 3, SULFURAS_QUALITY),
            new Item(Constants.SULFURAS, 4, SULFURAS_QUALITY)
        };
        app = new GildedRose(copyControl());
        int days = 100;
        iterateBy(app, days);
        Item[] processed = app.items;
        assertThat(control).hasSameSizeAs(processed);
        assertThat(processed).allMatch(item -> item.quality == SULFURAS_QUALITY);
    }

    @Test
    public void conjuredDegradesTwiceFaster() {
        control = new Item[]{
            new Item("ABC", 5, 20), //
            new Item(Constants.CONJURED, 5, 30)}; //
        app = new GildedRose(copyControl());
        int days = 3;
        iterateBy(app, days);
        Item[] processed = app.items;
        int sellInDeltaGeneralItem = control[0].sellIn - processed[0].sellIn;
        int sellInDeltaConjured = control[1].sellIn - processed[1].sellIn;
        assertThat(sellInDeltaGeneralItem).isEqualTo(sellInDeltaConjured).isEqualTo(days);
        int qualityDeltaGeneralItem = control[0].quality - processed[0].quality;
        int qualityDeltaConjured = control[1].quality - processed[1].quality;
        assertThat(qualityDeltaGeneralItem * 2).isEqualTo(qualityDeltaConjured);
    }

    @Test
    public void backStageIncreaseInQualityWithAge() {
        control = new Item[]{
            new Item(Constants.BACK_STAGE, 20, 18),
            new Item(Constants.BACK_STAGE, 15, 16),
        };
        app = new GildedRose(copyControl());
        int days = 5;
        iterateBy(app, days);
        Item[] processed = app.items;
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(days);
            assertThat(after.quality - before.quality).isEqualTo(days);
        });
    }

    @Test
    public void backStageSellInBetween10And6() {
        control = new Item[]{
            new Item(Constants.BACK_STAGE, 10, 18),
            new Item(Constants.BACK_STAGE, 8, 16),
        };
        app = new GildedRose(copyControl());
        int days = 2;
        iterateBy(app, days);
        Item[] processed = app.items;
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(days);
            assertThat(after.quality - before.quality).isEqualTo(days * 2);
        });
    }

    @Test
    public void backStageSellInBetween5And0() {
        control = new Item[]{
            new Item(Constants.BACK_STAGE, 5, 18),
            new Item(Constants.BACK_STAGE, 2, 16),
        };
        app = new GildedRose(copyControl());
        int days = 2;
        iterateBy(app, days);
        Item[] processed = app.items;
        IntStream.range(0, control.length).forEach(index -> {
            Item before = control[index];
            Item after = processed[index];
            assertThat(before.sellIn - after.sellIn).isEqualTo(days);
            assertThat(after.quality - before.quality).isEqualTo(days * 3);
        });
    }

    @Test
    public void backStageZeroQualityAfterConcert() {
        control = new Item[]{
            new Item(Constants.BACK_STAGE, 2, 18),
            new Item(Constants.BACK_STAGE, 5, 16),
        };
        app = new GildedRose(control);
        int days = 10;
        iterateBy(app, days);
        Item[] processed = app.items;
        assertThat(processed).allMatch(item -> item.quality == 0);
    }

    private Item[] copyControl() {
        List<Item> list = Arrays.stream(control)
            .map(item -> new Item(item.name, item.sellIn, item.quality)).collect(
                Collectors.toList());
        Item[] output = new Item[list.size()];
        list.toArray(output);
        return output;
    }
}
