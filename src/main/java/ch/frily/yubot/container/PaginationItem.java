package ch.frily.yubot.container;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.utils.ComponentIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of children components that are bound contextually, to prevent unfortunate splits when builder the pages
 */
public class PaginationItem {

    @Getter
    private List<ContainerChildComponent> children = new ArrayList<>();

    @Setter
    private String title;

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

    public TextDisplay getTitleDisplay(){
        return TextDisplay.of(title);
    }

    /**
     * All components with the optional title
     */
    public List<ContainerChildComponent> getRenderComponents(){
        if (title == null) {
            return children;
        }
        List<ContainerChildComponent> rendered = new ArrayList<>();
        rendered.add(getTitleDisplay());
        rendered.addAll(children);
        return rendered;
    }

    /**
     * Amount of components this item takes up in the message component tree
     */
    public int getComponentCount(){
        return (int) ComponentIterator.createStream(getRenderComponents()).count();
    }

    /**
     * Component count of a single child
     */
    private static int childComponentCount(ContainerChildComponent child){
        return (int) ComponentIterator.createStream(List.of(child)).count();
    }

    /**
     * Split this item into chunks that each fit into the given component budget. Each split item carries the tite (of present)
     * @param budget Maximum component count a single chunk may occupy
     * @return The chunks in order, each carrying the same title
     */
    public List<PaginationItem> split(int budget){
        if (getComponentCount() <= budget) {
            return List.of(this);
        }

        int titleCost = title == null ? 0 : 1;
        List<PaginationItem> chunks = new ArrayList<>();
        PaginationItem current = newChunk();
        int used = titleCost;

        for (ContainerChildComponent child : children) {
            int childCost = childComponentCount(child);

            // Even alone next to the title this child cannot fit, so it can never be rendered
            if (titleCost + childCost > budget) {
                child = TextDisplay.of("-# ⚠️ Ein Eintrag konnte aufgrund von Discord-Limitierungen nicht dargestellt werden.");
                childCost = childComponentCount(child);
            }

            // Start the next chunk before the current one runs over the budget
            if (!current.children.isEmpty() && used + childCost > budget) {
                chunks.add(current);
                current = newChunk();
                used = titleCost;
            }

            current.addChild(child);
            used += childCost;
        }

        if (!current.children.isEmpty()) {
            chunks.add(current);
        }
        return chunks;
    }

    /**
     * Create an empty chunk that inherits the item title
     */
    private PaginationItem newChunk(){
        PaginationItem chunk = new PaginationItem();
        chunk.title = title;
        return chunk;
    }
}
