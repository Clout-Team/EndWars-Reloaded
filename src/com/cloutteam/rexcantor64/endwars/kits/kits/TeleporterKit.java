package com.cloutteam.rexcantor64.endwars.kits.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.cloutteam.rexcantor64.endwars.Main;
import com.cloutteam.rexcantor64.endwars.kits.Kit;

import uk.endercraft.endercore.utils.CustomStack;

public class TeleporterKit extends Kit {

	@Override
	public int getID() {
		return 2;
	}

	@Override
	public boolean canUse(Player p) {
		return true;
	}

	@Override
	public void giveItems(Player p) {
		p.getInventory().addItem(new ItemStack(Material.STONE_AXE));
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, 0));
		p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		p.getInventory().setItem(8, new ItemStack(Material.ENDER_PEARL, 4));
	}

	@Override
	public boolean isListener() {
		return false;
	}

	@Override
	public ItemStack getShowcaseItem(Player p) {
		return new CustomStack().setMaterial(Material.ENDER_PEARL)
				.setDisplayName((canUse(p) ? ChatColor.GREEN : ChatColor.RED) + "Teleporter")
				.setEnchantments(
						Main.get().getKit(p) == getID() ? new Object[] { Enchantment.DURABILITY, "1" } : new Object[0])
				.setItemInfoHidden(true).build();
	}

	@Override
	public String getName() {
		return "Teleporter";
	}

}
