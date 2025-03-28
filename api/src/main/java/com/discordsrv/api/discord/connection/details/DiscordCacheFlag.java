/*
 * This file is part of the DiscordSRV API, licensed under the MIT License
 * Copyright (c) 2016-2025 Austin "Scarsz" Shapiro, Henri "Vankka" Schubin and DiscordSRV contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.discordsrv.api.discord.connection.details;

import com.discordsrv.api.discord.entity.JDAEntity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public enum DiscordCacheFlag implements JDAEntity<CacheFlag> {

    ACTIVITY(CacheFlag.ACTIVITY),
    VOICE_STATE(CacheFlag.VOICE_STATE),
    EMOJI(CacheFlag.EMOJI),
    STICKER(CacheFlag.STICKER),
    CLIENT_STATUS(CacheFlag.CLIENT_STATUS),
    MEMBER_OVERRIDES(CacheFlag.MEMBER_OVERRIDES),
    ROLE_TAGS(CacheFlag.ROLE_TAGS),
    FORUM_TAGS(CacheFlag.FORUM_TAGS),
    ONLINE_STATUS(CacheFlag.ONLINE_STATUS),
    SCHEDULED_EVENTS(CacheFlag.SCHEDULED_EVENTS),

    ;

    private final CacheFlag jda;

    DiscordCacheFlag(CacheFlag jda) {
        this.jda = jda;
    }

    public DiscordGatewayIntent requiredIntent() {
        GatewayIntent intent = jda.getRequiredIntent();
        if (intent == null) {
            return null;
        }

        return DiscordGatewayIntent.getByJda(intent);
    }

    @Override
    public CacheFlag asJDA() {
        return jda;
    }
}
