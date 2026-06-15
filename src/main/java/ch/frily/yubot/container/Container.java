package ch.frily.yubot.container;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class Container {

    @Setter
    @Getter
    private List<ContainerChildComponent> components = new ArrayList<>();

    @Getter
    @Setter
    private List<net.dv8tion.jda.api.components.container.Container> containers = new ArrayList<>();

    @Getter
    @Setter
    private Color color;

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

    /**
     * Add a {@link TextDisplay} component
     * @param text
     * @return
     */
    public List<ContainerChildComponent> addTextDisplay(String text){
        return addComponent(TextDisplay.of(text));
    }

    public List<ContainerChildComponent> addSection(SectionAccessoryComponent accessory, SectionContentComponent... components){
        return addComponent(Section.of(accessory, Arrays.asList(components)));
    }

    /**
     * Add a (invisible) separator
     * @return
     */
    public List<ContainerChildComponent> addInvisibleSeparator(Separator.Spacing spacing){
        return addComponent(Separator.createInvisible(spacing));
    }

    /**
     * Add a (visible) divider
     * @return
     */
    public List<ContainerChildComponent> addLineSeparator(Separator.Spacing spacing){
        return addComponent(Separator.createDivider(spacing));
    }

    public List<net.dv8tion.jda.api.components.container.Container> addContainer(net.dv8tion.jda.api.components.container.Container container){
        this.containers.add(container);
        return this.containers;
    }

    public void buildContainerWithCurrentComponents() {
        containers.add(net.dv8tion.jda.api.components.container.Container.of(components));
        components.clear();
    }

    public List<net.dv8tion.jda.api.components.container.Container> build() {
        if (!components.isEmpty()) {
            List<ContainerChildComponent> newComponents = new ArrayList<>();

            for (int i = 0; i < components.size(); i++) {
                if (i > 0 && i % 40 == 0) {
                    net.dv8tion.jda.api.components.container.Container newContainer =
                            net.dv8tion.jda.api.components.container.Container.of(newComponents);
                    newContainer = newContainer.withAccentColor(color);
                    containers.add(newContainer);
                    newComponents.clear();
                }
                newComponents.add(components.get(i));
            }

            // Add remaining components to new container
            if (!newComponents.isEmpty()) {
                net.dv8tion.jda.api.components.container.Container newContainer =
                        net.dv8tion.jda.api.components.container.Container.of(newComponents);
                newContainer = newContainer.withAccentColor(color);
                containers.add(newContainer);
            }
        }

        return containers;
    }

}
