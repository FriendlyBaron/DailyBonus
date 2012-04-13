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
		if(plugin.isEnabled())
		{
			try
			{
				Thread.sleep(plugin.config.getInt("Main.Item Give Delay (In Seconds)")*1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if(player.isOnline() && plugin.isEnabled() && !plugin.numEarly.containsKey(player))
		{
			int amount = plugin.config.getInt("Tier." + num + ".Economy Amount");
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
			player.sendMessage(DailyBonusPlayerListener.replaceColors(plugin.config.getString("Tier." + num + ".Message").replaceAll("!amount", "" + amount)));
			List<?> items = plugin.config.getList("Tier." + num + ".Items");
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
				plugin.getServer().broadcastMessage(DailyBonusPlayerListener.replaceColors(plugin.config.getString("Main.Global Message").replaceAll("!amount", "" + amount).replaceAll("!playername", "" + player.getDisplayName())));
			}
		}
		if(plugin.numEarly.containsKey(player))
		{
			int num = plugin.numEarly.get(player);
			if(num <= 1)
			{
				plugin.numEarly.remove(player);
			}
			else
			{
				plugin.numEarly.remove(player);
				plugin.numEarly.put(player, (num-1));
			}
		}
	}
}
