/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2023 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.common.config.connection;

import com.discordsrv.common.config.Config;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConnectionConfig implements Config {

    public static final String FILE_NAME = "connections.yaml";
    public static final String HEADER = "DiscordSRV's configuration file for connections to different external services.\n"
            + "This file is intended to contain connection details to services in order to keep them out of the config.yml\n"
            + "and to serve as a easy way to identify and control what external connections are being used.\n"
            + "\n"
            + "All domains listed as \"Requires a connection to\" require port 443 (https/wss) unless otherwise specified\n"
            + "\n"
            + " ABSOLUTELY DO NOT SEND THIS FILE TO ANYONE - IT ONLY CONTAINS SECRETS\n";

    @Override
    public final String getFileName() {
        return FILE_NAME;
    }

    public BotConfig bot = new BotConfig();

    public StorageConfig storage = new StorageConfig();

    public MinecraftAuthConfig minecraftAuth = new MinecraftAuthConfig();

    public UpdateConfig update = new UpdateConfig();
}
