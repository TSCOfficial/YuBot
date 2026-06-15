package ch.frily.yubot.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.Collection;

public class Util {

    public static boolean isTeamMember(Member member) {
        Role teamRole = EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM);
        return member.getRoles().contains(teamRole);
    }

    public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
        for (T element : elements) {
            if (collection.contains(element)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <T> boolean containsAny(Collection<T> collection, T... elements) {
        return containsAny(collection, Arrays.stream(elements).toList());
    }

    public static String format(String template, Object... args) {
        int i = 0;
        while (template.contains("{}") && i < args.length) {
            template = template.replaceFirst("\\{\\}", String.valueOf(args[i++]));
        }
        return template;
    }
}
