package ch.frily.yubot.container;

import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.feature.TicketStatus;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.util.Color;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TicketPanelContainer extends Container {

    public TicketPanelContainer() {
        this.setColor(new Color("789bac").get());

        this.addTextDisplay("## Ticketsystem");
        this.addTextDisplay("Wähle den passenden Kontaktbereich aus, drücke den dazugehörigen Button und wähle anschliessend die gewünschte Kategorie aus.");

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
            this.addTextDisplay("**Verfügbare Kategorien:**");
            this.addTextDisplay(groupCategories);

        });
    }
}
