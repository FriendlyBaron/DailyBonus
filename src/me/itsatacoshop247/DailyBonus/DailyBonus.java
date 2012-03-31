package me.itsatacoshop247.DailyBonus;

import net.milkbowl.vault.economy.Economy;

//wait time until give (thread?)

//global message

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyBonus extends JavaPlugin 
{
	public static Economy econ = null;
	public void onDisable() 
	{
	}
	
	public void onEnable() 
	{
		new DailyBonusPlayerListener(this);
		getServer().getPluginManager().registerEvents(new DailyBonusPlayerListener(this), this);
		loadConfiguration();
		setupEconomy();
	}

	private void loadConfiguration() 
	{
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
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