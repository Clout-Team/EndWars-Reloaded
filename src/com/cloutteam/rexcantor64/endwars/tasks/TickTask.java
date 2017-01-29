package com.cloutteam.rexcantor64.endwars.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.cloutteam.rexcantor64.endwars.Main;
import com.cloutteam.rexcantor64.endwars.kits.KitsManager;
import com.cloutteam.rexcantor64.endwars.kits.kits.LeaperKit;

import net.minecraft.server.v1_10_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;
import uk.endercraft.endercore.GameState;
import uk.endercraft.endercore.language.LanguageMain;

public class TickTask extends BukkitRunnable {

	@SuppressWarnings("deprecation")
	public void run() {
		Main.get().updateScoreboard();
		if (Main.get().getGameState() == GameState.COUNTDOWN) {
			int timer = Main.get().getTimer();
			if (timer == 0) {
				Main.get().startGame();
				return;
			}
			if (timer % 10 == 0 || timer <= 5) {
				Main.get().broadcast("countdown", timer);
				switch (timer) {
				case 5:
				case 4:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.GREEN + "" + timer, "");
					break;
				case 3:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.YELLOW + "" + timer, "");
					break;
				case 2:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.RED + "" + timer, "");
					break;
				case 1:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.DARK_RED + "" + timer, "");
					break;
				}
			}
			Main.get().decreaseTimer();
		} else if (Main.get().getGameState() == GameState.PLAYING) {
			LeaperKit kit = (LeaperKit) KitsManager.get().getById(1);
			for (Player p : Bukkit.getOnlinePlayers()) {
				int left = kit.getCooldown(p);
				if (left > 0)
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(
							ChatSerializer.a("{\"text\":\"" + LanguageMain.get(p, "leaper.cooldown", left) + "\"}"),
							(byte) 2));
			}
		}
	}
}
