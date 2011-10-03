package com.moosemanstudios.WorldGameMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
import org.bukkit.util.config.Configuration;

public class WorldGameMode extends JavaPlugin {
	public Logger log = Logger.getLogger("minecraft");
	public HashMap<World, GameMode> worldGameModes = new HashMap<World, GameMode>();
	static String mainDirectory = "plugins/WorldGameMode";
	final String survival = "survival";
	final String creative = "creative";
	Configuration conf;
	PluginManager pm;
	private final WGMPlayerListener playerlistener = new WGMPlayerListener(this);
	
	public void onDisable() {
		log.info("[WorldGameMode] is disabled");
		
	}

	public void onEnable() {
		
		// create the config if it doesn't exist
		conf = this.getConfiguration();
		create_config();
		
		reload_config();
		
		pm = this.getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_JOIN, playerlistener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_CHANGED_WORLD, playerlistener, Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		
		String[] split = args;
		String commandName = cmd.getName().toLowerCase();
		
		if (commandName.equalsIgnoreCase("wgm")) {
			// make sure they actually added terms to the command
			if (split.length == 0) {
				sender.sendMessage(ChatColor.RED + "Type " + ChatColor.WHITE + "/wgm help" + ChatColor.RED + " for help");
				return true;
			}
			
			if (split[0].equalsIgnoreCase("help")) {
				player.sendMessage(ChatColor.YELLOW + "WorldGameMode help");
				player.sendMessage(ChatColor.GRAY + "-----------------------------------");
				player.sendMessage(ChatColor.RED + "/wgm help" + ChatColor.WHITE + ": Display the help screen");
				
				// display the help menu based on the users permissions
				if (player.hasPermission("wgm.reload")) {
					player.sendMessage(ChatColor.RED + "/wgm reload" + ChatColor.WHITE + ": Reloads config file");
				}
				if (player.hasPermission("wgm.change")) {
					player.sendMessage(ChatColor.RED + "/wgm change [world] (creative/survival)" + ChatColor.WHITE + ": Changes specified worlds game mode");
				}
				if (player.hasPermission("wgm.list")) {
					player.sendMessage(ChatColor.RED + "/wgm list" + ChatColor.WHITE + ": Lists the worlds and current game type");
				}
				
				return true;
			}
			
			if (split[0].equalsIgnoreCase("reload")) {
				if (player.hasPermission("wgm.reload")) {
					reload_config();
					player.sendMessage("WorldGameMode config reloaded");
					log.info("[WorldGameMode] " + player.getName() + " reloaded config");
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
					return true;
				}
			}
			
			if (split[0].equalsIgnoreCase("change")) {
				String worldChange = split[1];
				String modeChange = split[2];
				
				// check and make sure that the user specified a valid game mode
				if (!(modeChange.equalsIgnoreCase(survival) || modeChange.equalsIgnoreCase(creative) )) {
					player.sendMessage(ChatColor.RED + "Unrecognized game mode: " + modeChange);
					return true;
				}
				
				// check that the world they specified is valid
				// based on the server not hte hash, possible they created a new world
				World tempWorld = this.getServer().getWorld(worldChange);	
				if (tempWorld == null) {
					player.sendMessage(ChatColor.RED + "Invalid world: " + worldChange);
					return true;
				}
				
				// at this point the input is valid, go ahead and add/update the hashmap
				if (modeChange.equalsIgnoreCase(survival)) {
					worldGameModes.put(tempWorld, GameMode.SURVIVAL);
				} else {
					worldGameModes.put(tempWorld, GameMode.CREATIVE);
				}
				
				// at this point, get all the players on the specified world and change there game mode to reflect the change
				List<Player> worldPlayers = this.getServer().getWorld(worldChange).getPlayers();
				for (Player tempPlayer : worldPlayers) {
					playerlistener.change_player_mode(tempPlayer, this.getServer().getWorld(worldChange));
					tempPlayer.sendMessage("Worlds game mode has been changed");
				}
				
				player.sendMessage("World " + tempWorld.getName() + " mode successfully changed to " + modeChange);
					
				return true;
			}
			
			if (split[0].equalsIgnoreCase("list")) {
				if (player.hasPermission("wgm.list")) {
					player.sendMessage(ChatColor.YELLOW + "WorldGameMode");
					player.sendMessage(ChatColor.GRAY + "-----------------------------------");
					
					// get the list of keys to use for iterating
					for (World key : worldGameModes.keySet()) {
						GameMode tempMode = worldGameModes.get(key);
						
						if (tempMode == GameMode.SURVIVAL) {
							player.sendMessage(key.getName() + ": " + ChatColor.BLUE +  "SURVIVAL");
						} else {
							player.sendMessage(key.getName() + ": " + ChatColor.GREEN + "CREATIVE");
						}
					}	
					
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
					return true;
				}
			}
		}		
		
		return false;
	}
	
	private void create_config() {
		new File(mainDirectory).mkdir();

		// get list of all the worlds
		List<World> worlds = this.getServer().getWorlds();
		
		// loop through the worlds and see if they exist in the config
		for (World world : worlds) {
			if (!propertyExists(world.getName())) {
				conf.setProperty(world.getName(), survival);
			}
		}
		conf.save();
		
	}
	
	private void reload_config() {
		conf.load();
		
		List<String> worldKeys = conf.getKeys();
		
		for (String world : worldKeys) {
			World tempWorld = this.getServer().getWorld(world);
			String gameMode = conf.getString(world);
			
			// make sure the world exists
			if (tempWorld != null) {
				if (gameMode.equalsIgnoreCase(survival)) {
					worldGameModes.put(tempWorld, GameMode.SURVIVAL);
					log.info("[WorldGameMode] world " + tempWorld.getName() + "; gamemode survival");
				} else {
					worldGameModes.put(tempWorld, GameMode.CREATIVE);
					log.info("[WorldGameMode] world " + tempWorld.getName() + "; gamemode creative");
				}
			}
		}
		
		
	}
	
	private boolean propertyExists(String path) {
		return this.getConfiguration().getProperty(path) != null;
	}
}
