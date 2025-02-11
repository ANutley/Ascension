/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2025 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.discordsrv.bukkit.player;

import com.discordsrv.bukkit.BukkitDiscordSRV;
import com.discordsrv.common.abstraction.player.IOfflinePlayer;
import com.discordsrv.common.abstraction.player.IPlayer;
import com.discordsrv.common.abstraction.player.provider.ServerPlayerProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class BukkitPlayerProvider extends ServerPlayerProvider<BukkitPlayer, BukkitDiscordSRV> implements Listener {

    private final Function<Player, BukkitPlayer> playerConstructor;

    public BukkitPlayerProvider(BukkitDiscordSRV discordSRV, Function<Player, BukkitPlayer> playerConstructor) {
        super(discordSRV);
        this.playerConstructor = playerConstructor;
    }

    // IPlayer

    @Override
    public void subscribe() {
        discordSRV.server().getPluginManager().registerEvents(this, discordSRV.plugin());

        // Add players that are already connected
        for (Player player : discordSRV.server().getOnlinePlayers()) {
            addPlayer(player, true);
        }
    }

    @Override
    public void unsubscribe() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        addPlayer(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (player(event.getPlayer()) == null) {
            // The player wasn't loaded at PlayerLoginEvent (offline mode)
            addPlayer(event.getPlayer(), false);
        }
    }

    private void addPlayer(Player player, boolean initial) {
        addPlayer(player.getUniqueId(), playerConstructor.apply(player), initial);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer().getUniqueId());
    }

    public BukkitPlayer player(Player player) {
        BukkitPlayer srvPlayer = player(player.getUniqueId());
        if (srvPlayer == null) {
            throw new IllegalStateException("Player not available");
        }
        return srvPlayer;
    }

    // IOfflinePlayer

    private CompletableFuture<IOfflinePlayer> getFuture(Supplier<OfflinePlayer> provider) {
        return discordSRV.scheduler().supply(() -> {
            OfflinePlayer offlinePlayer = provider.get();
            if (offlinePlayer == null) {
                return null;
            }

            return new BukkitOfflinePlayer(discordSRV, offlinePlayer);
        });
    }

    @Override
    public CompletableFuture<IOfflinePlayer> lookupOfflinePlayer(UUID uuid) {
        IPlayer player = player(uuid);
        if (player != null) {
            return CompletableFuture.completedFuture(player);
        }

        return getFuture(() -> discordSRV.server().getOfflinePlayer(uuid));
    }

    @SuppressWarnings("deprecation") // Shut up, I know
    @Override
    public CompletableFuture<IOfflinePlayer> lookupOfflinePlayer(String username) {
        IPlayer player = player(username);
        if (player != null) {
            return CompletableFuture.completedFuture(player);
        }

        return getFuture(() -> discordSRV.server().getOfflinePlayer(username));
    }

    public IOfflinePlayer offlinePlayer(OfflinePlayer offlinePlayer) {
        return new BukkitOfflinePlayer(discordSRV, offlinePlayer);
    }
}
