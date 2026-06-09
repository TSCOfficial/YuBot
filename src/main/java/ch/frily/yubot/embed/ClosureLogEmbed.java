package ch.frily.yubot.embed;

import lombok.Setter;

import java.awt.*;

public class ClosureLogEmbed implements IEmbed {

    @Setter
    private boolean isOpen;

    public ClosureLogEmbed(boolean isOpen){
        this.isOpen = isOpen;
    }

    @Override
    public String getTitle() {
        if (isOpen) {
            return "🔓 Server eröffnet";
        } else {
            return "🔒 Server geschlossen";
        }
    }

    @Override
    public String getDescription() {
        if (isOpen) {
            return "Es wurde minestens 1 aktive\\*r Moderator\\*in - Server wurde eröffnet.";
        } else {
            return "Es sind keine aktive mods mehr verfügbar - Server wurde geschlossen.";
        }
    }

    @Override
    public Color getColor() {
        if (isOpen) {
            return ch.frily.yubot.util.Color.GREEN;
        } else {
            return ch.frily.yubot.util.Color.RED;
        }
    }
}
