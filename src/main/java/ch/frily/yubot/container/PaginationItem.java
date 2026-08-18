package ch.frily.yubot.container;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.utils.ComponentIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of children components that are bound contextually, to prevent unfortunate splits when builder the pages
 */
@Slf4j
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
     * @return The chunks in order, each carrying the same title
     */
    public List<PaginationItem> split(PaginationContainer container){
        int budget = container.getItemComponentBudget() - container.getCurrentPageComponentCount();
        log.info("new split with budget: {}", budget);
        if (getComponentCount() <= budget) {
            return List.of(this);
        }

        int titleCost = title == null ? 0 : 1;
        List<PaginationItem> chunks = new ArrayList<>();
        PaginationItem current = newChunk();

        for (ContainerChildComponent child : children) {
            int childCost = childComponentCount(child);
            int childrenBudget = container.getItemComponentBudget() - container.getCurrentPageComponentCount();

            // Start the next chunk before the current one runs over the budget
            log.info("Splitting item with {} components into {} chunks: used: {}, childcost: {}, budget: {}, equation: {}", getComponentCount(), chunks.size() + 1, container.getCurrentPageComponentCount(), childCost, childrenBudget, container.getItemComponentBudget() - container.getCurrentPageComponentCount() - childCost - titleCost <= 0);
            if (!current.children.isEmpty() && container.getItemComponentBudget() - container.getCurrentPageComponentCount() - childCost - titleCost <= 0) {
                log.info("added chunk");
                chunks.add(current);
                current = newChunk();
                container.setCurrentPageComponentCount(titleCost);
            }

            current.addChild(child);
            container.setCurrentPageComponentCount(container.getCurrentPageComponentCount() + childCost+ titleCost);
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
