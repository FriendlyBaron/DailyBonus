package me.itsatacoshop247.DailyBonus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
		int tiers = plugin.getConfig().getInt("Main.Number of Tiers");
		for(int x = tiers; x > 0; x--)
		{
			if(player.hasPermission("dailybonus.tier." + x))
			{
				if(plugin.getConfig().getInt("Main.Item Give Delay (In Seconds)") > 0)
				{
					Runnable r = new DailyBonusItemDelay(plugin, player, x);
					new Thread(r).start();
					x = -666;
				}
				else
				{
					int amount = plugin.getConfig().getInt("Tier." + x + ".Economy Amount");
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
					player.sendMessage(replaceColors(plugin.getConfig().getString("Tier." + x + ".Message").replaceAll("!amount", "" + amount)));
					List<?> items = plugin.getConfig().getList("Tier." + x + ".Items");
					String[] items1 = (String[]) items.toArray(new String[0]);
					for(int y = 0; y < items1.length; y++)
					{
						String[] line = items1[y].split(";");
						if(!line[0].equals("0"))
						{
							ItemStack is = new ItemStack(Material.getMaterial(Integer.parseInt(line[0])), Integer.parseInt(line[1]));
							player.getInventory().addItem(is);
						}
					}
					if(plugin.getConfig().getBoolean("Main.Global Message is Enabled"))
					{
						plugin.getServer().broadcastMessage(replaceColors(plugin.getConfig().getString("Main.Global Message").replaceAll("!amount", "" + amount).replaceAll("!playername", "" + player.getDisplayName())));
					}
					x = -666;
				}
			}
		}	
	}	


	private boolean CheckLastLogin(Player p) {
		if(plugin.getConfig().getList("Players for first login") != null)
		{
			String[] set = {""};
			set[0] = p.getName();
			if(!plugin.getConfig().getList("Players for first login").contains(p.getName()))
			{
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) plugin.getConfig().getList("Players for first login");
				list.addAll(Arrays.asList(set));
				plugin.getConfig().set("Players for first login", list);
				plugin.saveConfig();
				return true;
			}
		}
		else if(plugin.getConfig().getList("Players for first login") == null)
		{
			List<String> list = Arrays.asList(p.getName());
			plugin.getConfig().addDefault("Players for first login", list);
			plugin.saveConfig();
			return true;
		}
		
		if(!p.hasPlayedBefore())
		{
			return true;
		}
		Calendar current = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.setTimeInMillis(p.getLastPlayed());
		int day = current.get(Calendar.DATE);
		int month = (current.get(Calendar.MONTH) + 1);
		int day2 = last.get(Calendar.DATE);
		int month2 = (last.get(Calendar.MONTH) + 1);
		if(day2 > day && month2 >= month) //86,400,000 is 1 day exactly. If the player is new the value is 0 which keeps statement true
		{
			return true;
		}
		return false;
	}
	
	static String replaceColors (String message) 
	{
		return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
	}
}