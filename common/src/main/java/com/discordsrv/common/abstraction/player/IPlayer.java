/*
 * This file is part of DiscordSRV, licensed under the GPLv3 License
 * Copyright (c) 2016-2024 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
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

package com.discordsrv.common.abstraction.player;

import com.discordsrv.api.component.MinecraftComponent;
import com.discordsrv.api.placeholder.annotation.Placeholder;
import com.discordsrv.api.placeholder.annotation.PlaceholderPrefix;
import com.discordsrv.api.player.DiscordSRVPlayer;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.command.game.abstraction.sender.ICommandSender;
import com.discordsrv.common.config.main.AvatarProviderConfig;
import com.discordsrv.common.feature.profile.Profile;
import com.discordsrv.common.util.ComponentUtil;
import com.discordsrv.common.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@PlaceholderPrefix("player_")
public interface IPlayer extends DiscordSRVPlayer, IOfflinePlayer, ICommandSender {

    @Override
    default void sendMessage(@NotNull MinecraftComponent component) {
        sendMessage(ComponentUtil.fromAPI(component));
    }

    @Override
    DiscordSRV discordSRV();

    @ApiStatus.NonExtendable
    default Profile profile() {
        Profile profile = discordSRV().profileManager().getProfile(uniqueId());
        if (profile == null) {
            throw new IllegalStateException("Profile does not exist");
        }
        return profile;
    }

    @NotNull
    String username();

    @Override
    @ApiStatus.NonExtendable
    default @NotNull UUID uniqueId() {
        return identity().uuid();
    }

    CompletableFuture<Void> kick(Component component);

    void addChatSuggestions(Collection<String> suggestions);
    void removeChatSuggestions(Collection<String> suggestions);

    @NotNull
    @Placeholder("display_name")
    Component displayName();

    @Nullable
    @ApiStatus.NonExtendable
    @Placeholder("avatar_url")
    default String getAvatarUrl() {
        AvatarProviderConfig avatarConfig = discordSRV().config().avatarProvider;
        String avatarUrlTemplate = avatarConfig.avatarUrlTemplate;

        if (avatarConfig.autoDecideAvatarUrl) {
            // Offline mode
            if (UUIDUtil.isOffline(uniqueId())) avatarUrlTemplate = "https://cravatar.eu/helmavatar/%player_name%/128.png#%player_skin_texture_id%";
            // Bedrock
            else if (UUIDUtil.isGeyser(uniqueId())) avatarUrlTemplate = "https://api.tydiumcraft.net/skin?uuid=%player_uuid_short%&type=avatar&size=128";
        }

        if (avatarUrlTemplate == null) {
            return null;
        }

        return discordSRV().placeholderService().replacePlaceholders(avatarUrlTemplate, this);
    }

}
