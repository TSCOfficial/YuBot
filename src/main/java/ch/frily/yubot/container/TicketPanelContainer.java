package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.*;
import ch.frily.yubot.util.Color;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TicketPanelContainer extends Container {

    public TicketPanelContainer(boolean isServerOpen) {
        this.setColor(new Color("789bac").get());

        this.addTextDisplay("## Ticketsystem");
        this.addTextDisplay("Wähle die passende Kategorie aus, drücke den dazugehörigen Button und wähle anschliessend die gewünschte Ticketart aus.");
        this.addTextDisplay(String.format(
                "-# Falls du ein Vorschlag oder anderweitiges Feedback geben willst, kannst du dies gerne in %s schreiben.",
                EnvResolver.getChannelById(ForumChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_FEEDBACK).getAsMention()
                )
        );

        if (!isServerOpen) {
            Channel serverClosedChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN);
            Channel startHereChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_STARTHERE);
            this.addTextDisplay(String.format("""
                    ### ⚠️ Server geschlossen
                    **Hinweis:** Der Server ist momentan geschlossen, du siehst also keine Communitychannels mehr. Das Ticketsystem ist aber weiterhin verfügbar.
                    -# Für weitere Informationen, besuche %s und %s.
                    """, serverClosedChannel.getAsMention(), startHereChannel.getAsMention()));
        }

        Arrays.stream(TicketTypeGroup.values()).forEach(typeGroup -> {

            String groupCategories = Arrays.stream(TicketType.values())
                    .filter(type -> type.getGroup() == typeGroup)
                    .map(type -> {
                        try {
                            boolean typeIsLocked = TicketTypeControlRepository.isTypeLocked(type);

                            if (typeIsLocked) {
                                return Util.format("- %s (gesperrt)", type.getLabel());
                            }
                            return Util.format("- {}", type.getLabel());
                        } catch (Exception e) {
                            return ExceptionHandler.fail(e);
                        }
                    }).collect(Collectors.joining("\n"));

            this.addLineSeparator(Separator.Spacing.LARGE);

            this.addSection(
                    TicketManager.getInstance().getButtonByTypeGroup(typeGroup).build(),
                    TextDisplay.of(Util.format("### {}", typeGroup.getLabel())),
                    TextDisplay.of(typeGroup.getDescription())
            );
            this.addTextDisplay("**Verfügbare Arten:**");
            this.addTextDisplay(groupCategories);

        });
    }
}
