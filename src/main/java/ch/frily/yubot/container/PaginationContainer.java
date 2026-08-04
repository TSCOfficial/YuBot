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

    private final StaticContainerRegistry identifier;

    /** The arguments this container was built with, propagated into the navigation buttons */
    private final ContainerContext context;

    /** Zero based index of the page that gets rendered */
    private int currentPage;

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

    protected PaginationContainer(StaticContainerRegistry identifier, ContainerContext context){
        this.identifier = identifier;
        this.context = context;
        this.currentPage = context.page();
        log.debug("Created pagination container with identifier {} on page {}", identifier, currentPage);
    }

    /** The arguments this container was built with */
    protected ContainerContext getContext(){
        return context;
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
                log.warn("A single pagination item (index {}) takes up {} components but only {} fit on a page",
                        items.indexOf(item), itemComponents, budget);
                item = new PaginationItem(TextDisplay.ofFormat("-# ⚠️ Item %d, with %d children, was not able to render due to discord limitations.", items.indexOf(item), itemComponents)); // overwrite to fallback message
                itemComponents = item.getComponentCount(); // overwrite component cound to allow other items to be displayed next to the fallback message
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
    }

    /**
     * Build the container of the {@link #currentPage current page}, framed by the header, the footer, and the navigation controls
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

    /**
     * Build a navigation button that rebuilds this very container on a different page.
     * <p>
     * Next to the identifier and the target page, every foreign argument of the
     * {@link ContainerContext} is written into the button as well. That is what lets a container
     * with extra requirements survive a navigation click: the arguments it was built with are
     * handed back to it, while member and guild get derived from the interaction.
     * @param targetPage Zero based index of the page the button navigates to
     * @return The prepared button, still without label, emoji or style
     */
    private PaginationNavBtn navBtn(int targetPage) {
        PaginationNavBtn btn = new PaginationNavBtn();
        btn.addArgument(ContainerContext.ARG_IDENTIFIER, identifier.name());
        btn.addArgument(ContainerContext.ARG_PAGE, String.valueOf(targetPage));
        context.args().forEach(btn::addArgument);
        return btn;
    }

    public PaginationItem getControls() {
        PaginationItem controls = new PaginationItem();

        PaginationNavBtn homeBtn = navBtn(0);
        homeBtn.setEmoji(Emoji.fromFormatted("<:home:1526737131282763816>"));
        if (currentPage == 0) {
            homeBtn.setDisabled(true);
        }

        PaginationNavBtn prevBtn = navBtn(currentPage - 1);
        prevBtn.setLabel("<");
        prevBtn.setStyle(ButtonStyle.SECONDARY);
        if (currentPage <= 0) {
            prevBtn.setDisabled(true);
        }

        // Serves as a page indicator only, so it carries no navigation target
        PaginationNavBtn currentBtn = new PaginationNavBtn();
        currentBtn.setLabel(String.valueOf(currentPage + 1)); // +1 only to display human-readable page number (instead of index 0)
        currentBtn.setStyle(ButtonStyle.SECONDARY);
        currentBtn.setDisabled(true);

        PaginationNavBtn nextBtn = navBtn(currentPage + 1);
        nextBtn.setLabel(">");
        nextBtn.setStyle(ButtonStyle.SECONDARY);
        if (currentPage >= pages.size() - 1) {
            nextBtn.setDisabled(true);
        }

        controls.addChild(Separator.createDivider(Separator.Spacing.LARGE));
        controls.addChild(ActionRow.of(homeBtn.build(), prevBtn.build(), currentBtn.build(), nextBtn.build()));
        controls.addChild(TextDisplay.ofFormat("-# Seite %s / %s", currentPage + 1, pages.size()));
        return controls;
    }

    @Override
    public List<net.dv8tion.jda.api.components.container.Container> build() {
        return List.of(buildPagination());
    }
}
