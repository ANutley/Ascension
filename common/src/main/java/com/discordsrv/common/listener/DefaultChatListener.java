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

package com.discordsrv.common.listener;

import com.discordsrv.api.channel.GameChannel;
import com.discordsrv.api.discord.api.entity.message.SendableDiscordMessage;
import com.discordsrv.api.event.bus.EventPriority;
import com.discordsrv.api.event.bus.Subscribe;
import com.discordsrv.api.event.events.message.receive.game.ChatMessageReceiveEvent;
import com.discordsrv.api.event.events.message.send.game.ChatMessageSendEvent;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.component.util.ComponentUtil;
import com.discordsrv.common.config.main.channels.BaseChannelConfig;
import com.discordsrv.common.config.main.channels.ChannelConfig;
import com.discordsrv.common.config.main.channels.minecraftodiscord.MinecraftToDiscordChatConfig;
import com.discordsrv.common.function.OrDefault;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;

public class DefaultChatListener extends AbstractListener {

    public DefaultChatListener(DiscordSRV discordSRV) {
        super(discordSRV);
    }

    @Subscribe(priority = EventPriority.LAST)
    public void onChatReceive(ChatMessageReceiveEvent event) {
        if (checkProcessor(event) || checkCancellation(event)) {
            return;
        }

        GameChannel gameChannel = event.getGameChannel();
        Component message = ComponentUtil.fromAPI(event.message());

        OrDefault<BaseChannelConfig> channelConfig = discordSRV.channelConfig().orDefault(gameChannel);
        OrDefault<MinecraftToDiscordChatConfig> chatConfig = channelConfig.map(cfg -> cfg.minecraftToDiscord);

        SendableDiscordMessage.Builder builder = chatConfig.get(cfg -> cfg.messageFormat);
        if (builder == null) {
            return;
        }

        SendableDiscordMessage discordMessage = discordSRV.discordAPI().format(builder)
                .addContext(event.getPlayer())
                .addReplacement("%message%", DiscordSerializer.INSTANCE.serialize(message))
                .build();

        discordSRV.eventBus().publish(
                new ChatMessageSendEvent(
                        discordMessage,
                        gameChannel
                )
        );
    }

    @Subscribe(priority = EventPriority.LAST)
    public void onChatSend(ChatMessageSendEvent event) {
        if (checkProcessor(event) || checkCancellation(event) || !discordSRV.isReady()) {
            return;
        }

        GameChannel channel = event.getTargetChannel();
        BaseChannelConfig channelConfig = discordSRV.channelConfig().get(channel);
        List<String> channelIds = channelConfig instanceof ChannelConfig ? ((ChannelConfig) channelConfig).channelIds : Collections.emptyList();
        if (channelIds.isEmpty()) {
            return;
        }

        for (String channelId : channelIds) {
            discordSRV.discordAPI().getTextChannelById(channelId).ifPresent(textChannel ->
                    textChannel.sendMessage(event.getDiscordMessage()));
        }
    }
}
