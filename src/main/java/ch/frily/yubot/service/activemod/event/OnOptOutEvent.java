package ch.frily.yubot.service.activemod.event;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.service.activemod.Closure;
import ch.frily.yubot.service.dynamicmsg.DynamicMessageList;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.events.Event;

import java.io.IOException;
import java.sql.SQLException;

public class OnOptOutEvent implements IEvent<GuildMemberRoleRemoveEvent> {

    private static OnOptOutEvent instance;

    public static OnOptOutEvent getInstance() {
        if (instance == null) {
            instance = new OnOptOutEvent();
        }
        return instance;
    }

    @Override
    public Class<? extends Event> getListenerType() {
        return null;
    }

    @Override
    public <T extends Event> void execute(T event) throws SQLException, IOException {
        GuildMemberRoleRemoveEvent castEvent = (GuildMemberRoleRemoveEvent) event;
        if (Util.isTeamMember(castEvent.getMember())) {
            if (castEvent.getRoles().contains(EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD))) {
                Closure.getInstance().triggerUpdate();
            }

            DynamicMessageList.TEAMLIST.update();
        }
    }
}
