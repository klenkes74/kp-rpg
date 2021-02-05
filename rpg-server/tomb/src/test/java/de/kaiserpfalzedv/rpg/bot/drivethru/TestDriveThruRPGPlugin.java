/*
 * Copyright (c) 2021 Kaiserpfalz EDV-Service, Roland T. Lichti.
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

package de.kaiserpfalzedv.rpg.bot.drivethru;

import de.kaiserpfalzedv.rpg.core.resources.ImmutableResourceMetadata;
import de.kaiserpfalzedv.rpg.core.user.UserStoreService;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.Guild;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.ImmutableGuild;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.ImmutableGuildData;
import de.kaiserpfalzedv.rpg.integrations.discord.testsupport.*;
import io.quarkus.test.junit.QuarkusTest;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Test for the DriveThruRPG plugin for discord.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 1.2.0  2021-02-05
 */
@QuarkusTest
public class TestDriveThruRPGPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(TestDriveThruRPGPlugin.class);

    /**
     * A valid api key (40 digits, hex).
     */
    private static final String VALID_DISCORD_API_KEY = "deadb3afdeadbeafdeadbeafdeadbeafdeadbeaf";
    /**
     * An invalid api key.
     */
    private static final String INVALID_DISCORD_API_KEY = "deadbeafdeadbeafd-adbeafdeadbeafdeadbeaf";

    private static final Guild GUILD = ImmutableGuild.builder()
            .metadata(
                    ImmutableResourceMetadata.builder()
                            .kind(Guild.KIND)
                            .apiVersion(Guild.API_VERSION)

                            .namespace(Guild.DISCORD_NAMESPACE)
                            .name("the-guild")

                            .uid(UUID.randomUUID())
                            .generation(0L)
                            .created(OffsetDateTime.now(Clock.systemUTC()))

                            .build()
            )
            .spec(
                    ImmutableGuildData.builder()
                            .build()
            )
            .build();
    private static final net.dv8tion.jda.api.entities.Guild DISCORD_GUILD = new TestGuild("discord-guild");
    private static final TextChannel CHANNEL = new TestTextChannel(1L, DISCORD_GUILD, "test-channel", ChannelType.PRIVATE);
    private static final User DISCORD_USER = new TestUser(1L, "user#0001");
    /**
     * The plugin to test.
     */
    private final DriveThruRPGPlugin sut;
    private final UserStoreService userStore;

    @Inject
    public TestDriveThruRPGPlugin(
            final DriveThruRPGPlugin sut,
            final UserStoreService userStore
    ) {
        this.sut = sut;
        this.userStore = userStore;
    }

    @BeforeAll
    static void setUp() {
        MDC.put("test-class", TestDriveThruRPGPlugin.class.getSimpleName());
    }

    @AfterAll
    static void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldRegisterTheTokenWhenTokenIsValid() {
        MessageReceivedEvent input = new TestMessageEvent(
                1L,
                new TestMessage(
                        null,
                        CHANNEL,
                        DISCORD_USER,
                        "tb!dtr token " + VALID_DISCORD_API_KEY
                )
        );

        sut.workOnMessage(GUILD, input);

        Optional<de.kaiserpfalzedv.rpg.core.user.User> user = userStore.findByNameSpaceAndName(Guild.DISCORD_NAMESPACE, DISCORD_USER.getName());

        assertEquals(VALID_DISCORD_API_KEY, user.orElseThrow().getSpec().orElseThrow().getDriveThruRPGApiKey().orElseThrow());
    }

    @Test
    void shouldThrowAnExceptionWhenApiKeyIsInvalid() {
        MessageReceivedEvent input = new TestMessageEvent(
                1L,
                new TestMessage(
                        null,
                        CHANNEL,
                        DISCORD_USER,
                        "tb!dtr token " + INVALID_DISCORD_API_KEY
                )
        );

        sut.workOnMessage(GUILD, input);

        Optional<de.kaiserpfalzedv.rpg.core.user.User> user = userStore.findByNameSpaceAndName(Guild.DISCORD_NAMESPACE, DISCORD_USER.getName());

        if (user.isPresent() && user.get().getSpec().isPresent() && user.get().getSpec().get().getDriveThruRPGApiKey().isPresent()) {
            assertNotEquals(INVALID_DISCORD_API_KEY, user.orElseThrow().getSpec().orElseThrow().getDriveThruRPGApiKey().orElseThrow());
        }
    }

    @AfterEach
    void tearDownEach() {
        MDC.remove("test");
    }
}

