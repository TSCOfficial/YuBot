package ch.frily.yubot.interaction.button;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface IButton {

    String getId();

    String getLabel();

    ButtonStyle getStyle();

    /**
     * define URL if style URL
     */
    default String getUrl(){
        return null;
    }

    default boolean isDisabled(){
        return false;
    }

    default EmojiUnion getEmoji(){
        return null;
    }

    default Button build(){
        String idOrUrl = getId();
        if (getStyle() == ButtonStyle.LINK && getUrl() != null) {
            idOrUrl = getUrl();
        }
        Button button = Button.of(getStyle(), idOrUrl, getLabel(), getEmoji());
        if (isDisabled()) {
            button.asDisabled();
        }
        return button;
    }

    void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException;
}
