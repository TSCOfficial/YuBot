package ch.frily.yubot.feature;

import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum AbsenceType {
    FULLY_ABSENT("Vollständig abwesend", "Nimm dir die Auszeit - Kaum bis gar erreichbar", Emoji.fromCustom("fully_absent", 1534907908356050984L, false)),
    REACHABLE("Abwesend aber erreichbar", "Eigentlich nicht da, aber falls was ist erreichbar", Emoji.fromCustom("reachable", 1534907904212074536L, false)),
    PARTIALLY_ABSENT("Teilweise abwesend", "Schaue hin und wieder mal drauf ob was wichtiges gebraucht wird", Emoji.fromCustom("partialy_absent", 1534911839450828840L, false));

    @Getter
    private final String label;
    @Getter
    private final String description;
    @Getter
    private final Emoji emoji;

    AbsenceType(String label, String description, Emoji emoji) {
        this.label = label;
        this.description = description;
        this.emoji = emoji;
    }
}
