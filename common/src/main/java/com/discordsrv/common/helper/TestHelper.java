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

package com.discordsrv.common.helper;

import java.util.function.Consumer;

/**
 * Data access for unit testing.
 */
public final class TestHelper {

    private static final ThreadLocal<Consumer<Throwable>> error = new ThreadLocal<>();

    public static void fail(Throwable throwable) {
        Consumer<Throwable> handler = error.get();
        if (handler != null) {
            handler.accept(throwable);
        }
    }

    public static void set(Consumer<Throwable> handler) {
        error.set(handler);
    }
}
