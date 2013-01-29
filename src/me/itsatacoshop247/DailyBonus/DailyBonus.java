package me.itsatacoshop247.DailyBonus;

import java.io.File;

//random amounts

//meta data on items
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyBonus extends JavaPlugin 
{
	public static Economy econ = null;
	
	public HashSet<String> playerList = new HashSet<String>();
	
	public HashMap<String, Integer> numEarly = new HashMap<String, Integer>();
	
	File configFile;
	FileConfiguration config;
	
	public Logger log = Logger.getLogger("Minecraft");
	
	public void onDisable() 
	{
		getServer().getScheduler().cancelTasks(this);
		Player[] players = this.getServer().getOnlinePlayers();
		if(players.length > 0)
		{
			for(int x = 0; x < players.length; x++)
			{
				File file = new File(this.getDataFolder().getAbsolutePath()+"/players/"+players[x].getName()+".yml");
				FileConfiguration pfile = new YamlConfiguration();
				try {
					pfile.load(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				pfile.set(("Time.Last"), System.currentTimeMillis());
				
				try {
					pfile.save(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void onEnable() 
	{
		new DailyBonusPlayerListener(this);
		getServer().getPluginManager().registerEvents(new DailyBonusPlayerListener(this), this);
		setupEconomy();
		
		this.configFile = new File(getDataFolder(), "config.yml");
		try 
		{
			firstRun();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		this.config = new YamlConfiguration();
		loadYamls();
		config.options().copyDefaults(true);
		
		this.updateConfig();
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("DailyBonus"))
		{
			if(args.length > 0)
			{
				if(args[0].equalsIgnoreCase("Reload"))
				{
					if(!sender.hasPermission("dailybonus.reload"))
					{
						sender.sendMessage(ChatColor.WHITE + "You don't have dailybonus.reload permissions!");
					}
					else
					{
						Player[] players = this.getServer().getOnlinePlayers();
						if(players.length > 0)
						{
							for(int x = 0; x < players.length; x++)
							{
								File file = new File(this.getDataFolder().getAbsolutePath()+"/players/"+players[x].getName()+".yml");
								FileConfiguration pfile = new YamlConfiguration();
								try {
									pfile.load(file);
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvalidConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								pfile.set(("Time.Last"), System.currentTimeMillis());
								
								try {
									pfile.save(file);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						
						this.loadYamls();
						sender.sendMessage(ChatColor.GOLD + "DailyBonus has been reloaded.");
						return true;
					}
				}
			}
			else
			{
				sender.sendMessage(ChatColor.YELLOW + this.getName() + " by " + this.getDescription().getAuthors() +". Version " + this.getDescription().getVersion() + ".");
				sender.sendMessage("Use '" + ChatColor.YELLOW + "/DailyBonus reload" + ChatColor.WHITE + "' to reload the plugin.");
				return true;
			}
		}
		return false;
	}
	
	private void updateConfig() 
	{
		HashMap<String, String> items = new HashMap<String, String>();
		
		items = loadConfigurables(items);
		
		int num = 0;
		for(Map.Entry<String, String> item : items.entrySet())
		{
			if(this.config.get(item.getKey()) == null)
			{
				if(item.getValue().equalsIgnoreCase("LIST"))
				{
					List<String> list = Arrays.asList("LIST ITEMS GO HERE");
					this.config.addDefault(item.getKey(), list);
				}
				else if(item.getValue().equalsIgnoreCase("true"))
				{
					this.config.addDefault(item.getKey(), true);
				}
				else if(item.getValue().equalsIgnoreCase("false"))
				{
					this.config.addDefault(item.getKey(), false);
				}
				else if(isInteger(item.getValue()))
				{
					this.config.addDefault(item.getKey(), Integer.parseInt(item.getValue()));
				}
				else
				{
					this.config.addDefault(item.getKey(), item.getValue());
				}
				num++;
			}
		}
		if(num > 0)
		{
			this.log.info("[DailyBonus] " + num + " missing items added to config file.");
		}
		this.saveConfig();
	}

	public boolean isInteger(String input)  
	{  
	   try  
	   {  
	      Integer.parseInt(input);  
	      return true;  
	   }  
	   catch(Exception e)  
	   {  
	      return false; 
	   }  
	} 

	private HashMap<String, String> loadConfigurables(HashMap<String, String> items) 
	{
		//1.0
		items.put("Main.Number of Tiers", "1");
		items.put("Main.Item Give Delay (In Seconds)", "0");
		items.put("Main.Global Message", "&9[DailyBonus] &6!playername just got abonus of !amount !type for logging in today!");
		items.put("Main.Global Message is Enabled", "true");
		
		return items;
	}
	
	private void firstRun() throws Exception 
	{
		if (!this.configFile.exists()) 
		{
			this.configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), this.configFile);
		}
		
		File file = new File(this.getDataFolder().getAbsolutePath()+"/players");
		if(!file.exists())
		{
			file.mkdir();
		}
	}
	
	private void copy(InputStream in, File file) 
	{
		try 
		{
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) 
			{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void loadYamls() 
	{
		try 
		{
			this.config.load(this.configFile);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void saveConfig() 
	{
		try 
		{
			this.config.save(this.configFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private boolean setupEconomy() 
	{
        if (getServer().getPluginManager().getPlugin("Vault") == null) 
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) 
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}