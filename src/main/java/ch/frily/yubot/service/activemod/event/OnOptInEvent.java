package ch.frily.yubot.service.activemod.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.activemod.Closure;
import ch.frily.yubot.service.dynamicmsg.DynamicMessageList;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;

import java.io.IOException;
import java.sql.SQLException;

public class OnOptInEvent implements IEvent<GuildMemberRoleAddEvent> {

    private static OnOptInEvent instance;

    public static OnOptInEvent getInstance() {
        if (instance == null) {
            instance = new OnOptInEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return GuildMemberRoleAddEvent.class;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        GuildMemberRoleAddEvent castEvent = (GuildMemberRoleAddEvent) event;
        if (Util.isTeamMember(castEvent.getMember())) {

            if (castEvent.getRoles().contains(EnvKey.ROLE_ACTIVEMOD.getAsRole())) {
                Closure.getInstance().triggerUpdate();
            }

            DynamicMessageList.TEAMLIST.update();
        }
    }
}
