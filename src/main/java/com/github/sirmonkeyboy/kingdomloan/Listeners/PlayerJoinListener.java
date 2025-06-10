package com.github.sirmonkeyboy.kingdomloan.Listeners;

import com.github.sirmonkeyboy.kingdomloan.Utils.MariaDB;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;


public class PlayerJoinListener implements Listener {

    private final MariaDB data;

    public PlayerJoinListener(MariaDB data) {
        this.data = data;
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        data.updatePlayerName(player);
        data.loanPayOutToLoaner(player);
    }
}
