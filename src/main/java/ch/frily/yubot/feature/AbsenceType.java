package ch.frily.yubot.feature;

import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum AbsenceType {
    FULLY_ABSENT("Vollständig abwesend", "Nimm dir die Auszeit - Kaum bis gar erreichbar", Emoji.fromCustom("fully_absent", 1538631485211549837L, false)),
    REACHABLE("Abwesend aber erreichbar", "Eigentlich nicht da, aber falls was ist erreichbar", Emoji.fromCustom("reachable", 1538631488398950410L, false)),
    PARTIALLY_ABSENT("Teilweise abwesend", "Schaue hin und wieder mal drauf ob was wichtiges gebraucht wird", Emoji.fromCustom("partially_absent", 1538631486390149230L, false));

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
