package ch.frily.yubot.container;

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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class TicketPanelContainer extends Container {

    public TicketPanelContainer() {

        boolean isServerOpen = ClosureRepository.hasActiveModerators();
        AtomicBoolean hasAnyClosedTypes = new AtomicBoolean(false);

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
                                hasAnyClosedTypes.set(true);
                                return String.format("- %s <:lock:1521623305017102346>", type.getLabel());
                            }
                            return String.format("- %s", type.getLabel());
                        } catch (Exception e) {
                            log.error("Failed to format ticket type", e);
                            return String.format("- %s", type.getLabel());
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

        if (hasAnyClosedTypes.get()) {
            this.addLineSeparator(Separator.Spacing.LARGE);
            this.addTextDisplay("-# *<:lock:1521623305017102346>: Ticketart ist momentan gesperrt und kann nicht geöffnet werden.*");
        }
    }
}
