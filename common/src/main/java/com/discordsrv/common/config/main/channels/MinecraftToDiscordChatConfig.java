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

package com.discordsrv.common.config.main.channels;

import com.discordsrv.api.discord.api.entity.message.SendableDiscordMessage;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigSerializable
public class MinecraftToDiscordChatConfig {

    public SendableDiscordMessage.Builder format = SendableDiscordMessage.builder()
            .setWebhookUsername("%player_display_name%")
            .setWebhookAvatarUrl("%player_avatar_url%")
            .setContent("%message%");// TODO

    // TODO: more info on regex pairs (String#replaceAll)
    @Comment("Regex filters for Minecraft message contents (this is the %message% part of the \"format\" option)")
    public Map<Pattern, String> contentRegexFilters = new LinkedHashMap<>();
    
}