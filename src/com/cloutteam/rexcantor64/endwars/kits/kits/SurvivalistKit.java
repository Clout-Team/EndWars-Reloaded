package com.cloutteam.rexcantor64.endwars.kits.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cloutteam.rexcantor64.endwars.Main;
import com.cloutteam.rexcantor64.endwars.kits.Kit;

import uk.endercraft.endercore.utils.CustomStack;

public class SurvivalistKit extends Kit {

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public boolean canUse(Player p) {
		return true;
	}

	@Override
	public void giveItems(Player p) {
		p.setMaxHealth(24);
		p.setHealth(24);
		p.getInventory().addItem(new ItemStack(Material.STONE_AXE));
	}

	@Override
	public boolean isListener() {
		return false;
	}

	@Override
	public ItemStack getShowcaseItem(Player p) {
		return new CustomStack().setMaterial(Material.LEATHER_CHESTPLATE)
				.setDisplayName((canUse(p) ? ChatColor.GREEN : ChatColor.RED) + "Survivalist")
				.setEnchantments(
						Main.get().getKit(p) == getID() ? new Object[] { Enchantment.DURABILITY, "1" } : new Object[0])
				.setItemInfoHidden(true).build();
	}

	@Override
	public String getName() {
		return "Survivalist";
	}

}
