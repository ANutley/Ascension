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

package com.discordsrv.common.core.logging.impl;

import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.helper.Timeout;
import com.discordsrv.common.logging.LogAppender;
import com.discordsrv.common.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

public class DependencyLoggingHandler implements LogAppender {

    private static final Map<String, List<String>> BLACKLISTED_MESSAGES = new HashMap<>();
    private static final Map<String, String> LOGGER_MAPPINGS = new HashMap<>();
    private static final Timeout RATELIMIT_TIMEOUT = new Timeout(Duration.ofSeconds(20));

    static {
        // Class names here will get relocated, which is fine
        LOGGER_MAPPINGS.put("net.dv8tion.jda", "JDA");
        LOGGER_MAPPINGS.put("com.zaxxer.hikari", "Hikari");

        BLACKLISTED_MESSAGES.put("net.dv8tion.jda", Arrays.asList(
                // We have our own more informative log messages for this
                "WebSocket connection was closed and cannot be recovered due to identification issues",
                // Failed JDA requests (handled with RestAction default failure)
                "There was an I/O error while executing a REST request: ",
                "There was an unexpected error while executing a REST request",
                // This doesn't need to be a warning logged to the user
                "Encountered 429 on route "
        ));
        BLACKLISTED_MESSAGES.put("com.zaxxer.hikari", Collections.singletonList(
                // This is fine, we don't need a warning about it
                "was not found, trying direct instantiation." // "Registered driver with driverClassName={} was not found, trying direct instantiation."
        ));
    }

    private final DiscordSRV discordSRV;

    public DependencyLoggingHandler(DiscordSRV discordSRV) {
        this.discordSRV = discordSRV;
    }

    @Override
    public void append(@Nullable String loggerName, @NotNull LogLevel logLevel, @Nullable String message,
                       @Nullable Throwable throwable) {
        if (loggerName == null) {
            loggerName = "null";
        }

        if (message != null) {
            List<String> blacklistedMessages = new ArrayList<>();

            // Get blacklisted messages for the logger
            for (Map.Entry<String, List<String>> entry : BLACKLISTED_MESSAGES.entrySet()) {
                if (loggerName.startsWith(entry.getKey())) {
                    blacklistedMessages.addAll(entry.getValue());
                }
            }

            // Go through the blacklisted messages we gathered
            for (String blacklistedMessage : blacklistedMessages) {
                if (message.contains(blacklistedMessage)) {
                    logLevel = LogLevel.DEBUG;
                    break;
                }
            }
        }

        // Prettify logger name, if possible
        String name = loggerName;
        for (Map.Entry<String, String> entry : LOGGER_MAPPINGS.entrySet()) {
            if (name.startsWith(entry.getKey())) {
                name = entry.getValue();
                break;
            }
        }

        if (name.equals("JDA") && message != null &&
                (message.startsWith("Encountered global rate limit!")
                || message.startsWith("Encountered cloudflare rate limit!"))) {
            if (!RATELIMIT_TIMEOUT.checkAndUpdate()) {
                discordSRV.logger().warning(message);
                return;
            }

            boolean usingProxy = discordSRV.connectionConfig().httpProxy.enabled;
            discordSRV.logger().warning("+------------------------------------------>");
            discordSRV.logger().warning("| " + message);
            discordSRV.logger().warning("| This means the " + (usingProxy ? "proxy" : "server") + "'s ip is sending too many requests to Discord");
            discordSRV.logger().warning("| The ip is being temporarily restricted from making requests to Discord");
            discordSRV.logger().warning("+------------------------------------------>");
            return;
        }

        if (name.equals("JDA") && message != null
                && message.contains("Got an unexpected error. Please redirect the following message to the devs:")
                && throwable instanceof RejectedExecutionException
                && discordSRV.status().isShutdown()) {
            // Might happen if the server shuts down while JDA is starting
            return;
        }

        discordSRV.logger().log(null, logLevel, "[" + name + "]" + (message != null ? " " + message : ""), throwable);
    }
}
