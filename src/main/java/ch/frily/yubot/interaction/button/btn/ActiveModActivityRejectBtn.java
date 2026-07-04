package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.ActiveModRepository;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Reject the activity request and opt-out
 */
public class ActiveModActivityRejectBtn implements IButton {
    @Override
    public String getId() {
        return "activemod-activity-reject-btn";
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
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        ActiveMod forActiveMod = ActiveModRepository.getModeratorByActivityRequestMessageId(event.getMessageIdLong());
        if (event.getMember().equals(forActiveMod.member())) {
            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            guild.removeRoleFromMember(forActiveMod.member(), EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD)).queue();
            event.getMessage().delete().queue();
            event.reply("✅ Du wurdest erfolgreich als Active-mod entfernt.").setEphemeral(true).queue();
        } else {
            throw new PermissionDeniedException(String.format("Nur %s kann seine/ihre Anfrage ablehnen.", forActiveMod.member().getAsMention()));
        }
    }
}
