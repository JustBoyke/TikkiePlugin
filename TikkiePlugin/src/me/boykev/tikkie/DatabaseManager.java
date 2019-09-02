package me.boykev.tikkie;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DatabaseManager
{
  private File configFile;
  private FileConfiguration config;
  
  public DatabaseManager(Main basement)
  {
    this.configFile = new File(basement.getDataFolder(), "mysql.yml");
    this.config = YamlConfiguration.loadConfiguration(this.configFile);
  }
  
  public void save()
  {
    try
    {
      this.config.save(this.configFile);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public FileConfiguration getConfig()
  {
    return this.config;
  }

  public boolean reloadConfig() {
      try {
          config = YamlConfiguration.loadConfiguration(configFile);
          return true;
      } catch (Exception erorr) {
          return false;
      }
  }
public void LoadDefaults() {
    config.addDefault("db.host", "localhost");
    config.addDefault("db.port", 3306);
    config.addDefault("db.database", "Kingdom");
    config.addDefault("db.username", "root");
    config.addDefault("db.password", "WW HIERO");
    config.addDefault("db.tabel", "TABEL HIERO");
    config.addDefault("setup.cooldown", 10);
    config.options().copyDefaults(true);
    save();
	
}

public FileConfiguration editConfig() {
	return config;
}
}
