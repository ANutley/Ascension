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

package com.discordsrv.common.feature.linking;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LinkStore extends LinkProvider {

    Duration LINKING_CODE_EXPIRY_TIME = Duration.ofMinutes(5);
    Duration LINKING_CODE_RATE_LIMIT = Duration.ofSeconds(5);

    CompletableFuture<Void> createLink(@NotNull UUID playerUUID, long userId);
    CompletableFuture<Void> removeLink(@NotNull UUID playerUUID, long userId);

    /**
     * Gets the linking code information for the given code.
     * @param userId the Discord user id this request is for
     * @param code the code
     * @return a part with the Player's {@link UUID} and username
     */
    CompletableFuture<Pair<UUID, String>> getCodeLinking(long userId, @NotNull String code);
    CompletableFuture<Void> removeLinkingCode(@NotNull UUID playerUUID);

    CompletableFuture<Integer> getLinkedAccountCount();
}
