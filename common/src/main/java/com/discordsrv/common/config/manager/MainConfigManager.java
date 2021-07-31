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

package com.discordsrv.common.config.manager;

import com.discordsrv.api.discord.api.entity.message.DiscordMessageEmbed;
import com.discordsrv.api.discord.api.entity.message.SendableDiscordMessage;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.config.main.MainConfig;
import com.discordsrv.common.config.main.channels.ChannelConfigHolder;
import com.discordsrv.common.config.manager.loader.YamlConfigLoaderProvider;
import com.discordsrv.common.config.manager.manager.TranslatedConfigManager;
import com.discordsrv.common.config.serializer.DiscordMessageEmbedSerializer;
import com.discordsrv.common.config.serializer.SendableDiscordMessageSerializer;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public abstract class MainConfigManager<C extends MainConfig>
        extends TranslatedConfigManager<C, YamlConfigurationLoader>
        implements YamlConfigLoaderProvider {

    public MainConfigManager(DiscordSRV discordSRV) {
        super(discordSRV);
    }

    @Override
    protected String fileName() {
        return MainConfig.FILE_NAME;
    }

    @Override
    public ConfigurationOptions defaultOptions() {
        return YamlConfigLoaderProvider.super.defaultOptions()
                .serializers(builder -> {
                    ObjectMapper.Factory objectMapper = defaultObjectMapper();
                    builder.register(ChannelConfigHolder.class, new ChannelConfigHolder.Serializer(objectMapper));
                    builder.register(DiscordMessageEmbed.Builder.class, new DiscordMessageEmbedSerializer());
                    builder.register(SendableDiscordMessage.Builder.class, new SendableDiscordMessageSerializer());
                });
    }
}
