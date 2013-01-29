package me.itsatacoshop247.DailyBonus;

import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DailyBonusItemDelay implements Runnable {

	public final DailyBonus plugin;
	private Player player;
	private int num;
	
	public DailyBonusItemDelay(DailyBonus instance, Player importPlayer, int importNum)
	{
		this.plugin = instance;
		this.player = importPlayer;
		this.num = importNum;
	}
	@Override
	public void run() 
	{
		if(player.isOnline() && plugin.isEnabled() && !plugin.numEarly.containsKey(player.getName()))
		{
			int amount = 0;
			String amt = plugin.config.getString("Tier." + this.num + ".Economy Amount");
			if(amt.split(";").length > 1)
			{
				amount = Integer.parseInt(amt.split(";")[0]) + (int)((Math.random()*(Integer.parseInt(amt.split(";")[1])*2))-Integer.parseInt(amt.split(";")[1]));
			}
			else
			{
				amount = plugin.config.getInt("Tier." + this.num + ".Economy Amount");
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
			player.sendMessage(DailyBonusPlayerListener.replaceColors(plugin.config.getString("Tier." + num + ".Message").replaceAll("!amount", "" + amount)));
			if(plugin.config.get("Tier." + num + ".Items") != null)
			{
				@SuppressWarnings("unchecked")
				List<String> items = (List<String>) plugin.config.getList("Tier." + num + ".Items");
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
			if(plugin.config.get("Tier." + num + ".Commands") != null)
			{
				@SuppressWarnings("unchecked")
				List<String> cmds = (List<String>) plugin.config.getList("Tier." + num + ".Commands");
				
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
		}
		plugin.playerList.remove(player.getName());
		
		if(plugin.numEarly.containsKey(player.getName()))
		{
			int num = plugin.numEarly.get(player.getName());
			if(num <= 1)
			{
				plugin.numEarly.remove(player.getName());
			}
			else
			{
				plugin.numEarly.remove(player.getName());
				plugin.numEarly.put(player.getName(), (num-1));
			}
		}
	}
	
	static String replaceColors (String message) 
	{
		return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
	}
}