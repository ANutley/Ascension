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

package com.discordsrv.common.core.placeholder.context;

import com.discordsrv.api.component.MinecraftComponent;
import com.discordsrv.api.placeholder.annotation.Placeholder;
import com.discordsrv.api.placeholder.annotation.PlaceholderRemainder;
import com.discordsrv.common.DiscordSRV;

public class TextHandlingContext {

    private final DiscordSRV discordSRV;

    public TextHandlingContext(DiscordSRV discordSRV) {
        this.discordSRV = discordSRV;
    }

    @Placeholder("text")
    public MinecraftComponent text(@PlaceholderRemainder String text) {
        return discordSRV.componentFactory().textBuilder(text).build();
    }
}
