package com.gildedrose;

import static com.gildedrose.Constants.AGED_BRIE;
import static com.gildedrose.Constants.APPRECIATION_RATE;
import static com.gildedrose.Constants.BACK_STAGE;
import static com.gildedrose.Constants.BREAK_POINT;
import static com.gildedrose.Constants.CONJURED;
import static com.gildedrose.Constants.DEPRECIATION_RATE;
import static com.gildedrose.Constants.LOWER_BOUND;
import static com.gildedrose.Constants.SELLIN_MOVE;
import static com.gildedrose.Constants.STAGE_ONE;
import static com.gildedrose.Constants.STAGE_TWO;
import static com.gildedrose.Constants.SULFURAS;
import static com.gildedrose.Constants.SULFURAS_QUALITY;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Strategies {

  private static Map<String, Consumer<Item>> strategies = new HashMap<>();
  private static Consumer<Item> updateNormalItem = item -> {
    item.quality -= DEPRECIATION_RATE;
    if (item.sellIn <= BREAK_POINT) {
      item.quality -= DEPRECIATION_RATE;
    }
    item.sellIn -= SELLIN_MOVE;
  };
  private static Consumer<Item> checkQualityBounds = item -> {
    if (item.quality > Constants.UPPER_BOUND) {
      item.quality = Constants.UPPER_BOUND;
    }
    if (item.quality < Constants.LOWER_BOUND) {
      item.quality = Constants.LOWER_BOUND;
    }
  };

  private static Consumer<Item> updateAgedBrie = item -> {
    item.quality += APPRECIATION_RATE;
    if (item.sellIn <= BREAK_POINT) {
      item.quality += APPRECIATION_RATE;
    }
    item.sellIn -= SELLIN_MOVE;
  };

  private static Consumer<Item> updateSulfuras = item -> item.quality = SULFURAS_QUALITY;

  private static Consumer<Item> updateBackStage = item -> {
    int previousSellIn = item.sellIn;
    item.quality += APPRECIATION_RATE;
    if (previousSellIn < STAGE_ONE) {
      item.quality += APPRECIATION_RATE;
    }
    if (previousSellIn < STAGE_TWO) {
      item.quality += APPRECIATION_RATE;
    }
    if (previousSellIn <= BREAK_POINT) {
      item.quality = LOWER_BOUND;
    }
    item.sellIn -= SELLIN_MOVE;
  };

  private static Consumer<Item> updateConjured = item -> {
    item.quality -= DEPRECIATION_RATE * 2;
    if (item.sellIn <= BREAK_POINT) {
      item.quality -= DEPRECIATION_RATE * 2;
    }
    item.sellIn -= SELLIN_MOVE;
  };

  static {
    strategies.put(AGED_BRIE, updateAgedBrie.andThen(checkQualityBounds));
    strategies.put(SULFURAS, updateSulfuras);
    strategies.put(BACK_STAGE, updateBackStage.andThen(checkQualityBounds));
    strategies.put(CONJURED, updateConjured.andThen(checkQualityBounds));
  }

  public static Consumer<Item> getUpdateStrategy(Item item) {
    return strategies.getOrDefault(item.name, updateNormalItem.andThen(checkQualityBounds));
  }
}
