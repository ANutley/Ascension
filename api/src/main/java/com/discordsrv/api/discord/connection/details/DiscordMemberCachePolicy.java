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

import com.discordsrv.api.DiscordSRVApi;
import com.discordsrv.api.discord.entity.guild.DiscordGuildMember;
import com.discordsrv.api.profile.IProfile;

/**
 * Represents a Discord member caching policy, a function which dictates if a given {@link DiscordGuildMember} should be cached.
 */
@FunctionalInterface
public interface DiscordMemberCachePolicy {

    DiscordMemberCachePolicy ALL = member -> true;
    DiscordMemberCachePolicy LINKED = member -> DiscordSRVApi.optional()
            .map(api -> api.profileManager().getProfile(member.getUser().getId()))
            .map(IProfile::isLinked).orElse(false);
    DiscordMemberCachePolicy VOICE = member -> member.asJDA().getVoiceState() != null;
    DiscordMemberCachePolicy OWNER = DiscordGuildMember::isOwner;

    boolean isCached(DiscordGuildMember member);
}
