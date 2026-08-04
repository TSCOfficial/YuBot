package ch.frily.yubot.container;

import lombok.Getter;
import net.dv8tion.jda.api.components.container.Container;

import java.util.List;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticContainerRegistry {

    SERVER_CLOSE(new ServerClosedContainer().build()),
    RULES(new RulesContainer().build()),
    TICKET_PANEL(new TicketPanelContainer().build()),
    START_HERE(new StartHereContainer().build()),
    MENTAL_HEALTH(new MentalHealthHelpContainer().build()),
    ABSENCE_OVERVIEW(new AbsenceOverviewContainer(0).build());

    @Getter
    private final List<Container> containers;

    StaticContainerRegistry(List<Container> containers) {
        this.containers = containers;
    }
}
