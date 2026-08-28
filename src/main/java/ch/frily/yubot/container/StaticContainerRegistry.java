package ch.frily.yubot.container;

import net.dv8tion.jda.api.components.container.Container;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 * <p>
 * Every entry receives a {@link ContainerContext} instead of positional arguments, so a container
 * may depend on the interacting member, on foreign arguments, or on nothing at all without changing
 * the signature.
 */
public enum StaticContainerRegistry {

    SERVER_CLOSE(true, ctx -> {
        return new ServerClosedContainer().build();
    }),
    RULES(true, ctx -> {
        return new RulesContainer().build();
    }),
    TICKET_PANEL(true, ctx -> {
        return new TicketPanelContainer().build();
    }),
    START_HERE(true, ctx -> {
        return new StartHereContainer().build();
    }),
    MENTAL_HEALTH(true, ctx -> {
        return new MentalHealthHelpContainer().build();
    }),
    ABSENCE_OVERVIEW(true, ctx -> {
        return new AbsenceOverviewContainer(ctx).build();
    }),
    /** Renders the absences of the interacting member, so it only works with an interaction present (therefore not sendable) */
    ABSENCE_EDITOWN(false, ctx -> {
        return new AbsenceEditOwnContainer(ctx).build();
    }),
    ACTIVE_MOD_CONTROL(true, ctx -> {
        return new ActiveModDashboardContainer(ctx).build();
    });

    /** Whether the container may be sent as a standalone message via the send container command */
    private final boolean sendableStatically;

    private final Function<ContainerContext, List<Container>> containerFactory;

    StaticContainerRegistry(boolean sendableStatically, Function<ContainerContext, List<Container>> containerFactory) {
        this.sendableStatically = sendableStatically;
        this.containerFactory = containerFactory;
    }

    public List<Container> getContainer(ContainerContext context) {
        return containerFactory.apply(context);
    }

    /**
     * The containers that make sense as a standalone message
     * <p>
     * Per-user containers are left out because they would be rendered for whoever ran the command
     * and then stay that way for everyone reading the channel.
     * @return The containers that may be sent via the send container command
     */
    public static List<StaticContainerRegistry> sendable() {
        return Arrays.stream(values())
                .filter(container -> container.sendableStatically)
                .toList();
    }
}
