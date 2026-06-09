package ch.frily.yubot.container;

import net.dv8tion.jda.api.components.container.Container;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticContainerRegistry {

    SERVER_CLOSE_CONTAINER(new ServerClosedContainer().build());

    private final Container container;

    StaticContainerRegistry(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }
}
