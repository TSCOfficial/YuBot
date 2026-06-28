package ch.frily.yubot.container;

import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.feature.TicketStatus;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.util.Color;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TicketPanelContainer extends Container {

    public TicketPanelContainer() {
        this.setColor(new Color("789bac").get());

        this.addTextDisplay("## Ticketsystem");
        this.addTextDisplay("Wähle die passende Kategorie aus, drücke den dazugehörigen Button und wähle anschliessend die gewünschte Ticketart aus.");
        this.addTextDisplay(String.format(
                "Falls du Ein Vorschlag oder anderweitiges Feedback geben willst, kannst du dies gerne in %s ",
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_FEEDBACK).getAsMention()
                )
        );

        Arrays.stream(TicketTypeGroup.values()).forEach(typeGroup -> {

            String groupCategories = Arrays.stream(TicketType.values()).filter(type -> type.getGroup() == typeGroup).map(type -> {
                return Util.format("- {}", type.getLabel());
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
