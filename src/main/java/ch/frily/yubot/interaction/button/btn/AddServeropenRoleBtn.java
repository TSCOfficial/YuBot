package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

public class AddServeropenRoleBtn implements IButton {
    @Override
    public String getId() {
        return "add-serveropen-role-btn";
    }

    @Override
    public String getLabel() {
        return "Werde Benachrichtigt!";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("\uD83D\uDCF2");
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        Role pingRole = EnvResolver.getRoleById(EnvKey.ROLE_EROEFFNUNGSPING);
        event.getGuild().addRoleToMember(event.getMember(), pingRole).queue();
        event.reply(String.format("""
                        Du wirst nun Benachrichtigt, sobald der Server öffnet!
                        -# Auf wunsch kannst du die %s Rolle im <id:customize> wieder abwählen.
                        """
                , pingRole.getAsMention()
        )).setEphemeral(true).setAllowedMentions(List.of()).queue();
    }
}
