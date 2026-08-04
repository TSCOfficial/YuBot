package ch.frily.yubot.interaction.button;

import ch.frily.yubot.interaction.ArgumentComponent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;

@Slf4j
public abstract class Button extends ArgumentComponent implements IButton {

    public net.dv8tion.jda.api.components.buttons.Button build(){
        String idOrUrl = getFullIdentification();
        if (getStyle() == ButtonStyle.LINK && getUrl() != null) {
            idOrUrl = getUrl();
        }
        net.dv8tion.jda.api.components.buttons.Button button = net.dv8tion.jda.api.components.buttons.Button.of(getStyle(), idOrUrl, getLabel(), getEmoji());
        if (isDisabled()) {
            button = button.asDisabled();
        }
        return button;
    }
}
