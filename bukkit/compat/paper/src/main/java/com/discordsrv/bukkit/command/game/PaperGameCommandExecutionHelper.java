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

package com.discordsrv.bukkit.command.game;

import com.discordsrv.bukkit.BukkitDiscordSRV;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApiStatus.AvailableSince("Paper 1.12")
public class PaperGameCommandExecutionHelper extends BukkitGameCommandExecutionHelper {

    public PaperGameCommandExecutionHelper(BukkitDiscordSRV discordSRV) {
        super(discordSRV);
    }

    @Override
    public CompletableFuture<List<String>> getRootCommands(CommandSender commandSender) {
        return discordSRV.scheduler().supplyOnMainThread(
                commandSender,
                () -> new ArrayList<>(discordSRV.server().getCommandMap().getKnownCommands().keySet())
        );
    }
}