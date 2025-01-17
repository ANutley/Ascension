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

package com.discordsrv.bukkit;

import com.discordsrv.api.DiscordSRVApi;
import com.discordsrv.api.reload.ReloadFlag;
import com.discordsrv.api.reload.ReloadResult;
import com.discordsrv.bukkit.component.translation.BukkitTranslationLoader;
import com.discordsrv.bukkit.config.main.BukkitConfig;
import com.discordsrv.bukkit.player.BukkitPlayerProvider;
import com.discordsrv.bukkit.plugin.BukkitPluginManager;
import com.discordsrv.bukkit.scheduler.BukkitScheduler;
import com.discordsrv.common.AbstractDiscordSRV;
import com.discordsrv.common.command.game.abstraction.GameCommandExecutionHelper;
import com.discordsrv.common.config.configurate.manager.ConnectionConfigManager;
import com.discordsrv.common.config.configurate.manager.MainConfigManager;
import com.discordsrv.common.config.configurate.manager.MessagesConfigManager;
import com.discordsrv.common.config.configurate.manager.abstraction.ServerConfigManager;
import com.discordsrv.common.config.connection.ConnectionConfig;
import com.discordsrv.common.config.messages.MessagesConfig;
import com.discordsrv.common.feature.debug.data.OnlineMode;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Server;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public abstract class BukkitDiscordSRV extends AbstractDiscordSRV<IBukkitBootstrap, BukkitConfig, ConnectionConfig, MessagesConfig> {

    private BukkitAudiences audiences;
    private BukkitTranslationLoader translationLoader;

    private final BukkitPluginManager pluginManager;

    private final ConnectionConfigManager<ConnectionConfig> connectionConfigManager;
    private final MainConfigManager<BukkitConfig> configManager;
    private final MessagesConfigManager<MessagesConfig> messagesConfigManager;

    public BukkitDiscordSRV(IBukkitBootstrap bootstrap) {
        super(bootstrap);

        this.pluginManager = new BukkitPluginManager(this);

        // Config
        this.connectionConfigManager = new ConnectionConfigManager<>(this, ConnectionConfig::new);
        this.configManager = new ServerConfigManager<>(this, BukkitConfig::new);
        this.messagesConfigManager = new MessagesConfigManager<>(this, MessagesConfig::new);
    }

    @Override
    protected void enable() throws Throwable {
        // Service provider
        server().getServicesManager().register(DiscordSRVApi.class, this, plugin(), ServicePriority.Normal);

        // Adventure related stuff
        this.audiences = BukkitAudiences.create(bootstrap.getPlugin());
        this.translationLoader = new BukkitTranslationLoader(this);

        // Integrations
        registerIntegration("com.discordsrv.bukkit.integration.VaultIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.PlaceholderAPIIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.EssentialsXIntegration");

        // Chat Integrations
        registerIntegration("com.discordsrv.bukkit.integration.chat.ChattyChatIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.chat.GriefPreventionChatIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.chat.LunaChatIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.chat.McMMOChatIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.chat.TownyChatIntegration");
        registerIntegration("com.discordsrv.bukkit.integration.chat.VentureChatIntegration");

        super.enable();
    }

    @Override
    protected List<ReloadResult> reload(Set<ReloadFlag> flags, boolean initial) throws Throwable {
        if (flags.contains(ReloadFlag.TRANSLATIONS)) {
            translationLoader.reload();
        }

        return super.reload(flags, initial);
    }

    @Override
    public OnlineMode onlineMode() {
        try {
            Class<?> paperConfig = Class.forName("com.destroystokyo.paper.PaperConfig");
            Field velocitySupport = paperConfig.getField("velocitySupport");
            Field velocityOnlineMode = paperConfig.getField("velocityOnlineMode");

            if (velocitySupport.getBoolean(null) && velocityOnlineMode.getBoolean(null)) {
                return OnlineMode.VELOCITY;
            }
        } catch (Throwable ignored) {}

        try {
            Class<?> spigotConfig = Class.forName("org.spigotmc.SpigotConfig");
            Field bungee = spigotConfig.getField("bungee");

            if (bungee.getBoolean(null)) {
                return OnlineMode.BUNGEE;
            }
        } catch (Throwable ignored) {}

        return OnlineMode.of(server().getOnlineMode());
    }

    @Override
    public abstract BukkitScheduler scheduler();

    @Override
    public abstract @Nullable GameCommandExecutionHelper executeHelper();

    @Override
    public abstract @NotNull BukkitPlayerProvider playerProvider();

    @Override
    public ServerType serverType() {
        return ServerType.SERVER;
    }

    public JavaPlugin plugin() {
        return bootstrap().getPlugin();
    }

    public Server server() {
        return bootstrap().getPlugin().getServer();
    }

    public BukkitAudiences audiences() {
        return audiences;
    }

    @Override
    public BukkitPluginManager pluginManager() {
        return pluginManager;
    }

    @Override
    public ConnectionConfigManager<ConnectionConfig> connectionConfigManager() {
        return connectionConfigManager;
    }

    @Override
    public MainConfigManager<BukkitConfig> configManager() {
        return configManager;
    }

    @Override
    public MessagesConfigManager<MessagesConfig> messagesConfigManager() {
        return messagesConfigManager;
    }
}
