package ch.frily.yubot.container;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class Container {

    @Setter
    @Getter
    private List<ContainerChildComponent> components = new ArrayList<>();

    /**
     * Add a component to the container<br>
     * Available components:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.components.section.Section}</li>
     *     <li>{@link net.dv8tion.jda.api.components.textdisplay.TextDisplay}</li>
     *     <li>{@link net.dv8tion.jda.api.components.mediagallery.MediaGallery}</li>
     *     <li>{@link net.dv8tion.jda.api.components.buttons.Button}</li>
     *     <li>{@link net.dv8tion.jda.api.components.actionrow.ActionRow}</li>
     *     <li>...</li>
     * </ul>
     * <a href="https://docs.discord.com/developers/components/reference">Discord components reference</a>
     * @param component
     * @return
     */
    public List<ContainerChildComponent> addComponent(ContainerChildComponent component) {
        components.add(component);
        return components;
    }

    public List<ContainerChildComponent> addComponents(List<ContainerChildComponent> components) {
        this.components.addAll(components);
        return this.components;
    }

    protected net.dv8tion.jda.api.components.container.Container build() {
        return net.dv8tion.jda.api.components.container.Container.of(this.components);
    }
}
