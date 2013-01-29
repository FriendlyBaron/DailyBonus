package me.itsatacoshop247.DailyBonus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class DailyBonusPlayerListener implements Listener //part of new method, instead of extends
{
	public DailyBonus plugin;
	
	public DailyBonusPlayerListener(DailyBonus instance)
	{
		plugin = instance;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		Player player = event.getPlayer();
		
		if(!CheckLastLogin(player))
		{
			return;
		}
		int tiers = plugin.config.getInt("Main.Number of Tiers");
		for(int x = tiers; x > 0; x--)
		{
			if(player.hasPermission("dailybonus.tier." + x))
			{
				if(plugin.config.getInt("Main.Item Give Delay (In Seconds)") > 0)
				{
					Runnable r = new DailyBonusItemDelay(plugin, player, x);
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, r, 20*plugin.config.getInt("Main.Item Give Delay (In Seconds)"));
					plugin.playerList.add(player.getName());
					x = -666;
				}
				else
				{
					int amount = 0;
					String amt = plugin.config.getString("Tier." + x + ".Economy Amount");
					if(amt.split(";").length > 1)
					{
						amount = Integer.parseInt(amt.split(";")[0]) + (int)((Math.random()*(Integer.parseInt(amt.split(";")[1])*2))-Integer.parseInt(amt.split(";")[1]));
					}
					else
					{
						amount = plugin.config.getInt("Tier." + x + ".Economy Amount");
					}
					if(amount > 0)
					{
						if(DailyBonus.econ != null)
						{
							@SuppressWarnings("unused")
							EconomyResponse r = DailyBonus.econ.depositPlayer(player.getName(), amount);
						}
						else
						{
							player.sendMessage(ChatColor.DARK_RED + "The DailyBonus plugin would have given you economy money, but the server doesn't have Vault enabled, or it is not enabled correctly!");
						}
					}
					player.sendMessage(replaceColors(plugin.config.getString("Tier." + x + ".Message").replaceAll("!amount", "" + amount).replaceAll("!type", "" + DailyBonus.econ.currencyNamePlural())));
					if(plugin.config.get("Tier." + x + ".Items") != null)
					{
						@SuppressWarnings("unchecked")
						List<String> items = (List<String>) plugin.config.getList("Tier." + x + ".Items");
						
						for(String itemsline : items)
						{
							String[] line = itemsline.split(";");
							String [] data = itemsline.split("-");
							if(!line[0].equals("0"))
							{								
								ItemStack is = new ItemStack(Material.getMaterial(Integer.parseInt(line[0])), Integer.parseInt(line[1]));
								if(data.length > 1)
								{
									is.setDurability(Short.parseShort(data[1]));
								}
								
								if(line.length > 2)
								{
									is.setAmount(Integer.parseInt(line[1]) + (int)((Math.random()*(Integer.parseInt(line[2].split("-")[0])*2))-Integer.parseInt(line[2].split("-")[0])));
								}
								
								if(player.getInventory().firstEmpty() < 0)
								{
									player.getWorld().dropItemNaturally(player.getEyeLocation(), is);
								}
								else
								{
									player.getInventory().addItem(is);
								}
							}
						}
					}
					if(plugin.config.get("Tier." + x + ".Commands") != null)
					{
						@SuppressWarnings("unchecked")
						List<String> cmds = (List<String>) plugin.config.getList("Tier." + x + ".Commands");
						
						for(String cmd : cmds)
						{
							cmd = cmd.replaceAll("!player", player.getName());
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
						}
					}
					if(plugin.config.getBoolean("Main.Global Message is Enabled"))
					{
						for(Player p : plugin.getServer().getOnlinePlayers())
						{
							if(!p.equals(player))
							{
								p.sendMessage(replaceColors(plugin.config.getString("Main.Global Message").replaceAll("!amount", "" + amount).replaceAll("!playername", "" + player.getDisplayName()).replaceAll("!type", "" + DailyBonus.econ.currencyNamePlural())));
							}
						}
					}
					x = -666;
				}
			}
		}
		File file = new File(plugin.getDataFolder().getAbsolutePath()+"/players/"+player.getName()+".yml");
		FileConfiguration pfile = new YamlConfiguration();
		pfile.load(file);
		
		if(pfile.get("Time.Last") != null)
		{
			pfile.set(("Time.Last"), System.currentTimeMillis());
			pfile.save(file);
		}
		else
		{
			pfile.set("Time.Last", System.currentTimeMillis());
			pfile.save(file);
		}
	}	

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		Player player = event.getPlayer();
		
		File file = new File(plugin.getDataFolder().getAbsolutePath()+"/players/"+player.getName()+".yml");
		FileConfiguration pfile = new YamlConfiguration();
		pfile.load(file);
		
		long login = pfile.getLong("Time.Last");
		
		if(pfile.get("Time.Last") != null)
		{
			pfile.set(("Time.Last"), System.currentTimeMillis());
		}
		else
		{
			pfile.set("Time.Last", System.currentTimeMillis());
		}
		
		if(plugin.playerList.contains(player.getName()))
		{
			pfile.set("Time.Logged Early", true);
			plugin.playerList.remove(player.getName());
			
			if(plugin.numEarly.containsKey(player.getName()))
			{
				int already = plugin.numEarly.get(player.getName());
				plugin.numEarly.remove(player.getName());
				plugin.numEarly.put(player.getName(), (already+1));
			}
			else
			{
				plugin.numEarly.put(player.getName(), 1);
			}
		}
		
		Calendar current = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.setTimeInMillis(login);
		
		if(last.get(Calendar.DATE) < current.get(Calendar.DATE) || (last.get(Calendar.MONTH) + 1) < (current.get(Calendar.MONTH) + 1) || (last.get(Calendar.YEAR)) < (current.get(Calendar.YEAR)))
		{
			pfile.set("Time.Logged Early", true);
		}
		pfile.save(file);
	}

	private boolean CheckLastLogin(Player p) throws FileNotFoundException, IOException, InvalidConfigurationException 
	{
		File file = new File(plugin.getDataFolder().getAbsolutePath()+"/players/"+p.getName()+".yml");
		if(!file.exists())
		{
			file.createNewFile();
			FileConfiguration pfile = new YamlConfiguration();
			pfile.load(file);
			pfile.set("Time.Last", System.currentTimeMillis());
			pfile.set("Time.Logged Early", false);
			pfile.save(file);
			
			return true;
		}
		FileConfiguration pfile = new YamlConfiguration();
		pfile.load(file);
		
		if(pfile.get("Time.Logged Early") != null)
		{
			if(pfile.getBoolean("Time.Logged Early"))
			{
				pfile.set("Time.Logged Early", false);
				pfile.save(file);
				return true;
			}
		}
		
		if(pfile.get("Time.Last") != null)
		{
			Calendar current = Calendar.getInstance();
			Calendar last = Calendar.getInstance();
			last.setTimeInMillis(pfile.getLong("Time.Last"));
			
			if(last.get(Calendar.DATE) < current.get(Calendar.DATE) || (last.get(Calendar.MONTH) + 1) < (current.get(Calendar.MONTH) + 1) || (last.get(Calendar.YEAR)) < (current.get(Calendar.YEAR)))
			{
				return true;
			}
		}
		return false;
	}
	
	static String replaceColors (String message) 
	{
		return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
	}
}