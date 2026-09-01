package ch.frily.yubot.feature.ticket;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;

/**
 * Group of tickettypes<br>
 * This is used for the Ticket Panel
 */
public enum TicketTypeGroup {
    SUPPORT(
            "Support",
            """
                    Hier findest du Hilfe bei allgemeinen Anliegen, technischen Problemen oder Regelverstössen.
                    """
    ),
    AWARENESS(
            "Awareness",
            """
                    Erhalte hilfe bei persönlichen Schwierigkeiten oder Unwohlsein.
                    """
    ),
    BEWERBUNG(
            "Bewerbung",
            String.format("""
                    Bewirb dich auf eine offene Stelle im %s.
                    """, EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM).getAsMention())
    );

    @Getter
    private final String label;
    @Getter
    private final String description;

    /**
     *
     * @param label Label of the group (displayed on the panel)
     * @param description Description of the group (displayed on the panel)
     */
    TicketTypeGroup(String label, String description) {
        this.label = label;
        this.description = description;
    }
}