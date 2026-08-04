package ch.frily.yubot.container;

import ch.frily.yubot.interaction.button.btn.PaginationNavBtn;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PaginationContainer extends Container {

    private static final Logger log = LoggerFactory.getLogger(PaginationContainer.class);

    /**
     * Holds a list of items that build up a page
     * @param number
     * @param items
     */
    record Page(int number, List<PaginationItem> items){}

    private int currentPage = 0;

    private static final int MAX_COMPONENTS_PER_PAGE = 30;

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

    private void addPage(int number, List<PaginationItem> items){
        pages.add(new Page(number, List.copyOf(items)));
    }

    public PaginationContainer(int currentPage){
        this.currentPage = currentPage;
    }

    /**
     * Build the pages from the items
     */
    private void buildPages(){
        List<PaginationItem> currentProcessedItems = new ArrayList<>();
        AtomicInteger componentsCount = new AtomicInteger(0);
        log.info("amount of items: {} at start of build", items.size());
        items.forEach(item -> {
            int componentLength = item.getChildren().size();
            int newComponentCount = componentsCount.addAndGet(1);
            log.info("Adding item to page: " + newComponentCount);
            currentProcessedItems.add(item);

            if (newComponentCount >= MAX_COMPONENTS_PER_PAGE){
                log.info("adding page and clearning info");
                addPage(pages.size() + 1, currentProcessedItems);
                currentProcessedItems.clear();
                componentsCount.set(0);
            }
        });
        addPage(pages.size() + 1, currentProcessedItems);
        log.info("Built Fallback page pages with {} items", currentProcessedItems.size());
        currentProcessedItems.clear();
        componentsCount.set(0);
    }

    public net.dv8tion.jda.api.components.container.Container buildPagination() {
        buildPages();
        log.info("Built {} pages with {} items", pages.size(), pages.getFirst().items().size());
        log.info("Pages: {}", pages); // -> Pages: [Page[number=1, items=[]]]
        List<ContainerChildComponent> components = new ArrayList<>();
        pages.get(currentPage).items().forEach(item -> { // the pages seem to loos their items??
            log.info("Child item count: " + item.getChildren().size());
            components.addAll(item.getChildren());
        });
        return net.dv8tion.jda.api.components.container.Container.of(components);
    }

    public ActionRow getControls() {
        PaginationNavBtn homeBtn = new PaginationNavBtn();
        homeBtn.setEmoji(Emoji.fromFormatted("<:home:1526737131282763816>"));
        homeBtn.addArgument("navigateTo", "1");

        PaginationNavBtn prevBtn = new PaginationNavBtn();
        prevBtn.setLabel("<");
        prevBtn.setStyle(ButtonStyle.SECONDARY);
        prevBtn.addArgument("navigateTo", "prev");

        PaginationNavBtn currentBtn = new PaginationNavBtn();
        prevBtn.setLabel("current");
        prevBtn.setStyle(ButtonStyle.SECONDARY);
        prevBtn.setDisabled(true);

        PaginationNavBtn nextBtn = new PaginationNavBtn();
        nextBtn.setLabel(">");
        prevBtn.setStyle(ButtonStyle.SECONDARY);
        nextBtn.addArgument("navigateTo", "next");

        return ActionRow.of(homeBtn.build(), prevBtn.build(), currentBtn.build(), nextBtn.build());
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
