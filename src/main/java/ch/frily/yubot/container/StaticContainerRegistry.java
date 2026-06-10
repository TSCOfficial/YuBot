package ch.frily.yubot.container;

import lombok.Getter;
import net.dv8tion.jda.api.components.container.Container;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticContainerRegistry {

    SERVER_CLOSE_CONTAINER(new ServerClosedContainer().build());

    @Getter
    private final Container container;

    StaticContainerRegistry(Container container) {
        this.container = container;
    }
}
