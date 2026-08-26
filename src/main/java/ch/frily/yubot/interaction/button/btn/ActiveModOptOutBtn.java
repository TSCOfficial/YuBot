package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * When the last active mod wants to opt-out via command, the bot askes to approve the opt-out before closing the server
 */
public class ActiveModOptOutBtn extends Button {
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

        if (!event.getMember().getRoles().contains(activeMod)) {
            throw new InvalidStateException("Du bist nicht als aktiver moderator\\*in markiert.", null);
        }
        Closure.deleteRequestedAttentionMessages();
        event.getGuild().removeRoleFromMember(event.getMember(), activeMod).submit().thenAccept(ThrowingConsumer.wrap(null, _ -> {
            int activeModCount = Closure.getActiveMods().size();

            String countInfo = "Es sind nun noch **" + activeModCount + "** aktive Moderator\\*innen.";
            if (activeModCount == 1) {
                countInfo = "Es ist nun nur noch **" + activeModCount + "** aktive\\*r Moderator\\*in";
            }

            event.reply("✅ Du wurdest als aktive\\*r moderator\\*in entfernt.\n-# " + countInfo).setEphemeral(true).queue();
        }));
    }
}
