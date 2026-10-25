package ch.frily.yubot.service.ticket;

import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public enum TicketTeam {
    SUPPORT_TEAM(
            "\uD83D\uDD29 Supporter*innen",
            "Das Support-Team",
            List.of(
                EnvKey.ROLE_SUPPORT.getAsRole()
            )
    ),
    SUPPORT_LEITUNG(
            "\uD83D\uDD29 Supporter*innen-Leitung",
            "Die Support-Leitung und Stellvertretung",
            List.of(
                    EnvKey.ROLE_SUPPORTLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVSUPPORTLEITUNG.getAsRole()
            )
    ),
    MODERATION_TEAM(
            "⚒\uFE0F Moderator*innen",
            "Das Moderations-Team",
            List.of(
                    EnvKey.ROLE_MODERATOR.getAsRole()
            )
    ),
    MODERATION_LEITUNG(
            "\uD83D\uDEE0\uFE0F Moderator*innen-Leitung",
            "Die Moderations-Leitung und Stellvertretung",
            List.of(
                    EnvKey.ROLE_MODLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVMODLEITUNG.getAsRole()
            )
    ),
    EVENT_TEAM(
            "\uD83C\uDF8A Event-Team",
            "Das Event-Team",
            List.of(
                    EnvKey.ROLE_EVENT.getAsRole()
            )
    ),
    EVENT_LEITUNG(
            "\uD83C\uDF89 Event-Leitung",
            "Die Event-Leitung und Stellvertretung",
            List.of(
                    EnvKey.ROLE_EVENTLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVEVENTLEITUNG.getAsRole()
            )
    ),
    DEVELOPMENT_TEAM(
            "💻 Development-Team",
            "Das Entwickluns-team",
            List.of(
                    EnvKey.ROLE_DEVELOPER.getAsRole()
            )
    ),
    AWARENESS_TEAM(
            "\uD83D\uDC9A Awareness-Team",
            "Das Awareness-Team",
            List.of(
                    EnvKey.ROLE_AWARENESS.getAsRole()
            )
    ),
    AWARENESS_LEITUNG(
            "\uD83C\uDF3F Awareness-Leitung",
            "Die Awareness-Leitung und Stellvertretung",
            List.of(
                    EnvKey.ROLE_AWARENESSLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVAWARENESSLEITUNG.getAsRole()
            )
    ),
    ORGANISATION(
            "\uD83E\uDE9B Organisation",
            "Leitungen & Stellvertretungen aller Fachbereiche",
            List.of(
                    EnvKey.ROLE_SUPPORTLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVSUPPORTLEITUNG.getAsRole(),
                    EnvKey.ROLE_MODLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVMODLEITUNG.getAsRole(),
                    EnvKey.ROLE_EVENTLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVEVENTLEITUNG.getAsRole(),
                    EnvKey.ROLE_AWARENESSLEITUNG.getAsRole(),
                    EnvKey.ROLE_STVAWARENESSLEITUNG.getAsRole(),
                    EnvKey.ROLE_DEVLEITUNG.getAsRole()
            )
    ),
    SERVER_LEITUNG(
            "⚙\uFE0F Server-Leitung",
            "Die Serverleitung (Leitungs-ticket)",
            List.of(
            EnvKey.ROLE_SERVERLEITUNG.getAsRole()
            )
    );

    @Getter
    final String name;

    /**
     * Description of the team
     * <p>
     *     May not be longer than 100 characters
     * </p>
     */
    @Getter
    final String description;

    @Getter
    final List<Role> roles;

    TicketTeam(String name, String description, List<Role> roles) {
        this.name = name;
        this.description = description;
        this.roles = roles;
    }
}
