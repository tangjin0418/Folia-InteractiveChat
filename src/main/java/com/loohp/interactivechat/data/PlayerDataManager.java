package com.loohp.interactivechat.data;

import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements Listener {

    private final InteractiveChat plugin;
    private final Database database;
    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();

    public PlayerDataManager(InteractiveChat plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            onJoin(new PlayerJoinEvent(player, ""));
        }
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return data.get(uuid);
    }

    public void reload() {
        for (PlayerData pd : data.values()) {
            pd.reload();
        }
    }

    //===============

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!database.playerExists(playerUUID)) {
                database.createPlayer(playerUUID, player.getName());
            }
            PlayerData pd = database.getPlayerInfo(playerUUID);
            data.put(playerUUID, pd);
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        PlayerData pd = data.remove(event.getPlayer().getUniqueId());
        if (pd != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> pd.save());
        }
    }

    //=============

    public static class PlayerData {

        private final Database database;
        private final UUID uuid;
        private String playername;
        private boolean mentionDisabled;
        private int inventoryDisplayLayout;

        protected PlayerData(Database database, UUID uuid, String playername, boolean mentionDisabled, int inventoryDisplayLayout) {
            this.database = database;
            this.uuid = uuid;
            this.playername = playername;
            this.mentionDisabled = mentionDisabled;
            this.inventoryDisplayLayout = inventoryDisplayLayout;
        }

        public synchronized void reload() {
            database.getPlayerInfo(this);
        }

        public synchronized void save() {
            database.save(this);
        }

        public UUID getUniqueId() {
            return uuid;
        }

        public String getPlayerName() {
            return playername;
        }

        public void setPlayerName(String playername) {
            this.playername = playername;
        }

        public boolean isMentionDisabled() {
            return mentionDisabled;
        }

        public void setMentionDisabled(boolean value) {
            this.mentionDisabled = value;
        }

        public int getInventoryDisplayLayout() {
            return inventoryDisplayLayout;
        }

        public void setInventoryDisplayLayout(int inventoryDisplayLayout) {
            this.inventoryDisplayLayout = inventoryDisplayLayout;
        }

    }

}
