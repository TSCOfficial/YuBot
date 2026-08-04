package ch.frily.yubot.container;

import lombok.Getter;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.utils.ComponentIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of children components that are bound contextually, to prevent unfortunat splits when builder the pages
 */
public class PaginationItem {

    @Getter
    private List<ContainerChildComponent> children = new ArrayList<>();

    public PaginationItem(){

    }
    public PaginationItem(ContainerChildComponent child){
        this.addChild(child);
    }
    public PaginationItem(List<ContainerChildComponent> children){
        this.addChildren(children);
    }

    public void addChild(ContainerChildComponent child){
        this.children.add(child);
    }

    public void addChildren(List<ContainerChildComponent> children){
        this.children.addAll(children);
    }

    /**
     * Amount of components this item takes up in the message component tree.
     * <p>
     * Discord counts nested components as well, so a
     * {@link net.dv8tion.jda.api.components.section.Section} holding a text display and an
     * accessory button counts as three components instead of one.
     * @return The recursive component count of all children
     */
    public int getComponentCount(){
        return (int) ComponentIterator.createStream(children).count();
    }
}
