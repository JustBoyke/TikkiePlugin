package me.boykev.tikkie;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener{
	
	public Economy economy;
	
	@SuppressWarnings("unused")
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		db = new DatabaseManager(this);
		cm = new PrivateConfigManager(this);
		System.out.println(ChatColor.GREEN + "TikkieV0.5 is opgestart!");
		Bukkit.getPluginManager().registerEvents(this, this);
		cm.LoadDefaults();
		cm.save();
		db.LoadDefaults();
		db.save();
		if(Bukkit.getPluginManager().getPlugin("Vault") instanceof Vault)
    	{
    		RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

    		if(service != null)
    			economy = service.getProvider();
    	}
		
	}
	
	public void onDisable() {
		System.out.println(ChatColor.RED + "Tikkie is nu uitgeschakeld");
	}
	
	public String invname = ChatColor.RED + "Tikkie";
	public String prefix = "[" + ChatColor.GOLD + "Tikkie" + ChatColor.WHITE + "] ";
	private DatabaseManager db;
	private PrivateConfigManager cm;
	private SqlManager sql;
	
	public HashMap<String, Long> cooldown = new HashMap<String, Long>();
	private int cooldowntime = 15;
	
	
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
					p.sendMessage(this.prefix + ChatColor.RED + "Je hebt niet genoeg argumenten gebruikt: " + ChatColor.GREEN + "/tikkie send [player] [bedrag]");
					return false;
				}
				
				OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
				Integer bedrag = Integer.parseInt(args[2]);
				UUID uuid = op.getUniqueId();
				
				if(op.hasPlayedBefore() == false) {
					p.sendMessage(this.prefix + ChatColor.RED + "Deze speler is niet gevonden!");
					return false;
				}
				if(bedrag < 1) {
					p.sendMessage(this.prefix + ChatColor.RED + "Het minimale tikkie bedrag is 1 euro!");
					return false;
				}
				if(bedrag > 500000) {
					p.sendMessage(this.prefix + ChatColor.RED + "Het maximale bedrag voor een Tikkie is: 500.000€");
					return false;
				}
				
				if(!p.hasPermission("tikkie.send")) {
					p.sendMessage(this.prefix + "Oeps, tikkie is nog niet beschikbaar voor jouw");
					return false;
				}
				
				if(cooldown.containsKey(p.getName())) {
					long left = ((cooldown.get(p.getName())/1000)+cooldowntime) - (System.currentTimeMillis()/1000);
					if(left > 0) {
						p.sendMessage(ChatColor.RED + "Je moet nog " + left + " seconden wachten tot je weer een tikkie kan versturen!");
						return false;
					}
				}
				
				Date now = new Date();
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
				
				Random r = new Random();
				int rand = r.nextInt(9000);
				
				if(cm.getConfig().getString(uuid + ".last") == null) {
					cm.editConfig().set(uuid + ".last", rand);
					cm.save();
				}
				float a = Float.valueOf(cm.getConfig().getString(uuid + ".last")).floatValue();
				DecimalFormat df = new DecimalFormat("#");
		        double ans1 = a + 1;
		        String lastlog = df.format(ans1);
		        cm.editConfig().set(uuid + ".verzoeken." + lastlog + ".Player", p.getName());
		        cm.editConfig().set(uuid + ".verzoeken." + lastlog + ".UUID", p.getUniqueId().toString());
		        cm.editConfig().set(uuid + ".verzoeken." + lastlog + ".bedrag", bedrag);
		        cm.editConfig().set(uuid + ".verzoeken." + lastlog + ".timestamp", format.format(now));
		        cm.editConfig().set(uuid + ".last", lastlog);
		        cm.save();
		        if(bedrag < 1000) {
		        	sql.makeLog(p.getName(), "verzoek verzenden ", bedrag, op.getName(), lastlog, "NEE");
		        }
		        if(bedrag > 1000) {
		        	sql.makeLog(p.getName(), "verzoek verzenden ", bedrag, op.getName(), lastlog, "JA");
		        }
		        cooldown.put(p.getName(), System.currentTimeMillis());
		        p.sendMessage(this.prefix + ChatColor.GREEN + "verzoek voor betalen van " + ChatColor.GRAY + bedrag + ChatColor.GREEN + "€ verzonden naar: " + ChatColor.GRAY + op.getName());
		        if(op.isOnline()) {
		        	op.getPlayer().sendMessage(this.prefix + ChatColor.RED + "Je hebt een tikkie ontvangen van: " + ChatColor.GRAY + p.getName() + ChatColor.RED + " ter waarde van: " + ChatColor.GRAY + bedrag + "€");
		        }
		        return false;
				
			}
			
			if(args[0].equalsIgnoreCase("check")) {
				String uuid = p.getUniqueId().toString();
				if(args.length < 2) {
					if(cm.getConfig().getConfigurationSection(uuid + ".verzoeken") == null) {
						p.sendMessage(this.prefix + ChatColor.RED + "Jij hebt nog nooit verzoeken ontvangen!");
						return false;
					}
					for(String key : cm.getConfig().getConfigurationSection(uuid + ".verzoeken").getKeys(false)) {
						String afzender = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + key + ".Player") + ChatColor.RED;
						String bedrag = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + key + ".bedrag") + "€ "+ ChatColor.RED;
						p.sendMessage(ChatColor.RED + "Verzoek van " + afzender + " Met waarde: " + bedrag + "en ID: " + ChatColor.GRAY + key);
					}
					p.sendMessage(ChatColor.GREEN + "Meer info over een speciefiek verzoek? Doe dan: " + ChatColor.BLUE + "/tikkie check [id]");
					return false;
				}
				
				String tid = args[1];
				if(cm.getConfig().getConfigurationSection(uuid + ".verzoeken." + tid) != null) {
					String afzender = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".Player") + ChatColor.RED;
					String bedrag = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".bedrag") + "€ "+ ChatColor.RED;
					String time = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".timestamp") + ChatColor.RED;
					String uuids = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".UUID") + ChatColor.RED;
					
					p.sendMessage(ChatColor.GREEN + "Verzoek van: " + afzender);
					p.sendMessage(ChatColor.GREEN + "UUID Afzender: " + uuids);
					p.sendMessage(ChatColor.GREEN + "Te betalen bedrag: " + bedrag);
					p.sendMessage(ChatColor.GREEN + "Ontvangen op: " + time);
					p.sendMessage(ChatColor.GREEN + "Tikkie ID: " + ChatColor.GRAY + tid);
					return false;
				}
				p.sendMessage(this.prefix + ChatColor.RED + "Dit verzoek is niet gevonden in je account!");
				return false;
			}
			
			if(args[0].equalsIgnoreCase("pay")) {	
				String uuid = p.getUniqueId().toString();
				if(!p.hasPermission("tikkie.pay")) {
					p.sendMessage(this.prefix + "Oeps, jij kan nog niet betalen met tikkie!");
					return false;
				}
				if(args.length < 2) {
					p.sendMessage(this.prefix + ChatColor.RED + "Gebruik /tikkie pay [id]");
					return false;
				}
				String tid = args[1];
				if(cm.getConfig().getConfigurationSection(uuid + ".verzoeken." + tid) != null) {
					@SuppressWarnings("unused")
					OfflinePlayer player = Bukkit.getOfflinePlayer(cm.getConfig().getString(uuid + ".verzoeken." + tid + ".Player"));
					String afzender = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".Player") + ChatColor.RED;
					String bedrag = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".bedrag") + "€ "+ ChatColor.RED;
					String uuids = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".UUID") + ChatColor.RED;
					
					TextComponent msg = new TextComponent(ChatColor.RED + "Om te bevestigen " + ChatColor.BLUE + "(Klik Hier)");
					msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tikkie confirmpayment " + args[1]));
					p.sendMessage(ChatColor.GREEN + "Verzoek van: " + afzender);
					p.sendMessage(ChatColor.GREEN + "UUID Afzender: " + uuids);
					p.sendMessage(ChatColor.GREEN + "Te betalen bedrag: " + bedrag);
					p.sendMessage(ChatColor.GREEN + "Tikkie ID: " + ChatColor.GRAY + tid);
					p.spigot().sendMessage(msg);
				}
			}
			if(args[0].equalsIgnoreCase("confirmpayment")) {
				String uuid = p.getUniqueId().toString();
				String tid = args[1];
				
				if(cm.getConfig().getConfigurationSection(uuid + ".verzoeken." + tid) == null) {
					p.sendMessage(this.prefix + ChatColor.RED + "Deze betaling kon niet worden bevestigd omdat hij niet is gevonden in je account!");
					return false;
				}
				
				OfflinePlayer player = Bukkit.getOfflinePlayer(cm.getConfig().getString(uuid + ".verzoeken." + tid + ".Player"));
				String afzender = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".Player") + ChatColor.RED;
				String bedrag = ChatColor.GRAY + cm.getConfig().getString(uuid + ".verzoeken." + tid + ".bedrag") + "€ "+ ChatColor.RED;
				int money = cm.getConfig().getInt(uuid + ".verzoeken." + tid + ".bedrag");
				
				
				if(economy.getBalance(p.getPlayer()) < money) {
					p.sendMessage(this.prefix + ChatColor.RED + "Je hebt niet genoeg geld om deze tikkie te kunnen betalen!");
					return false;
				}
				economy.depositPlayer(player, money);
				economy.withdrawPlayer(p, money);
				
				if(player.isOnline()) {
					player.getPlayer().sendMessage(this.prefix + ChatColor.GRAY + p.getName() + ChatColor.GREEN + " heeft een tikkie overgemaakt van: " + bedrag);
		        }
				
				
				
				p.sendMessage(this.prefix + ChatColor.RED + "Betaling van " + bedrag + "gedaan aan speler: " + afzender);
				cm.editConfig().set(uuid + ".verzoeken." + tid, null);
				if(money < 1000) {
		        	sql.makeLog(p.getName(), "verzoek betaald ", money, player.getName(), tid, "NEE");
		        }
		        if(money > 1000) {
		        	sql.makeLog(p.getName(), "verzoek betaald ", money, player.getName(), tid, "JA");
		        }
				cm.save();
				return false;
			}
			if(args[0].equalsIgnoreCase("remove")) {
				if(!p.hasPermission("tikkie.admin")) {
					p.sendMessage(this.prefix + ChatColor.RED + "Dit commando is alleen voor mensen met de permissie: " + ChatColor.GRAY + "tikkie.admin");
					return false;
				}
				if(args.length < 3) {
					p.sendMessage(ChatColor.RED + "gebruik /tikkie remove [player] [id]");
					return false;
				}
				
				OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
				String id = args[2];
				
				if(cm.getConfig().getConfigurationSection(op.getUniqueId().toString() + ".verzoeken." + id) == null) {
					p.sendMessage(this.prefix + ChatColor.RED + "Dit ID is niet gevonden op het account van " + ChatColor.GRAY + op.getName() + " (" + op.getUniqueId().toString() + ")");
					return false;
				}
				int money = cm.getConfig().getInt(p.getUniqueId().toString() + ".verzoeken." + id + ".bedrag");
				sql.makeLog(p.getName(), "verzoek verwijderd door admin ", money, op.getName(), id, "n.v.t");
				cm.editConfig().set(op.getUniqueId().toString() + ".verzoeken." + id, null);
				cm.save();
				p.sendMessage(this.prefix + ChatColor.RED + "Tikkie met onderstaande gevens is verwijderd:");
				p.sendMessage(ChatColor.GREEN + "Player: " + ChatColor.GRAY + op.getName());
				p.sendMessage(ChatColor.GREEN + "UUID: " + ChatColor.GRAY + op.getUniqueId().toString());
				p.sendMessage(ChatColor.GREEN + "Verzoek ID: " + ChatColor.GRAY + id);
				
				if(op.isOnline()) {
					op.getPlayer().sendMessage(this.prefix + ChatColor.RED + "Een administrator heeft je tikkie met ID: " + ChatColor.GRAY + id + ChatColor.RED + " verwijderd uit je account!");
				}
				return false;
			}
			if(args[0].equalsIgnoreCase("deny")) {
				if(args.length < 2) {
					p.sendMessage(ChatColor.RED + "gebruik /tikkie deny [id]");
					return false;
				}
				
				String id = args[1];
				
				if(cm.getConfig().getConfigurationSection(p.getUniqueId().toString() + ".verzoeken." + id) == null) {
					p.sendMessage(this.prefix + ChatColor.RED + "Dit verzoek is niet gevonden in je account!");
					return false;
				}
				
				OfflinePlayer player = Bukkit.getOfflinePlayer(cm.getConfig().getString(p.getUniqueId().toString() + ".verzoeken." + id + ".Player"));
				int money = cm.getConfig().getInt(p.getUniqueId().toString() + ".verzoeken." + id + ".bedrag");
				sql.makeLog(p.getName(), "verzoek afgewezen ", money, player.getName(), id, "n.v.t");
				
				cm.editConfig().set(p.getUniqueId().toString() + ".verzoeken." + id, null);
				cm.save();
				p.sendMessage(this.prefix + ChatColor.RED + "Tikkie met onderstaande gevens is afgewezen:");
				p.sendMessage(ChatColor.GREEN + "Player: " + ChatColor.GRAY + p.getName());
				p.sendMessage(ChatColor.GREEN + "UUID: " + ChatColor.GRAY + p.getUniqueId().toString());
				p.sendMessage(ChatColor.GREEN + "Verzoek ID: " + ChatColor.GRAY + id);
				return false;
			}
		}
		return false;
	}

	
	
}