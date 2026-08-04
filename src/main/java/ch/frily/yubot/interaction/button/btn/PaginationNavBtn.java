package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.interaction.button.Button;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Slf4j
public class PaginationNavBtn extends Button {

    @Setter
    /** Define button style. Default: {@link ButtonStyle#PRIMARY}*/
    private ButtonStyle style = ButtonStyle.PRIMARY;
    @Nullable
    @Setter
    private EmojiUnion emoji;
    @Nullable
    @Setter
    private String label;
    @Setter
    private boolean isDisabled = false;

    @Override
    public String getId() {
        return "pagination-nav";
    }

    @Override
    public ButtonStyle getStyle() {
        return style;
    }

    @Override
    public EmojiUnion getEmoji() {
        return emoji;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * Re-render the paginated container on the page this button points to.
     * <p>
     * The container gets rebuilt instead of cached, so a page always shows current data.
     * Everything needed for that comes out of the component ID and the event itself, which is why
     * this works for any paginated container without knowing what that container requires.
     */
    @Override
    public void execute(@NonNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        log.debug("Pagination navigation for component ID {}", componentId);

        StaticContainerRegistry registry = StaticContainerRegistry.valueOf(
                getArgument(componentId, ContainerContext.ARG_IDENTIFIER));
        ContainerContext context = ContainerContext.fromComponentId(event);

        event.editComponents(registry.getContainer(context))
                .useComponentsV2()
                .setAllowedMentions(List.of())
                .queue();
    }
}
