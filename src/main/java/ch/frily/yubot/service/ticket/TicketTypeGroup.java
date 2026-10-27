package ch.frily.yubot.service.ticket;

import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

/**
 * Group of tickettypes<br>
 * This is used for the Ticket Panel
 */
public enum TicketTypeGroup {
    SUPPORT(
            "Support",
            """
                    Hier findest du Hilfe bei allgemeinen Anliegen, technischen Problemen oder Regelverstössen.
                    """,
            EnvKey.CATEGORY_TICKETSUPPORT.getAsCategory()
    ),
    AWARENESS(
            "Awareness",
            """
                    Erhalte hilfe bei persönlichen Schwierigkeiten oder Unwohlsein.
                    """,
            EnvKey.CATEGORY_TICKETAWARENESS.getAsCategory()
    ),
    BEWERBUNG(
            "Bewerbung",
            String.format("""
                    Bewirb dich auf eine offene Stelle im %s.
                    """, EnvKey.ROLE_YUTEAM.getAsRole().getAsMention()),
            EnvKey.CATEGORY_TICKETBEWERBUNG.getAsCategory()
    );

    @Getter
    private final String label;
    @Getter
    private final String description;
    @Getter
    private final Category channelCategory;

    /**
     *
     * @param label Label of the group (displayed on the panel)
     * @param description Description of the group (displayed on the panel)
     */
    TicketTypeGroup(String label, String description,  Category channelCategory) {
        this.label = label;
        this.description = description;
        this.channelCategory = channelCategory;
    }
}