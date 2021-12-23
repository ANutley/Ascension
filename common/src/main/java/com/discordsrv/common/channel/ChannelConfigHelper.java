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

package com.discordsrv.common.channel;

import com.discordsrv.api.channel.GameChannel;
import com.discordsrv.api.discord.api.entity.channel.DiscordTextChannel;
import com.discordsrv.api.event.bus.Subscribe;
import com.discordsrv.api.event.events.channel.GameChannelLookupEvent;
import com.discordsrv.api.event.events.lifecycle.DiscordSRVReloadEvent;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.config.main.channels.base.BaseChannelConfig;
import com.discordsrv.common.config.main.channels.base.ChannelConfig;
import com.discordsrv.common.config.main.channels.base.IChannelConfig;
import com.discordsrv.common.function.OrDefault;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChannelConfigHelper {

    private final DiscordSRV discordSRV;
    private final LoadingCache<String, GameChannel> nameToChannelCache;
    private final Map<Long, Map<String, BaseChannelConfig>> discordToConfigMap;

    public ChannelConfigHelper(DiscordSRV discordSRV) {
        this.discordSRV = discordSRV;
        this.nameToChannelCache = discordSRV.caffeineBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .refreshAfterWrite(10, TimeUnit.SECONDS)
                .build(new CacheLoader<String, GameChannel>() {

                    @Override
                    public @Nullable GameChannel load(@NonNull String channelName) {
                        GameChannelLookupEvent event = new GameChannelLookupEvent(null, channelName);
                        discordSRV.eventBus().publish(event);
                        if (!event.isProcessed()) {
                            return null;
                        }

                        return event.getChannelFromProcessing();
                    }
                });
        this.discordToConfigMap = new ConcurrentHashMap<>();

        discordSRV.eventBus().subscribe(this);
    }

    @Subscribe
    public void onReload(DiscordSRVReloadEvent event) {
        if (!event.isConfig()) {
            return;
        }

        Map<Long, Map<String, BaseChannelConfig>> newMap = new HashMap<>();
        for (Map.Entry<String, BaseChannelConfig> entry : channels().entrySet()) {
            String channelName = entry.getKey();
            BaseChannelConfig value = entry.getValue();
            if (value instanceof IChannelConfig) {
                IChannelConfig channelConfig = (IChannelConfig) value;
                for (long channelId : channelConfig.ids()) {
                    newMap.computeIfAbsent(channelId, key -> new LinkedHashMap<>())
                            .put(channelName, value);
                }
            }
        }

        synchronized (discordToConfigMap) {
            discordToConfigMap.clear();
            discordToConfigMap.putAll(newMap);
        }
    }

    private Map<String, BaseChannelConfig> channels() {
        return discordSRV.config().channels;
    }

    private BaseChannelConfig getDefault() {
        return channels().computeIfAbsent(ChannelConfig.DEFAULT_KEY, key -> new BaseChannelConfig());
    }

    public Set<OrDefault<BaseChannelConfig>> getAllChannels() {
        BaseChannelConfig defaultConfig = getDefault();

        Set<OrDefault<BaseChannelConfig>> channelConfigs = new HashSet<>();
        for (Map.Entry<String, BaseChannelConfig> entry : channels().entrySet()) {
            if (entry.getKey().equals(ChannelConfig.DEFAULT_KEY)) {
                continue;
            }
            channelConfigs.add(new OrDefault<>(entry.getValue(), defaultConfig));
        }
        return channelConfigs;
    }

    public OrDefault<BaseChannelConfig> orDefault(GameChannel gameChannel) {
        return orDefault(gameChannel.getOwnerName(), gameChannel.getChannelName());
    }

    public OrDefault<BaseChannelConfig> orDefault(String ownerName, String channelName) {
        BaseChannelConfig defaultConfig = getDefault();

        return new OrDefault<>(
                get(ownerName, channelName),
                defaultConfig
        );
    }

    public BaseChannelConfig get(GameChannel gameChannel) {
        return get(gameChannel.getOwnerName(), gameChannel.getChannelName());
    }

    public BaseChannelConfig get(String ownerName, String channelName) {
        if (ownerName != null) {
            BaseChannelConfig config = channels().get(ownerName + ":" + channelName);
            if (config != null) {
                return config;
            }

            GameChannel gameChannel = nameToChannelCache.get(channelName);
            if (gameChannel != null && gameChannel.getOwnerName().equals(ownerName)) {
                config = channels().get(channelName);
                return config;
            }
            return null;
        }

        GameChannel gameChannel = nameToChannelCache.get(channelName);
        return gameChannel != null ? get(gameChannel) : null;
    }

    public Map<GameChannel, OrDefault<BaseChannelConfig>> orDefault(DiscordTextChannel discordTextChannel) {
        BaseChannelConfig defaultConfig = getDefault();

        Map<GameChannel, OrDefault<BaseChannelConfig>> channels = new HashMap<>();
        for (Map.Entry<GameChannel, BaseChannelConfig> entry : getDiscordResolved(discordTextChannel).entrySet()) {
            channels.put(
                    entry.getKey(),
                    new OrDefault<>(entry.getValue(), defaultConfig)
            );
        }
        return channels;
    }

    public Map<GameChannel, BaseChannelConfig> getDiscordResolved(DiscordTextChannel channel) {
        Map<String, BaseChannelConfig> pairs = getDiscord(channel);
        if (pairs == null) {
            return Collections.emptyMap();
        }

        Map<GameChannel, BaseChannelConfig> channels = new LinkedHashMap<>();
        for (Map.Entry<String, BaseChannelConfig> entry : pairs.entrySet()) {
            GameChannel gameChannel = nameToChannelCache.get(entry.getKey());
            if (gameChannel == null) {
                continue;
            }

            channels.put(gameChannel, entry.getValue());
        }

        return channels;
    }

    public Map<String, BaseChannelConfig> getDiscord(DiscordTextChannel channel) {
        synchronized (discordToConfigMap) {
            return discordToConfigMap.get(channel.getId());
        }
    }
}
