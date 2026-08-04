package ch.frily.yubot.container;

import ch.frily.yubot.interaction.button.btn.PaginationNavBtn;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginationContainer extends Container {

    private static final Logger log = LoggerFactory.getLogger(PaginationContainer.class);

    /**
     * Holds a list of items that build up a page
     * @param number
     * @param items
     */
    record Page(int number, List<PaginationItem> items){}

    /** Zero based index of the page that gets rendered */
    private int currentPage = 0;

    /** The container wrapping a page counts as a component itself */
    private static final int CONTAINER_COMPONENT_COUNT = 1;

    /** List of the items */
    private List<PaginationItem> items = new ArrayList<>();

    @Getter
    @Setter
    private PaginationItem header;

    @Getter
    @Setter
    private PaginationItem footer;

    /** List of the pages */
    private List<Page> pages = new ArrayList<>();

    protected void addItem(PaginationItem item){
        this.items.add(item);
    }

    protected void addItem(ContainerChildComponent child){
        this.items.add(new PaginationItem(child));
    }

    protected void addItems(PaginationItem[] items){
        this.items.addAll(List.of(items));
    }

    private void addPage(List<PaginationItem> items){
        pages.add(new Page(pages.size() + 1, List.copyOf(items)));
    }

    public PaginationContainer(int currentPage){
        this.currentPage = currentPage;
        log.info("new pagination container with current page {}", currentPage);
    }

    /**
     * Amount of components an item may contribute to a single page.
     * <p>
     * Discord limits a message to {@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE} components
     * including all nested ones, so everything that is rendered on every page has to be subtracted
     * from that budget before the items get distributed.
     * @return The components left over for the items of one page
     */
    private int getItemComponentBudget(){
        log.info("control components: {}", (int) ComponentIterator.createStream(getControls().getChildren()).count());
        int reserved = CONTAINER_COMPONENT_COUNT
                + (int) ComponentIterator.createStream(getControls().getChildren()).count()
                + (header == null ? 0 : header.getComponentCount())
                + (footer == null ? 0 : footer.getComponentCount());
        return Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE - reserved;
    }

    /**
     * Build the pages from the items
     * <p>
     * An item is never split up, so a new page is started as soon as the next item would exceed the
     * {@link #getItemComponentBudget() component budget} of the current one.
     */
    private void buildPages(){
        pages.clear();

        int budget = getItemComponentBudget();
        List<PaginationItem> currentProcessedItems = new ArrayList<>();
        int usedComponents = 0;

        for (PaginationItem item : items) {
            int itemComponents = item.getComponentCount();

            if (itemComponents > budget) {
                log.warn("A single pagination item takes up {} components but only {} fit on a page",
                        itemComponents, budget);
            }

            // Start the next page before the current one runs over the budget
            if (!currentProcessedItems.isEmpty() && usedComponents + itemComponents > budget) {
                addPage(currentProcessedItems);
                currentProcessedItems.clear();
                usedComponents = 0;
            }

            currentProcessedItems.add(item);
            usedComponents += itemComponents;
        }

        // Keep at least one (possibly empty) page so there is always something to render
        if (!currentProcessedItems.isEmpty() || pages.isEmpty()) {
            addPage(currentProcessedItems);
        }

        log.debug("Built {} pages out of {} items with a budget of {} components per page",
                pages.size(), items.size(), budget);
    }

    /**
     * Build the container of the {@link #currentPage current page}, framed by the header, the footer
     * and the navigation controls
     * @return
     */
    public net.dv8tion.jda.api.components.container.Container buildPagination() {
        buildPages();

        // The requested page may no longer exist, e.g. when items were removed in the meantime
        currentPage = Math.clamp(currentPage, 0, pages.size() - 1);

        List<ContainerChildComponent> components = new ArrayList<>();
        if (header != null) {
            components.addAll(header.getChildren());
        }
        pages.get(currentPage).items().forEach(item -> components.addAll(item.getChildren()));
        if (footer != null) {
            components.addAll(footer.getChildren());
        }
        if (pages.size() > 1) {
            components.addAll(getControls().getChildren());
        }

        net.dv8tion.jda.api.components.container.Container container =
                net.dv8tion.jda.api.components.container.Container.of(components);
        if (getColor() != null) {
            container = container.withAccentColor(getColor());
        }
        return container;
    }

    public PaginationItem getControls() {
        PaginationItem controls = new PaginationItem();

        PaginationNavBtn homeBtn = new PaginationNavBtn();
        homeBtn.setEmoji(Emoji.fromFormatted("<:home:1526737131282763816>"));
        homeBtn.addArgument("navigateTo", "0");
        if (currentPage == 0) {
            homeBtn.setDisabled(true);
        }

        PaginationNavBtn prevBtn = new PaginationNavBtn();
        prevBtn.setLabel("<");
        prevBtn.setStyle(ButtonStyle.SECONDARY);
        prevBtn.addArgument("navigateTo", String.valueOf(currentPage - 1));
        if (currentPage <= 0) {
            prevBtn.setDisabled(true);
        }

        PaginationNavBtn currentBtn = new PaginationNavBtn();
        currentBtn.setLabel(String.valueOf(currentPage + 1)); // +1 only to display human-readable page number (instead of index 0)
        currentBtn.setStyle(ButtonStyle.SECONDARY);
        currentBtn.setDisabled(true);

        PaginationNavBtn nextBtn = new PaginationNavBtn();
        nextBtn.setLabel(">");
        nextBtn.setStyle(ButtonStyle.SECONDARY);
        nextBtn.addArgument("navigateTo", String.valueOf(currentPage + 1));
        if (currentPage >= pages.size() - 1) {
            nextBtn.setDisabled(true);
        }

        controls.addChild(Separator.createDivider(Separator.Spacing.LARGE));
        controls.addChild(ActionRow.of(homeBtn.build(), prevBtn.build(), currentBtn.build(), nextBtn.build()));
        controls.addChild(TextDisplay.ofFormat("-# Seite %s / %s", currentPage + 1, pages.size()));
        return controls;
    }

//    @Override
//    public List<net.dv8tion.jda.api.components.container.Container> build() {
//       throw new IllegalStateException("PaginationContainer can only be built as pagination. Use buildPagination() instead.");
//    }


    @Override
    public List<net.dv8tion.jda.api.components.container.Container> build() {
        return List.of(buildPagination());
    }
}
