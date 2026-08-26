package ch.frily.yubot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Find members that have a given role<br>
     * @param role
     * @return true | false : Returns true when the list is completed
     */
    public static List<Member> getUsersByRole(Role role) {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        if (guild == null || role == null) {
            return List.of();
        }
        return guild.getMembersWithRoles(role);
    }

    public static Member getMemberByUser(User user) {
        return getMemberByUser(user.getIdLong());
    }

    public static Member getMemberByUser(long userId) {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        return guild.getMemberById(userId);
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

    public static String calcDuration(Temporal startInclusive, Temporal end, boolean includeEnd) {
        if (includeEnd) {
            end = end.plus(1, ChronoUnit.MINUTES);
        }
        return calcDuration(startInclusive, end);
    }

    public static String calcDuration(int minutes) {
        boolean isNegative = minutes < 0;
        if (isNegative) {
            minutes = minutes * -1;
        }
        long days = minutes / 1440;
        long hours = (minutes % 1440) / 60;
        long minutesLeft = minutes % 60;

        String openDuration;
        if (days > 0) {
            openDuration = String.format("%dd %dh %dmin", days, hours, minutesLeft);
        } else if (hours > 0) {
            openDuration = String.format("%dh %dmin", hours, minutesLeft);
        } else {
            openDuration = String.format("%dmin", minutesLeft);
        }

        if (isNegative) {
            openDuration = "-" + openDuration;
        }

        return openDuration;
    }

    public static long toEpochSeconds(LocalDateTime dateTime) {
        return dateTime.atZone(EnvResolver.getZoneId()).toEpochSecond();
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

    public static double calcPercentage(int value1, int value2) {
        if (value2 == 0)
            return 0;
        return (double) value1 / (double) value2 * 100;
    }

    public static String translateMonth(Month month) {
        return switch (month) {
            case JANUARY -> "Januar";
            case FEBRUARY -> "Februar";
            case MARCH -> "März";
            case APRIL -> "April";
            case MAY -> "Mai";
            case JUNE -> "Juni";
            case JULY -> "Juli";
            case AUGUST -> "August";
            case SEPTEMBER -> "September";
            case OCTOBER -> "Oktober";
            case NOVEMBER -> "November";
            case DECEMBER -> "Dezember";
        };
    }

    public static Category resolveCategory(GuildChannel channel) {
        if (channel instanceof ThreadChannel thread) {
            GuildChannel threadParent = resolveThreadParent(thread);
            return resolveCategory(threadParent);
        } else if (channel instanceof ICategorizableChannel categorizable) {
            return categorizable.getParentCategory();
        }
        return null;
    }

    public static GuildChannel resolveThreadParent(ThreadChannel thread) {
        return thread.getParentChannel();
    }
}
