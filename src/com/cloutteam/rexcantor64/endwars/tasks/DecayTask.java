package com.cloutteam.rexcantor64.endwars.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.cloutteam.rexcantor64.endwars.Main;

import uk.endercraft.endercore.GameState;

public class DecayTask extends BukkitRunnable {

	private int radius = -1;
	private Location center;

	public void run() {
		Main.get().updateScoreboard();
		if (Main.get().getGameState() == GameState.PLAYING) {
			Main.get().decreaseTimer();
			int timer = Main.get().getTimer();
			if (timer == 0) {
				Main.get().startDragon();
				return;
			}
			if ((timer % 60 == 0 || timer <= 5) && timer > 0)
				Main.get().broadcast("countdown.dragon", timer);

			if (!Main.get().isDragon())
				return;

			if (center == null)
				center = new Location(Bukkit.getWorld("world"), Main.get().getConfig().getInt("centerLocation.x"),
						Main.get().getConfig().getInt("centerLocation.y"),
						Main.get().getConfig().getInt("centerLocation.z"));
			if (radius == -1)
				radius = Main.get().getConfig().getInt("mapdecay.start-radius", 256);

			if (radius > Main.get().getConfig().getInt("mapdecay.stop-radius", 30))
				a(center.toVector(), radius, center.getWorld());
		}
	}

	@SuppressWarnings("deprecation")
	private void a(Vector center, int radius, World world) {
		int cx = center.getBlockX();
		int cy = center.getBlockY();
		int cz = center.getBlockZ();
		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				Location loc = new Location(world, x, cy, z);
				double d = center.distance(loc.toVector());
				if ((d > radius - 1) && (d < radius + 1))
					for (int y = 0; y <= world.getMaxHeight(); y++) {
						loc.setY(y);
						if (loc.getBlock().getType() != Material.AIR) {
							if (radius % Main.get().getConfig().getInt("mapdecay.decay-animation-interval", 4) == 0) {
								final FallingBlock fb = world.spawnFallingBlock(
										loc.getBlock().getLocation().clone().add(0.5, 0.5, 0.5),
										loc.getBlock().getType(), loc.getBlock().getData());
								Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
									@Override
									public void run() {
										fb.remove();
									}
								}, 19L);
							}
							loc.getBlock().setType(Material.AIR);

						}
					}
			}
		}
		this.radius--;
	}

}
