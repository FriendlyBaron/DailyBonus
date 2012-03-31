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
				Thread.sleep(plugin.getConfig().getInt("Main.Item Give Delay (In Seconds)")*1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if(player.isOnline() && plugin.isEnabled())
		{
			int amount = plugin.getConfig().getInt("Tier." + num + ".Economy Amount");
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
			player.sendMessage(DailyBonusPlayerListener.replaceColors(plugin.getConfig().getString("Tier." + num + ".Message").replaceAll("!amount", "" + amount)));
			List<?> items = plugin.getConfig().getList("Tier." + num + ".Items");
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
				plugin.getServer().broadcastMessage(DailyBonusPlayerListener.replaceColors(plugin.getConfig().getString("Main.Global Message").replaceAll("!amount", "" + amount).replaceAll("!playername", "" + player.getDisplayName())));
			}
		}
		

	}

}
