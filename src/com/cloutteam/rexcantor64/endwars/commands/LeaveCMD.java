package com.cloutteam.rexcantor64.endwars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.endercraft.endercore.managers.BungeeManager;

public class LeaveCMD implements CommandExecutor {

	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (s instanceof Player)
			BungeeManager.sendToServer((Player) s, "hub");
		return true;
	}

}
