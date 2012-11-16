package com.github.Wilwark.VoteReminder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.DateTime;

import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteReminder extends JavaPlugin implements Listener {

	public String VoteMessage = "&9%player%, vote at &cvote.minecraftserver.com!";
	public Votifier votifier;
	private final Map<String, DateTime> haveVoted = new HashMap<String, DateTime>();
	public int time = 60;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;	
	@Override
	public void onEnable() {
		Plugin votifier = getServer().getPluginManager().getPlugin("Votifier");
		if (!(votifier.isEnabled())) {
			getLogger().info("Votifier not found. Cannot activate VoteReminder.");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		
		this.VoteMessage = this.getConfig().getString("Message", this.VoteMessage);
		this.VoteMessage = ChatColor.translateAlternateColorCodes('&', this.VoteMessage);
		this.time = this.getConfig().getInt("time");

		getConfig().set("Message", this.VoteMessage);
		getConfig().set("time", this.time);
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getvotedSet().get("haveVoted", this.haveVoted);		
		
		this.sendReminder();
	}

	public void onDisable() {
		this.getvotedSet().set("haveVoted", this.haveVoted);
		this.savevotedSet();
		this.saveConfig();
	}

	@EventHandler
	public void hasVoted(VotifierEvent event) {
		Player[] playerList = getServer().getOnlinePlayers();
		for (final Player player : playerList) {
			if (event.getVote().getUsername().equalsIgnoreCase(player.getName())) {
				haveVoted.put(player.getName(), DateTime.now());				
			}
		}		
	}

	public void sendReminder() {
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					public void run() {													
							Player[] playerList = Bukkit.getServer().getOnlinePlayers();
							for (Player player : playerList) {
								if(haveVoted.get(player) != null){
									if(DateTime.now().isAfter((haveVoted.get(player.getName()).plusHours(24))) || DateTime.now().isEqual((haveVoted.get(player.getName()).plusHours(24)))){
										haveVoted.remove(player);							
									}								
								}
								if (!(player.hasPermission("VoteReminder.exempt"))) {
									if (!(haveVoted.containsKey(player.getName())) || haveVoted == null) {
										VoteMessage.replaceAll("%player%", player.getName());
										player.sendMessage(VoteMessage);								
									}
								}
							}
					}
				}, 20L , (time * 20));		
	}
	
	public void reloadvotedSet() {
	    if (customConfigFile == null) {
	    customConfigFile = new File(getDataFolder(), "customConfig.yml");
	    }
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("customConfig.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getvotedSet() {
	    if (customConfig == null) {
	        this.reloadvotedSet();
	    }
	    return customConfig;
	}
	
	public void savevotedSet() {
	    if (customConfig == null || customConfigFile == null) {
	    return;
	    }
	    try {
	        getvotedSet().save(customConfigFile);
	    } catch (IOException ex) {
	        this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
	    }
	}
	
}