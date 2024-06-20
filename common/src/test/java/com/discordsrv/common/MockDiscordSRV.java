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

package com.discordsrv.common;

import com.discordsrv.api.channel.GameChannel;
import com.discordsrv.common.bootstrap.IBootstrap;
import com.discordsrv.common.bootstrap.LifecycleManager;
import com.discordsrv.common.command.game.handler.ICommandHandler;
import com.discordsrv.common.config.configurate.manager.ConnectionConfigManager;
import com.discordsrv.common.config.configurate.manager.MainConfigManager;
import com.discordsrv.common.config.configurate.manager.MessagesConfigManager;
import com.discordsrv.common.config.configurate.manager.abstraction.ServerConfigManager;
import com.discordsrv.common.config.connection.ConnectionConfig;
import com.discordsrv.common.config.main.MainConfig;
import com.discordsrv.common.config.main.PluginIntegrationConfig;
import com.discordsrv.common.config.main.channels.base.ChannelConfig;
import com.discordsrv.common.config.main.generic.DestinationConfig;
import com.discordsrv.common.config.main.generic.ThreadConfig;
import com.discordsrv.common.config.messages.MessagesConfig;
import com.discordsrv.common.console.Console;
import com.discordsrv.common.debug.data.OnlineMode;
import com.discordsrv.common.debug.data.VersionInfo;
import com.discordsrv.common.exception.ConfigException;
import com.discordsrv.common.logging.Logger;
import com.discordsrv.common.logging.backend.impl.JavaLoggerImpl;
import com.discordsrv.common.messageforwarding.game.minecrafttodiscord.MinecraftToDiscordChatModule;
import com.discordsrv.common.player.IPlayer;
import com.discordsrv.common.player.provider.AbstractPlayerProvider;
import com.discordsrv.common.plugin.PluginManager;
import com.discordsrv.common.scheduler.Scheduler;
import com.discordsrv.common.scheduler.StandardScheduler;
import com.discordsrv.common.storage.impl.MemoryStorage;
import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class MockDiscordSRV extends AbstractDiscordSRV<IBootstrap, MainConfig, ConnectionConfig, MessagesConfig> {

    public static final MockDiscordSRV INSTANCE = new MockDiscordSRV();

    public boolean configLoaded = false;
    public boolean connectionConfigLoaded = false;
    public boolean messagesConfigLoaded = false;
    public boolean playerProviderSubscribed = false;

    private final Scheduler scheduler = new StandardScheduler(this);
    private Path path;

    public static void main(String[] args) {
        new MockDiscordSRV();
    }

    public MockDiscordSRV() {
        super(new IBootstrap() {
            @Override
            public Logger logger() {
                return JavaLoggerImpl.getRoot();
            }

            @Override
            public ClasspathAppender classpathAppender() {
                return null;
            }

            @Override
            public ClassLoader classLoader() {
                return MockDiscordSRV.class.getClassLoader();
            }

            @Override
            public LifecycleManager lifecycleManager() {
                return null;
            }

            @Override
            public Path dataDirectory() {
                return null;
            }
        });
        load();
        versionInfo = new VersionInfo("JUnit", "JUnit", "JUnit", "JUnit");
    }

    @Override
    public Path dataDirectory() {
        if (this.path == null) {
            Path path = Paths.get("/tmp/discordsrv-test");
            try {
                path = Files.createTempDirectory("discordsrv-test");
            } catch (IOException ignored) {}
            this.path = path;
        }

        return path;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public Console console() {
        return null;
    }

    @Override
    public PluginManager pluginManager() {
        return null;
    }

    @Override
    public OnlineMode onlineMode() {
        return OnlineMode.ONLINE;
    }

    @Override
    public ICommandHandler commandHandler() {
        return null;
    }

    @Override
    public @NotNull AbstractPlayerProvider<?, ?> playerProvider() {
        return new AbstractPlayerProvider<IPlayer, DiscordSRV>(this) {
            @Override
            public void subscribe() {
                playerProviderSubscribed = true;
            }
        };
    }

    @Override
    public ConnectionConfigManager<ConnectionConfig> connectionConfigManager() {
        return new ConnectionConfigManager<ConnectionConfig>(this) {
            @Override
            public ConnectionConfig createConfiguration() {
                return connectionConfig();
            }

            @Override
            public void load() {
                connectionConfigLoaded = true;
            }
        };
    }

    @Override
    public ConnectionConfig connectionConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.bot.token = FullBootExtension.BOT_TOKEN;
        config.storage.backend = MemoryStorage.IDENTIFIER;
        config.minecraftAuth.allow = false;
        config.update.firstPartyNotification = false;
        config.update.security.enabled = false;
        config.update.github.enabled = false;
        return config;
    }

    @Override
    public MainConfigManager<MainConfig> configManager() {
        return new ServerConfigManager<MainConfig>(this) {
            @Override
            public MainConfig createConfiguration() {
                return config();
            }

            @Override
            public void load() {
                configLoaded = true;
            }
        };
    }

    @Override
    protected void enable() throws Throwable {
        super.enable();

        registerModule(MinecraftToDiscordChatModule::new);
    }

    @Override
    public MainConfig config() {
        MainConfig config = new MainConfig() {
            @Override
            public PluginIntegrationConfig integrations() {
                return null;
            }
        };

        if (StringUtils.isNotEmpty(FullBootExtension.TEST_CHANNEL_ID)) {
            ChannelConfig global = (ChannelConfig) config.channels.get(GameChannel.DEFAULT_NAME);
            DestinationConfig destination = global.destination = new DestinationConfig();

            long channelId = Long.parseLong(FullBootExtension.TEST_CHANNEL_ID);
            long forumId = Long.parseLong(FullBootExtension.FORUM_CHANNEL_ID);

            List<Long> channelIds = destination.channelIds;
            channelIds.clear();
            channelIds.add(channelId);

            List<ThreadConfig> threadConfigs = destination.threads;
            threadConfigs.clear();

            ThreadConfig thread = new ThreadConfig();
            thread.channelId = channelId;
            threadConfigs.add(thread);

            ThreadConfig forumThread = new ThreadConfig();
            thread.channelId = forumId;
            threadConfigs.add(forumThread);
        }

        return config;
    }

    @Override
    public MessagesConfigManager<MessagesConfig> messagesConfigManager() {
        return new MessagesConfigManager<MessagesConfig>(null) {
            @Override
            public MessagesConfig createConfiguration() {
                return null;
            }

            @Override
            public void load() {
                messagesConfigLoaded = true;
            }
        };
    }
}
