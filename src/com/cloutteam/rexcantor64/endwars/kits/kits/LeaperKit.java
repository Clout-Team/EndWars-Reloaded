package com.cloutteam.rexcantor64.endwars.kits.kits;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.cloutteam.rexcantor64.endwars.Main;
import com.cloutteam.rexcantor64.endwars.kits.Kit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.endercraft.endercore.GameState;
import uk.endercraft.endercore.utils.CustomStack;

public class LeaperKit extends Kit {

	private List<Player> jumped = Lists.newArrayList();
	private Map<Player, Long> cooldown = Maps.newHashMap();

	@Override
	public int getID() {
		return 1;
	}

	@Override
	public boolean canUse(Player p) {
		return true;
	}

	@Override
	public void giveItems(Player p) {
		p.getInventory().addItem(new ItemStack(Material.STONE_AXE));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, 0));
	}

	@Override
	public boolean isListener() {
		return true;
	}

	@Override
	public ItemStack getShowcaseItem(Player p) {
		return new CustomStack().setMaterial(Material.STONE_AXE)
				.setDisplayName((canUse(p) ? ChatColor.GREEN : ChatColor.RED) + "Leaper")
				.setEnchantments(
						Main.get().getKit(p) == getID() ? new Object[] { Enchantment.DURABILITY, "1" } : new Object[0])
				.setItemInfoHidden(true).build();
	}

	@Override
	public String getName() {
		return "Leaper";
	}

	public int getCooldown(Player p) {
		if (!cooldown.containsKey(p))
			return -1;
		return (int) (this.cooldown.get(p) - (System.currentTimeMillis() / 1000));
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		if (this.cooldown.containsKey(p)) {
			long currentTime = System.currentTimeMillis() / 1000;
			int left = (int) (this.cooldown.get(p) - currentTime);
			if (left > 0) {
				e.setCancelled(true);
				return;
			}
			this.cooldown.remove(p);
		}
		if (Main.get().getKit(e.getPlayer()) == getID()) {
			if (p.getGameMode() == GameMode.CREATIVE)
				return;
			e.setCancelled(true);
			p.setAllowFlight(false);
			p.setFlying(false);

			p.setVelocity(p.getLocation().getDirection().setY(1));
			this.cooldown.put(p, (System.currentTimeMillis() / 1000) + 30);
			this.jumped.add(p);
			Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
				@Override
				public void run() {
					jumped.remove(p);
				}
			}, 100L);
		} else {
			p.setFallDistance(0.0F);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) throws InterruptedException {
		Player p = e.getPlayer();
		if (Main.get().getKit(p) == getID()) {
			if (this.cooldown.containsKey(p)) {
				long currentTime = System.currentTimeMillis() / 1000;
				int left = (int) (this.cooldown.get(p) - currentTime);
				if (left > 0)
					return;
				this.cooldown.remove(p);
			}
			if ((p.getGameMode() != GameMode.CREATIVE)
					&& (p.getLocation().subtract(0.0D, 1.0D, 0.0D).getBlock().getType() != Material.AIR)
					&& (!p.isFlying()) && Main.get().getGameState() == GameState.PLAYING) {
				p.setAllowFlight(true);
			}
		} else
			p.setFallDistance(0.0F);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && this.jumped.contains(e.getEntity())) {
			e.setCancelled(true);
			this.jumped.remove(e.getEntity());
		}
	}

}
