package ch.frily.yubot.service.ticket;

import ch.frily.yubot.Client;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;

import java.util.*;

@Slf4j
public enum TicketTeam {
    SUPPORT_TEAM(
            "\uD83D\uDD29 Supporter*innen",
            "Das Support-Team",
            List.of(
                Client.EnvKey.ROLE_SUPPORT.getAsRole()
            )
    ),
    SUPPORT_LEITUNG(
            "\uD83D\uDD29 Supporter*innen-Leitung",
            "Die Support-Leitung und Stellvertretung",
            List.of(
                    Client.EnvKey.ROLE_SUPPORTLEITUNG.getAsRole(),
                    Client.EnvKey.ROLE_STVSUPPORTLEITUNG.getAsRole()
            )
    ),
    MODERATION_TEAM(
            "⚒\uFE0F Moderator*innen",
            "Das Moderations-Team",
            List.of(
                    Client.EnvKey.ROLE_MODERATOR.getAsRole()
            )
    ),
    MODERATION_LEITUNG(
            "\uD83D\uDEE0\uFE0F Moderator*innen-Leitung",
            "Die Moderations-Leitung und Stellvertretung",
            List.of(
                    Client.EnvKey.ROLE_MODLEITUNG.getAsRole(),
                    Client.EnvKey.ROLE_STVMODLEITUNG.getAsRole()
            )
    ),
    EVENT_TEAM(
            "\uD83C\uDF8A Event-Team",
            "Das Event-Team",
            List.of(
                    Client.EnvKey.ROLE_EVENT.getAsRole()
            )
    ),
    EVENT_LEITUNG(
            "\uD83C\uDF89 Event-Leitung",
            "Die Event-Leitung und Stellvertretung",
            List.of(
                    Client.EnvKey.ROLE_EVENTLEITUNG.getAsRole(),
                    Client.EnvKey.ROLE_STVEVENTLEITUNG.getAsRole()
            )
    ),
    DEVELOPMENT_TEAM(
            "💻 Development-Team",
            "Das Entwickluns-team",
            List.of(
                    Client.EnvKey.ROLE_DEVELOPER.getAsRole()
            )
    ),
    AWARENESS_TEAM(
            "\uD83D\uDC9A Awareness-Team",
            "Das Awareness-Team",
            List.of(
                    Client.EnvKey.ROLE_AWARENESS.getAsRole()
            )
    ),
    AWARENESS_LEITUNG(
            "\uD83C\uDF3F Awareness-Leitung",
            "Die Awareness-Leitung und Stellvertretung",
            List.of(
                    Client.EnvKey.ROLE_AWARENESSLEITUNG.getAsRole(),
                    Client.EnvKey.ROLE_STVAWARENESSLEITUNG.getAsRole()
            )
    ),
    ORGANISATION(
            "\uD83E\uDE9B Organisation",
            "Leitungen & Stellvertretungen aller Fachbereiche",
            List.of(
                    Client.EnvKey.ROLE_ORGA.getAsRole()
            )
    ),
    SERVER_LEITUNG(
            "⚙\uFE0F Server-Leitung",
            "Die Serverleitung (Leitungs-ticket)",
            List.of(
            Client.EnvKey.ROLE_SERVERLEITUNG.getAsRole()
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

    /**
     * Get all teams matching the given roles
     * <p>
     *     It filters the teams by role-count, to prevent smaller teams to be mistaken for a selected team,
     *     even if a bigger team was selected with the same (and more) roles than the smaller team.
     * </p>
     * @param roles
     * @return
     */
    public static List<TicketTeam> getTeamsByRoles(List<Role> roles) {
        HashSet<Role> roleSet = roles.stream().collect(HashSet::new, HashSet::add, HashSet::addAll);

        Comparator<TicketTeam> teamRoleSizeComperator = new Comparator<TicketTeam>()
        {
            @Override
            public int compare(TicketTeam o1, TicketTeam o2) {
                return Integer.compare(o1.getRoles().size(), o2.getRoles().size());
            }
        };

        List<TicketTeam> matchingTeams = new ArrayList<>();

        Arrays.stream(TicketTeam.values())
                .sorted(teamRoleSizeComperator.reversed())
                .forEachOrdered(team -> {
                    if (roleSet.containsAll(team.getRoles())) {
                        matchingTeams.add(team);
                        team.getRoles().forEach(roleSet::remove);
                    }
                });

        return matchingTeams;
    }
}
