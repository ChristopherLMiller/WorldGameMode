package com.moosemanstudios.WorldGameMode;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGameMode extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	public HashMap<World, GameMode> worldGameModes = new HashMap<World, GameMode>();
	static String mainDirectory = "plugins/WorldGameMode";
	final String survival = "survival";
	final String creative = "creative";
	private PluginManager pm;
	private final WGMPlayerListener playerlistener = new WGMPlayerListener(this);
	
	public void onDisable() {
		log.info("[WorldGameMode] is disabled");
		save_config();
		
	}

	public void onEnable() {
		// create and load the config
		create_config();
		load_config();
		reload_players();
	
		pm = this.getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_JOIN, playerlistener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_CHANGED_WORLD, playerlistener, Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String commandName = cmd.getName().toLowerCase();
		
		if (commandName.equalsIgnoreCase("wgm")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Tupe " + ChatColor.WHITE + "/wgm help" + ChatColor.RED + " for help");
			}
			
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage("WorldGameMode help");
				sender.sendMessage("----------------------------------------");
				sender.sendMessage(ChatColor.RED + "/wgm help" + ChatColor.WHITE + ": Display the help screen");
				
				if (sender.hasPermission("wgm.admin")) {
					sender.sendMessage(ChatColor.RED + "/wgm reload" + ChatColor.WHITE + ": Reloads the config file");
					sender.sendMessage(ChatColor.RED + "/wgm change [world] (creative/survival)" + ChatColor.WHITE + ": Changes specified worlds game mode");
				}
				if (sender.hasPermission("wgm.list")) {
					sender.sendMessage(ChatColor.RED + "/wgm list" + ChatColor.WHITE + ": Lists what mode each world is in");
				}
				if (sender.hasPermission("wgm.mode")) {
					sender.sendMessage(ChatColor.RED + "/wgm mode (creative/survival)" + ChatColor.WHITE + ": Allows user to change there mode seperate from world");
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("wgm.admin")) {
					load_config();
					reload_players();
					sender.sendMessage("WorldGameMode config reloaded");
					log.info("[WorldGameMode] " + sender.getName() + " reloaded config");
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("mode")) {
				if (sender instanceof Player) {
					if (sender.hasPermission("wgm.mode")) {
						String mode = args[1];
						
						if (mode.equalsIgnoreCase(creative)) {
							((Player) sender).setGameMode(GameMode.CREATIVE);
						} else if (mode.equalsIgnoreCase(survival)) {
							((Player) sender).setGameMode(GameMode.SURVIVAL);
						} else {
							sender.sendMessage(ChatColor.RED + "Invalid mode");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Sorry, this command is only available to players");
				}
				
				return true;
			}
			
			if (args[0].equalsIgnoreCase("change")) {
				String world = args[1];
				String newMode = args[2];
				
				if (newMode.equalsIgnoreCase(survival)) {
					World tempWorld = this.getServer().getWorld(world);
					
					if (tempWorld != null) {
						worldGameModes.put(tempWorld, GameMode.SURVIVAL);
						getConfig().set(tempWorld.getName(), survival);
						saveConfig();
						reload_players();
						log.info("[WorldGameMode] " + world + " game mode changed to " + newMode);
						sender.sendMessage(world + " mode changed to SURVIVAL successfully!");
					} else {
						sender.sendMessage(ChatColor.RED + "Invalid world: " + world);
					}
				} else if (newMode.equalsIgnoreCase(creative)) {
					World tempWorld = this.getServer().getWorld(world);
					if (tempWorld != null) {
						worldGameModes.put(tempWorld, GameMode.CREATIVE);
						getConfig().set(tempWorld.getName(), creative);
						saveConfig();
						reload_players();
						log.info("[WorldGameMode] " + world + " game mode changed to " + newMode);
						sender.sendMessage(world + " mode changed to CREATIVE successfully!");
					} else {
						sender.sendMessage(ChatColor.RED + "Invalid world: " + world);
					}
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				if (sender.hasPermission("wgm.list")) {
					sender.sendMessage("WorldGameMode");
					sender.sendMessage("--------------------------------------------");
					
					for (World key : worldGameModes.keySet()) {
						if (worldGameModes.get(key) == GameMode.SURVIVAL) {
							sender.sendMessage(key.getName() + ": " + ChatColor.BLUE + "SURVIVAL");
						} else {
							sender.sendMessage(key.getName() + ": " + ChatColor.GREEN + "CREATIVE");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
				}
				return true;
			}
		}
		return false;
	}
	
	private void create_config() {
		List<World> worlds = this.getServer().getWorlds();
		
		for (World world : worlds) {
			// see if the world has a value stored
			if (!getConfig().contains(world.getName())) {
				// world doesn't exist yet, lets add it now
				getConfig().set(world.getName(), survival);
			}
		}
		
		saveConfig();
	}
	
	private void load_config() {
		this.reloadConfig();
		
		// get copy of they keys
		Set<String> keys = this.getConfig().getKeys(true);
		
		for (String key : keys) {
			World world = this.getServer().getWorld(key);
			
			if (this.getConfig().getString(key).equalsIgnoreCase(creative)) {
				worldGameModes.put(world, GameMode.CREATIVE);
				log.info("[WorldGameMode] loaded world: " + world.getName() + " mode: CREATIVE");
			} else if (this.getConfig().getString(key).equalsIgnoreCase(survival)) {
				worldGameModes.put(world, GameMode.SURVIVAL);
				log.info("[WorldGameMode] loaded world: " + world.getName() + " mode: SURVIVAL");
			} else {
				log.info("[WorldGameMode] Error loading record in save file! Mode not found for world " + world.getName());
			}
		}
	}
	
	private void save_config() {
		for (World key : worldGameModes.keySet()) {
			if (worldGameModes.get(key) == GameMode.CREATIVE) {
				getConfig().set(key.getName() , null);
				getConfig().set(key.getName() , creative);
				log.info("[WorldgameMode] saved world: " + key.getName() + " mode: CREATIVE");
			} else if (worldGameModes.get(key) == GameMode.SURVIVAL) {
				getConfig().set(key.getName(), null);
				getConfig().set(key.getName(), survival);
				log.info("[WorldGameMode] saved world: " + key.getName() + " mode: SURVIVAL");
			} else {
				log.info("[WorldGameModes] Error saving config file!");
			}
		}
		
		saveConfig();
	}
	
	private void reload_players() {
		Player[] players = this.getServer().getOnlinePlayers();
		
		for (Player player : players) {
			World world = player.getWorld();
			if (worldGameModes.get(world) == GameMode.CREATIVE) {
				player.setGameMode(GameMode.CREATIVE);
			} else {
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
	}
}
