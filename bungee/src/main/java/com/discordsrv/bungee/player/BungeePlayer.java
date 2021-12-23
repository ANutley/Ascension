/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2021 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.bungee.player;

import com.discordsrv.bungee.BungeeDiscordSRV;
import com.discordsrv.bungee.component.util.BungeeComponentUtil;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.player.IPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class BungeePlayer implements IPlayer {

    private final BungeeDiscordSRV discordSRV;
    private final ProxiedPlayer player;
    private final Identity identity;
    private final Audience audience;

    public BungeePlayer(BungeeDiscordSRV discordSRV, ProxiedPlayer player) {
        this.discordSRV = discordSRV;
        this.player = player;
        this.identity = Identity.identity(player.getUniqueId());
        this.audience = discordSRV.audiences().player(player);
    }

    @Override
    public void sendMessage(Identity identity, @NotNull Component message) {
        audience.sendMessage(
                identity != null ? identity : Identity.nil(),
                message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void runCommand(String command) {
        discordSRV.proxy().getPluginManager().dispatchCommand(player, command);
    }

    @Override
    public DiscordSRV discordSRV() {
        return discordSRV;
    }

    @Override
    public @NotNull String username() {
        return player.getName();
    }

    @Override
    public @NotNull Identity identity() {
        return identity;
    }

    @Override
    public @NotNull Component displayName() {
        return BungeeComponentUtil.fromLegacy(player.getDisplayName());
    }
}
