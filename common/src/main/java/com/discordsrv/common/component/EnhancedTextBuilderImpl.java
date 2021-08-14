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

package com.discordsrv.common.component;

import com.discordsrv.api.component.EnhancedTextBuilder;
import com.discordsrv.api.component.MinecraftComponent;
import com.discordsrv.api.placeholder.PlaceholderService;
import com.discordsrv.common.DiscordSRV;
import com.discordsrv.common.component.util.ComponentUtil;
import dev.vankka.enhancedlegacytext.EnhancedComponentBuilder;
import dev.vankka.enhancedlegacytext.EnhancedLegacyText;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnhancedTextBuilderImpl implements EnhancedTextBuilder {

    private final Set<Object> context = new HashSet<>();
    private final Map<Pattern, Function<Matcher, Object>> replacements = new HashMap<>();

    private final DiscordSRV discordSRV;
    private final String enhancedFormat;

    public EnhancedTextBuilderImpl(DiscordSRV discordSRV, String enhancedFormat) {
        this.discordSRV = discordSRV;
        this.enhancedFormat = enhancedFormat;
    }

    @Override
    public EnhancedTextBuilder addContext(Object... context) {
        this.context.addAll(Arrays.asList(context));
        return this;
    }

    @Override
    public EnhancedTextBuilder addReplacement(Pattern target, Function<Matcher, Object> replacement) {
        this.replacements.put(target, replacement);
        return this;
    }

    @Override
    public MinecraftComponent build() {
        EnhancedComponentBuilder builder = EnhancedLegacyText.get()
                .buildComponent(enhancedFormat);

        replacements.forEach(builder::replaceAll);
        builder.replaceAll(PlaceholderService.PATTERN,
                matcher -> discordSRV.placeholderService().getResult(matcher, context));

        Component component = builder.build();
        return ComponentUtil.toAPI(component);
    }
}
