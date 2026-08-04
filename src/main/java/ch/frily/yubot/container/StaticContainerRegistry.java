package ch.frily.yubot.container;

import lombok.Getter;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.function.Function;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticContainerRegistry {

    SERVER_CLOSE(args -> {
        return new ServerClosedContainer().build();
    }),
    RULES(args -> {
        return new RulesContainer().build();
    }),
    TICKET_PANEL(args -> {
        return new TicketPanelContainer().build();
    }),
    START_HERE(args -> {
        return new StartHereContainer().build();
    }),
    MENTAL_HEALTH(args -> {
        return new MentalHealthHelpContainer().build();
    }),
    ABSENCE_OVERVIEW(args -> {
        int currentPage = requireArg(args, 0, Integer.class);
        return new AbsenceOverviewContainer(0).build();
    }),
    ABSENCE_EDITOWN(args -> {
        int currentPage = requireArg(args, 0, Integer.class);
        Member member = requireArg(args, 1, Member.class);
        return new AbsenceEditOwnContainer(currentPage, member).build();
    });

    private final Function<Object[], List<Container>> containerFactory;

    StaticContainerRegistry(Function<Object[], List<Container>> containerFactory) {
        this.containerFactory = containerFactory;
    }

    public List<Container> getContainer(Object... args) {
        return containerFactory.apply(args);
    }

    private static <T> T requireArg(Object[] args, int index, Class<T> type) {
        if (args.length <= index) {
            throw new IllegalArgumentException("Missing argument at index " + index + " of type " + type.getSimpleName());
        }

        Object value = args[index];

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Invalid argument at index " + index + ". Expected " + type.getSimpleName()
                            + ", got " + (value == null ? "null" : value.getClass().getSimpleName())
            );
        }

        return type.cast(value);
    }
}
