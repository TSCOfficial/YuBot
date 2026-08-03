package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.Button;
import lombok.Setter;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

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
        if (emoji == null)
            return null;
        return Emoji.fromFormatted("<:home:1526737131282763816>");
    }

    @Override
    public String getLabel() {
        if (label == null)
            return null;
        return label;
    }

    @Override
    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        if (hasArgument(event.getComponentId(), "navigateTo")){

        }
    }
}
