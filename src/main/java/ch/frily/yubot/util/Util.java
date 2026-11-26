package ch.frily.yubot.util;

import ch.frily.yubot.EnvKey;
import de.uni_jena.cs.fusion.similarity.jarowinkler.JaroWinklerSimilarity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jspecify.annotations.NonNull;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class Util {

    public static final double FUZZY_SEARCH_DEFAULT_THRESHOLD = 0.6;

    /**
     * Search for similar Strings using a defined threshold
     * <p>
     *     Default threshold is <b>0.6</b> (60% similarity).<br>
     *     Greater threshold means more similar. Lower threshold means less similar.<br>
     * </p>
     * @param key the key term to search for
     * @param terms the terms to find the similarity in
     * @param threshold the threshold to use
     * @return a list of similar strings that are at least x% similar to the given key term (where x is the given threshold in percent)
     */
    public static @NonNull List<String> fuzzySearch(String key, List<String> terms, double threshold) {
        if (threshold == 0) {
            threshold = FUZZY_SEARCH_DEFAULT_THRESHOLD;
        }
        terms = terms.stream().map(MessageUtil::normalize).toList();
        var jw = JaroWinklerSimilarity.with(terms, threshold);
        key = MessageUtil.normalize(key);

        Map<String, Double> similar = jw.apply(key);

        double finalThreshold = threshold;
        return similar.entrySet().stream()
                .filter(entry -> entry.getValue() > finalThreshold)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Search for similar Strings using a default threshold (60% similar)
     * @param key the key term to search for
     * @param terms the terms to find the similarity in
     * @return a list of similar strings that are at least 60% similar to the given key term
     */
    public static @NonNull List<String> fuzzySearch(String key, List<String> terms) {
        return fuzzySearch(key, terms, FUZZY_SEARCH_DEFAULT_THRESHOLD);
    }

    /**
     * Check if a member is a team-member
     * <p>
     *     This checks if they have the team-role or is Administrator
     * </p>
     * @param member the member to check
     * @return True if they have the role, false if not
     */
    public static boolean isTeamMember(Member member) {
        if (isAdministrator(member)) return true;
        Objects.requireNonNull(member);
        Role teamRole = EnvKey.ROLE_YUTEAM.getAsRole();
        return member.getRoles().contains(teamRole);
    }

    public static boolean isActiveMod(Member member) {
        Objects.requireNonNull(member);
        Role activeModRole = EnvKey.ROLE_ACTIVEMOD.getAsRole();
        return member.getRoles().contains(activeModRole);
    }

    /**
     * Find members that have a given role<br>
     * @param role the role to get the members from
     * @return true | false : Returns true when the list is completed
     */
    public static List<Member> getUsersByRole(Role role) {
        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
        if (guild == null || role == null) {
            return List.of();
        }
        return guild.getMembersWithRoles(role);
    }

    public static Member getMemberByUser(User user) {
        return getMemberByUser(user.getIdLong());
    }

    public static Member getMemberByUser(long userId) {
        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
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

    /**
     * Check if a member has the required roles or is an administrator
     * @param member The member to check
     * @param roles The roles to check for
     * @return True if they have the required roles or if they are an administrator, false if not
     */
    public static boolean isPermitted(Member member, List<Role> roles) {
        return member.getRoles().stream().anyMatch(roles::contains) || isAdministrator(member);
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

    public static String calcDurationSeconds(int seconds) {
        if (seconds < 60) {
            return String.format("%ds", seconds);
        } else {
            return calcDuration(seconds / 60);
        }
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
        return dateTime.atZone(EnvKey.TIMEZONE.getAsZoneId()).toEpochSecond();
    }



    public static double calcPercentage(int value1, int value2) {
        if (value2 == 0)
            return 0;
        return (double) value1 / (double) value2 * 100;
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
