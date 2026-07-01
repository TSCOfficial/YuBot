package ch.frily.yubot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Util {

    /**
     * Check if a member has the {@link EnvKey#ROLE_YUTEAM} role
     * @param member
     * @return True if they have the role, false if not
     */
    public static boolean isTeamMember(Member member) {
        Role teamRole = EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM);
        return member.getRoles().contains(teamRole);
    }

    public static boolean isActiveMod(Member member) {
        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        return member.getRoles().contains(activeModRole);
    }

    /**
     * Check if a member has the {@link Permission#ADMINISTRATOR} permission
     * @param member The member to check
     * @return True if they are administrator, false if not
     */
    public static boolean isAdministrator(Member member) {
        List<Role> adminRoles = member.getRoles().stream().filter(role -> {
            return role.hasPermission(Permission.ADMINISTRATOR);
        }).toList();
        return !adminRoles.isEmpty();
    }

    public static String format(String template, Object... args) {
        int i = 0;
        while (template.contains("{}") && i < args.length) {
            template = template.replaceFirst("\\{\\}", String.valueOf(args[i++]));
        }
        return template;
    }

    public static String calcDuration(Temporal startInclusive, Temporal endExclusive){
        Duration duration = Duration.between(startInclusive, endExclusive);

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        String openDuration;
        if (days > 0) {
            openDuration = String.format("%dd %dh %dmin", days, hours, minutes);
        } else if (hours > 0) {
            openDuration = String.format("%dh %dmin", hours, minutes);
        } else {
            openDuration = String.format("%dmin", minutes);
        }

        return openDuration;
    }

    /**
     * Escapes characters in a text that are interpreted as Markdown on Discord.
     * @param input The string to escape
     * @return The escaped string
     */
    public static String escapeMarkdown(String input) {
        if (input == null) return null;

        return input
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace("|", "\\|");
    }

}
