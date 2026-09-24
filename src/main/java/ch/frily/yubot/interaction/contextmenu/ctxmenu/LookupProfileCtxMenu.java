package ch.frily.yubot.interaction.contextmenu.ctxmenu;

import ch.frily.yubot.database.repository.ProfileHistoryRepository;
import ch.frily.yubot.database.repository.ProfileMessageRepository;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.profile.ProfileHistory;
import ch.frily.yubot.feature.profile.ProfileMessage;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

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
        return List.of(
                EnvKey.ROLE_YUTEAM
        ).stream().map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException, ClassNotFoundException {
        String profileName = event.getTarget().getAuthor().getName();
        Message message = event.getTarget();

        if (!message.isWebhookMessage()) {
            throw new InvalidStateException("Keine Profil-Nachricht", "Das Lookup funktioniert nur für Profil-Nachrichten, welche via Webhook gesendet wurden.");
        }

        Profile profile = ProfileMessageRepository.get(message).profile();
        String replyMessage = getReplyMessage(profile, profileName);

        event.reply(replyMessage).setEphemeral(true).setAllowedMentions(List.of()).queue();
    }

    private static @NonNull String getReplyMessage(Profile profile, String profileName) {
        String replyMessage = String.format("Das Profil **%s** gehört dem Konto %s (%s).", profile.name(), profile.parentAccount().getAsMention(), profile.parentAccount().getEffectiveName());

        if (!profile.name().equals(profileName)) {
            // show extended information if the name doesn't match the webhook-name anymore
            replyMessage = String.format("""
                Das Profil **%s** gehört dem Konto %s (%s).
                -# Dieses profil heisst mittlerweile **%s**.
                """, profileName, profile.parentAccount().getAsMention(), profile.parentAccount().getEffectiveName(),
                    profile.name());
        }
        return replyMessage;
    }
}
