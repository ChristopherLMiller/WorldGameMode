package com.moosemanstudios.WorldGameMode;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class WGMPlayerListener extends PlayerListener {
	WorldGameMode plugin;
	
	WGMPlayerListener(WorldGameMode instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		change_mode(event.getPlayer(), event.getPlayer().getWorld());
	}
	
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		change_mode(event.getPlayer(), event.getPlayer().getWorld());
	}
	
	private void change_mode(Player player, World world) {
		// alrighty, get the mode for the current world
		GameMode mode = plugin.worldGameModes.get(world);
		
		// check the current game mode of the player
		GameMode playerMode = player.getGameMode();
		
		if (playerMode != mode) {
			player.setGameMode(mode);
		}
	}
	
	public void change_player_mode(Player player, World world) {
		change_mode(player, world);
	}
	
}
