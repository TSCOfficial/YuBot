package ch.frily.yubot.service.profile.ctxmenu;

import ch.frily.yubot.database.repository.ProfileMessageRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.service.profile.ProfileMessage;
import ch.frily.yubot.util.EnvKey;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Look up whom a profile belongs to
 */
@Slf4j
public class LookupProfileCtxMenu implements IMessageContextMenu {
    @Override
    public String getName() {
        return "Profile Lookup";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_YUTEAM
        ).map(EnvKey::getAsRole).toList();
    }

    @Override
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException {
        String profileName = event.getTarget().getAuthor().getName();
        Message message = event.getTarget();

        if (!message.isWebhookMessage()) {
            throw new InvalidStateException("Keine Profil-Nachricht", "Das Lookup funktioniert nur für Profil-Nachrichten, welche via Webhook gesendet wurden.");
        }

        ProfileMessage webhookMessage = ProfileMessageRepository.get(message);
        Profile profile = webhookMessage.profile();
        String replyMessage = "";

        if (profile != null) {
            replyMessage = String.format("Das Profil **%s** gehört dem Konto %s (%s).", profile.name(), profile.parentAccount().getAsMention(), profile.parentAccount().getEffectiveName());

            if (!profile.name().equals(profileName)) {
                // show extended information if the name doesn't match the webhook-name anymore
                replyMessage = String.format("""
                Das Profil **%s** gehört dem Konto %s (%s).
                -# Dieses profil heisst mittlerweile **%s**.
                """, profileName, profile.parentAccount().getAsMention(), profile.parentAccount().getEffectiveName(),
                        profile.name());
            }
        } else {
            // show extended information if the profile doesnt exist anymore
            replyMessage = String.format("""
                Diese Nachricht wurde von %s (%s) gesendet.
                -# Dieses Profil wurde mittlerweile gelöscht.
                """, webhookMessage.member().getAsMention(), webhookMessage.member().getEffectiveName());
        }

        event.reply(replyMessage).setEphemeral(true).setAllowedMentions(List.of()).queue();
    }
}
