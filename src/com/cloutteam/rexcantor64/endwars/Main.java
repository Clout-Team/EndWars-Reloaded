package com.cloutteam.rexcantor64.endwars;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.cloutteam.rexcantor64.endwars.commands.AdminCMD;
import com.cloutteam.rexcantor64.endwars.commands.LeaveCMD;
import com.cloutteam.rexcantor64.endwars.kits.KitsManager;
import com.cloutteam.rexcantor64.endwars.listeners.BlockListener;
import com.cloutteam.rexcantor64.endwars.listeners.PlayerListener;
import com.cloutteam.rexcantor64.endwars.tasks.DecayTask;
import com.cloutteam.rexcantor64.endwars.tasks.TickTask;
import com.cloutteam.rexcantor64.endwars.utils.NMSUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.endercraft.endercore.EnderMinigame;
import uk.endercraft.endercore.EnderPlayer;
import uk.endercraft.endercore.GameState;
import uk.endercraft.endercore.language.LanguageMain;
import uk.endercraft.endercore.managers.BungeeManager;
import uk.endercraft.endercore.managers.PlayerManager;

public class Main extends EnderMinigame {

	private static Main instance;
	private int timer = 0;
	private HashMap<Player, Player> hits = Maps.newHashMap();
	private HashMap<Player, Integer> kits = Maps.newHashMap();
	private boolean dragon;
	private List<Location> islands = Lists.newArrayList();

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		saveResource("config.yml", false);
		if (!copyDefaultWorld())
			return;
		getLogger().info("Minigame world copied successfuly!");
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {

			public void run() {
				try {
					World world = Bukkit.getServer()
							.createWorld(new WorldCreator("world").environment(Environment.NORMAL));
					world.setGameRuleValue("doMobSpawning", "false");
					List<String> paths = new ArrayList<String>(
							getConfig().getConfigurationSection("spawnLocations").getKeys(false));
					for (String pathS : paths) {
						String path = "spawnLocations." + pathS + ".";
						islands.add(new Location(world, getConfig().getDouble(path + "x", 0),
								getConfig().getDouble(path + "y", 0), getConfig().getDouble(path + "z", 0),
								(float) getConfig().getDouble(path + "yaw", 0),
								(float) getConfig().getDouble(path + "pitch", 0)));
					}
					getLogger().info("World " + world.getName() + " loaded successfuly!");
				} catch (Exception ex) {
					forceDisable("failed to load the minigame world!");
					ex.printStackTrace();
					return;
				}
				gameState = GameState.WAITING;
			}
		}, 5L);
		registerEvent(new PlayerListener());
		registerEvent(new BlockListener());
		getCommand("admin").setExecutor(new AdminCMD());
		getCommand("leave").setExecutor(new LeaveCMD());

		new TickTask().runTaskTimer(this, 20L, 20L);
		new DecayTask().runTaskTimer(this, 20L, getConfig().getInt("maydecay.delay", 4) * 20);
		hits.clear();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	private boolean copyDefaultWorld() {
		File world = new File(getDataFolder(), "world");
		if (!world.exists()) {
			forceDisable(
					"plugin couldn't find the default world! Please make sure you have a world folder on your plugin directory.");
			return false;
		}
		File defaultWorld = new File(".", "world");
		try {
			FileUtils.copyDirectory(world, defaultWorld);
		} catch (IOException e) {
			forceDisable("plugin couldn't copy the minigame world to the new world! Error: " + e.getMessage());
		}
		return true;
	}

	public void setTimer(int seconds) {
		this.timer = seconds;
	}

	public int getTimer() {
		return this.timer;
	}

	public void decreaseTimer() {
		this.timer--;
	}

	public void teleportToArena() {
		Random r = new Random();
		List<Location> locs = new ArrayList<Location>(islands);
		for (Player p : Bukkit.getOnlinePlayers()) {
			int i = r.nextInt(locs.size());
			p.teleport(locs.get(i));
			locs.remove(i);
		}
	}

	public int getPlayingCount() {
		int i = 0;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getGameMode() == GameMode.SURVIVAL)
				i++;
		return i;
	}

	public Player getWinner() {
		if (getPlayingCount() != 1)
			return null;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getGameMode() == GameMode.SURVIVAL)
				return p;
		return null;
	}

	public void reloadGame() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager.getData(p).save();
			BungeeManager.sendToServer(p, "hub");
		}
		Bukkit.unloadWorld("world", false);
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				copyDefaultWorld();
				Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
					public void run() {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
					}
				}, 10L);
			}
		}, 10L);
	}

	public void setLastHit(Player victim, Player hitter) {
		hits.put(victim, hitter);
	}

	public Player getLastHitter(Player victim) {
		return hits.get(victim);
	}

	public void setKit(Player player, Integer kitId) {
		kits.put(player, kitId);
	}

	public Integer getKit(Player player) {
		return kits.get(player);
	}

	@SuppressWarnings("deprecation")
	public void startGame() {
		Main.get().setGameState(GameState.PLAYING);
		setTimer(getConfig().getInt("mapdecay.auto-activate-time", 300));
		Main.get().teleportToArena();
		broadcast("start");
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.getInventory().clear();
			NMSUtils.setItemCooldown(p, Material.ENDER_PEARL, 30);
			KitsManager.get().getById(getKit(p)).giveItems(p);
			p.sendTitle(LanguageMain.get(p, "start.title"), "");
			countPlay(PlayerManager.getData(p));
		}
	}

	public boolean isDragon() {
		return dragon;
	}

	public void startDragon() {
		this.dragon = true;
		broadcast("dragon");
	}

	public void updateScoreboard() {
		boolean lobbySb = gameState == GameState.WAITING || gameState == GameState.COUNTDOWN;
		for (Player p : Bukkit.getOnlinePlayers()) {
			Scoreboard sb = p.getScoreboard();
			if (sb.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
				sb = Bukkit.getScoreboardManager().getNewScoreboard();
				p.setScoreboard(sb);
			}
			try {
				sb.getObjective("endwars").unregister();
			} catch (Exception e) {
			}
			Objective obj = sb.registerNewObjective("endwars", "dummy");
			obj.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "END" + ChatColor.DARK_PURPLE
					+ ChatColor.BOLD + "WARS");
			obj.getScore("    ").setScore(11);
			obj.getScore(lobbySb ? LanguageMain.get(p, "sb.lobby.players") : LanguageMain.get(p, "sb.alive"))
					.setScore(10);
			obj.getScore(
					lobbySb ? LanguageMain.get(p, "sb.lobby.playercount", getPlayingCount(), Bukkit.getMaxPlayers())
							: ChatColor.GOLD + "" + getPlayingCount())
					.setScore(9);
			obj.getScore(" ").setScore(8);
			obj.getScore(LanguageMain.get(p, "sb.kit")).setScore(7);
			obj.getScore(LanguageMain.get(p, "sb.kit.name", KitsManager.get().getById(Main.get().getKit(p)).getName()))
					.setScore(6);
			obj.getScore("  ").setScore(5);
			obj.getScore(lobbySb ? LanguageMain.get(p, "sb.lobby.coins") : LanguageMain.get(p, "sb.dragon"))
					.setScore(4);
			obj.getScore(lobbySb ? ChatColor.GOLD + "" + PlayerManager.getData(p).getCoins()
					: (Main.get().isDragon() ? LanguageMain.get(p, "sb.dragon.active")
							: LanguageMain.get(p, "sb.dragon.unactive")))
					.setScore(3);
			obj.getScore("   ").setScore(2);
			obj.getScore(LanguageMain.get(p, "sb.ip")).setScore(-1);
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
	}

	public void sendStats(Player p) {
		EnderPlayer ep = PlayerManager.getData(p);
		p.sendMessage(LanguageMain.get(ep, "stats.lineseparator1"));
		p.sendMessage(" ");
		p.sendMessage(LanguageMain.get(ep, "stats.participation"));
		if (ep.getCacheData().getKills() != 0)
			p.sendMessage(LanguageMain.get(ep, "stats.kills", ep.getCacheData().getKills(),
					ep.getCacheData().getKills() * 15, ep.getCacheData().getKills()));
		if (getWinner() == p)
			p.sendMessage(LanguageMain.get(ep, "stats.win"));
		else
			p.sendMessage(LanguageMain.get(ep, "stats.died"));
		p.sendMessage(" ");
		p.sendMessage(LanguageMain.get(ep, "stats.lineseparator2"));
	}

	public void broadcast(String code, Object... complements) {
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(LanguageMain.get(p, code, complements));
	}

	public static Main get() {
		return instance;
	}

	public List<Location> getIslands() {
		return islands;
	}

	public void countKill(EnderPlayer ep) {
		ep.getCacheData().addKills(1);
		ep.getMinigameData("endwars").addKills(1);
		ep.addCoins(15);
		ep.getMinigameData("endwars").addXp(1);
	}

	public void countDeath(EnderPlayer ep) {
		ep.getCacheData().addDeaths(1);
		ep.getMinigameData("endwars").addDeaths(1);
		ep.getMinigameData("endwars").addXp(-1);
	}

	public void countPlay(EnderPlayer ep) {
		ep.getMinigameData("endwars").addPlayed(1);
		ep.addCoins(25);
	}

	public void countWin(EnderPlayer ep) {
		ep.getCacheData().addWins(1);
		ep.getMinigameData("endwars").addWins(1);
		ep.addCoins(50);
		ep.getMinigameData("endwars").addXp(1);
	}

	@Override
	public String getMinigameName() {
		return "endwars";
	}

}
