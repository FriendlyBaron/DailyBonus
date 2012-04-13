package me.itsatacoshop247.DailyBonus;

import java.util.Calendar;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
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
					new Thread(r).start();
					plugin.playerList.add(player);
					x = -666;
				}
				else
				{
					int amount = plugin.config.getInt("Tier." + x + ".Economy Amount");
					if(amount != 0)
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
					player.sendMessage(replaceColors(plugin.config.getString("Tier." + x + ".Message").replaceAll("!amount", "" + amount)));
					List<?> items = plugin.config.getList("Tier." + x + ".Items");
					String[] items1 = (String[]) items.toArray(new String[0]);
					for(int y = 0; y < items1.length; y++)
					{
						String[] line = items1[y].split(";");
						if(!line[0].equals("0"))
						{
							ItemStack is = new ItemStack(Material.getMaterial(Integer.parseInt(line[0])), Integer.parseInt(line[1]));
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
					if(plugin.config.getBoolean("Main.Global Message is Enabled"))
					{
						plugin.getServer().broadcastMessage(replaceColors(plugin.config.getString("Main.Global Message").replaceAll("!amount", "" + amount).replaceAll("!playername", "" + player.getDisplayName())));
					}
					x = -666;
				}
			}
		}	
	}	

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		long login = plugin.players.getLong("Players." + player.getName() + ".Last");
		
		if(plugin.players.get("Players." + player.getName() + ".Last") != null)
		{
			plugin.players.set(("Players." + player.getName() + ".Last"), System.currentTimeMillis());
			plugin.savePlayers();
		}
		else
		{
			plugin.players.addDefault("Players." + player.getName() + ".Last", System.currentTimeMillis());
			plugin.savePlayers();
		}
		
		if(plugin.playerList.contains(player))
		{
			plugin.players.set("Players." + player.getName() + ".Logged Early", true);
			plugin.playerList.remove(player);
			
			if(plugin.numEarly.containsKey(player))
			{
				int already = plugin.numEarly.get(player);
				plugin.numEarly.remove(player);
				plugin.numEarly.put(player, (already+1));
			}
			else
			{
				plugin.numEarly.put(player, 1);
			}
		}
		
		Calendar current = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.setTimeInMillis(login);
		
		if(last.get(Calendar.DATE) < current.get(Calendar.DATE) || (last.get(Calendar.MONTH) + 1) < (current.get(Calendar.MONTH) + 1) || (last.get(Calendar.YEAR)) < (current.get(Calendar.YEAR)))
		{
			plugin.players.set("Players." + player.getName() + ".Logged Early", true);
		}
		
	}

	private boolean CheckLastLogin(Player p) {
		if(plugin.players.get("Players." + p.getName() + ".Logged Early") != null)
		{
			if(plugin.players.getBoolean("Players." + p.getName() + ".Logged Early"))
			{
				plugin.players.set("Players." + p.getName() + ".Logged Early", false);
				return true;
			}
		}
		if(plugin.players.get("Players." + p.getName() + ".Last") != null)
		{
			Calendar current = Calendar.getInstance();
			Calendar last = Calendar.getInstance();
			last.setTimeInMillis(plugin.players.getLong("Players." + p.getName() + ".Last"));
			
			if(last.get(Calendar.DATE) < current.get(Calendar.DATE) || (last.get(Calendar.MONTH) + 1) < (current.get(Calendar.MONTH) + 1) || (last.get(Calendar.YEAR)) < (current.get(Calendar.YEAR)))
			{
				return true;
			}
		}
		else
		{
			plugin.players.addDefault("Players." + p.getName() + ".Last", System.currentTimeMillis());
			plugin.players.addDefault("Players." + p.getName() + ".Logged Early", false);
			plugin.savePlayers();
			return true;
		}
		return false;
	}
	
	static String replaceColors (String message) 
	{
		return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
	}
}