package me.boykev.tikkie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin{
	
	
	@SuppressWarnings("unused")
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		db = new DatabaseManager(this);
		cm = new PrivateConfigManager(this);
		System.out.println(ChatColor.GREEN + "Tikkie is opgestart!");
		cm.LoadDefaults();
		cm.save();
		db.LoadDefaults();
		db.save();
	}
	
	public void onDisable() {
		System.out.println(ChatColor.RED + "Tikkie is nu uitgeschakeld");
	}
	
	public String invname = ChatColor.RED + "Tikkie";
	public String prefix = "[" + ChatColor.GOLD + "Tikkie" + ChatColor.WHITE + "] ";
	private DatabaseManager db;
	private PrivateConfigManager cm;
	private SqlManager sql;
	
	
	public Inventory createInv(Player p, Integer size) {
		Inventory inv = Bukkit.createInventory(p, size, this.invname);
		return inv;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		db = new DatabaseManager(this);
		sql = new SqlManager(this);
		cm = new PrivateConfigManager(this);
		
		if(cmd.getName().equalsIgnoreCase("tikkie")) {
			if(args.length < 1) {
				p.sendMessage(this.prefix + ChatColor.RED + "Je hebt niet genoeg argumenten gebruikt!");
				return false;
			}
			
			if(args[0].equalsIgnoreCase("send")) {
				if(args.length < 3) {
					p.sendMessage(ChatColor.RED + "Je hebt niet genoeg argumenten gebruikt: " + ChatColor.GREEN + "/tikkie send [player] [bedrag]");
					return false;
				}
				
				OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
				Integer bedrag = Integer.parseInt(args[2]);
				UUID uuid = op.getUniqueId();
				
				if(op.hasPlayedBefore() == false) {
					p.sendMessage(ChatColor.RED + "Deze speler is niet gevonden!");
					return false;
				}
				if(bedrag < 1) {
					p.sendMessage(ChatColor.RED + "Het minimale tikkie bedrag is 1 euro!");
					return false;
				}
				
				if(!p.hasPermission("tikkie.send")) {
					p.sendMessage("Oeps, tikkie is nog niet beschikbaar voor jouw");
					return false;
				}
				
				Integer lastid = cm.getConfig().getInt(uuid + ".verzoeken.last");
				Integer sum = lastid + 1;
				Date now = new Date();
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
				
				cm.editConfig().set(uuid + ".verzoeken", sum);
				cm.editConfig().set(uuid + ".verzoeken." + sum + ".Player", p.getName());
				cm.editConfig().set(uuid + ".verzoeken." + sum + ".UUID", p.getUniqueId());
				cm.editConfig().set(uuid + ".verzoeken." + sum + ".bedrag", bedrag);
				cm.editConfig().set(uuid + ".verzoeken." + sum + ".timestamp", format.format(now));
				cm.save();
				sql.makeLog(p.getName(), "verzoek verzenden", bedrag, op.getName());
				
			}
			
		}
		
		
		return false;
	}
	
	
	
	
}