package me.boykev.tikkie;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin{
	
	
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		System.out.println(ChatColor.GREEN + "Tikkie is opgestart!");
	}
	
	public void onDisable() {
		System.out.println(ChatColor.RED + "Tikkie is nu uitgeschakeld");
	}
	
	public String invname = ChatColor.RED + "Tikkie";
	public String prefix = "[" + ChatColor.GOLD + "Tikkie" + ChatColor.WHITE + "] ";
	
	
	public Inventory createInv(Player p, Integer size) {
		Inventory inv = Bukkit.createInventory(p, size, this.invname);
		return inv;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("tikkie")) {
			if(args.length < 1) {
				p.sendMessage(this.prefix + ChatColor.RED + "Je hebt niet genoeg argumenten gebruikt!");
				return false;
			}
			
			if(args[0].equalsIgnoreCase("send")) {
				
			}
			
		}
		
		
		return false;
	}
	
	
	
	
}