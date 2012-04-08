package me.itsatacoshop247.DailyBonus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyBonus extends JavaPlugin 
{
	public static Economy econ = null;
	
	public List<Player> playerList = new ArrayList<Player>();
	
	public HashMap<Player, Integer> numEarly = new HashMap<Player, Integer>();
	
	File configFile;
	File playersFile;
	FileConfiguration config;
	FileConfiguration players;
	
	public void onDisable() 
	{
		getServer().getScheduler().cancelTasks(this);
		Player[] players = this.getServer().getOnlinePlayers();
		if(players.length > 0)
		{
			for(int x = 0; x < players.length; x++)
			{
				this.players.set(("Players." + players[x].getName() + ".Last"), System.currentTimeMillis());
				this.savePlayers();
			}
		}
	}
	
	public void onEnable() 
	{
		new DailyBonusPlayerListener(this);
		getServer().getPluginManager().registerEvents(new DailyBonusPlayerListener(this), this);
		setupEconomy();
		
		this.configFile = new File(getDataFolder(), "config.yml");
		this.playersFile = new File(getDataFolder(), "players.yml");
		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.config = new YamlConfiguration();
		this.players = new YamlConfiguration();
		loadYamls();
		config.options().copyDefaults(true);
		players.options().copyDefaults(true);
	}
	
	private void firstRun() throws Exception {
		if (!this.playersFile.exists()) {
			this.playersFile.getParentFile().mkdirs();
			copy(getResource("players.yml"), this.playersFile);
			this.configFile.delete();
		}
		if (!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), this.configFile);
		}
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadYamls() {
		try {
			this.config.load(this.configFile);
			this.players.load(this.playersFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void savePlayers() {
		try {
			this.players.save(this.playersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}