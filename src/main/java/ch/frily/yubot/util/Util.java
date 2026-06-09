package ch.frily.yubot.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Util {

    public static boolean isTeamMember(Member member) {
        Role teamRole = EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM);
        return member.getRoles().contains(teamRole);
    }
}
