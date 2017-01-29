package com.cloutteam.rexcantor64.endwars.utils;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.cloutteam.rexcantor64.endwars.config.ChestConfig;
import com.cloutteam.rexcantor64.endwars.config.ChestConfig.ChestItem;
import com.google.common.collect.Maps;

public class RandomManager {

	private Random r = new Random();
	private static RandomManager instance;

	/**
	 * Get random items for a specific amount of chest slots.
	 * 
	 * @author Rexcantor64
	 * @param chestAmount
	 *            The size of the map.
	 * @return Returns a map with where key=slot and value=item
	 * @since v2.0.0-SNAPSHOT
	 */
	public Map<Integer, ItemStack> getRandomItems(int chestAmount) {
		Map<Integer, ItemStack> items = Maps.newHashMap();
		List<ChestItem> cItems = ChestConfig.get().getAllChestItems();
		int airPercent = 80;
		for (int i = 0; i < chestAmount; i++) {
			if (r.nextInt(100) <= airPercent) {
				items.put(i, new ItemStack(Material.AIR));
				continue;
			}
			double totalWeight = 0.0d;
			for (ChestItem item : cItems) {
				totalWeight += item.getChance();
			}
			int randomIndex = -1;
			double random = Math.random() * totalWeight;
			for (int i2 = 0; i2 < cItems.size(); ++i2) {
				random -= cItems.get(i2).getChance();
				if (random <= 0.0d) {
					randomIndex = i2;
					break;
				}
			}
			items.put(i, cItems.get(randomIndex).toBukkit());
		}
		return items;
	}

	public static RandomManager get() {
		if (instance == null)
			instance = new RandomManager();
		return instance;
	}

}
