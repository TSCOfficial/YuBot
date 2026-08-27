package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * When the last active mod wants to opt-out via command, the bot askes to approve the opt-out before closing the server
 */
@Slf4j
public class ActiveModOptOutBtn extends Button {

    public void setDeleteMsgOnOptOut(boolean deleteMsgOnOptOut) {
        addArgument("delete", deleteMsgOnOptOut);
    }

    @Override
    public String getId() {
        return "activemod-optout";
    }

    @Override
    public String getLabel() {
        return "Opt-out";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        Role activeMod = EnvResolver.getRoleById(1513639704870912130L);
        Member member = event.getMember() == null ? Util.getMemberByUser(event.getUser()) : event.getMember();
        if (!member.getRoles().contains(activeMod)) {
            throw new InvalidStateException("Du bist nicht als aktiver moderator\\*in markiert.", null);
        }

        Closure.deleteRequestedAttentionMessages();
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        guild.removeRoleFromMember(member, activeMod).submit().thenAccept(ThrowingConsumer.wrap(null, _ -> {
            int activeModCount = Closure.getActiveMods().size();

            String countInfo = "Es sind nun noch **" + activeModCount + "** aktive Moderator\\*innen.";
            if (activeModCount == 1) {
                countInfo = "Es ist nun nur noch **" + activeModCount + "** aktive\\*r Moderator\\*in";
            }

            event.reply("✅ Du wurdest als aktive\\*r moderator\\*in entfernt.\n-# " + countInfo).setEphemeral(true).queue();
            if (hasArgument(event.getComponentId(),"delete") && getArgument(event.getComponentId(),"delete").equals("true")) {
                event.getMessage().delete().queue();
            }
        }));
    }
}
