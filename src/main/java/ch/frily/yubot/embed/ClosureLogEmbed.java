package ch.frily.yubot.embed;

import lombok.Setter;

import java.awt.*;

public class ClosureLogEmbed implements IEmbed {

    @Setter
    private boolean isOpen;

    @Override
    public String getTitle() {
        String title = "Server geschlossen";
        if (isOpen) {
            title = "Server eröffnet";
        }
        return title;
    }

    @Override
    public String getDescription() {
        String description = "Es sind keine aktive mods mehr verfügbar - Server wurde geschlossen.";
        if (isOpen) {
            description = "Es wurde minestens 1 aktive*r Moderator*in - Server wurde eröffnet.";
        }
        return description;
    }

    @Override
    public Color getColor() {
        return new ch.frily.yubot.util.Color("c01d00").get();
    }
}
