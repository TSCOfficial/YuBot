package ch.frily.yubot.interaction.contextmenu.ctxmenu;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
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
        log.info(profileName);
        Profile profile = ProfileRepository.getProfileByName(profileName);
        event.reply(String.format("Das Profil **%s** gehört dem Konto %s (%s)", profileName, profile.parentAccount().getAsMention(), profile.parentAccount().getEffectiveName())).setEphemeral(true).queue();
    }
}
