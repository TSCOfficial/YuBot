package ch.frily.yubot.interaction.command.cmd.activemod;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.activemod.ActiveMod;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.interaction.modal.modal.SelectActiveModSendTypeModal;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ActiveModOptInCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "opt-in";
    }

    @Override
    public String getDescription() {
        return "Opt-in als aktive*r moderator*in";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_MODERATOR
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        if (ProfileRepository.getProfile(event.getMember()) == null || ProfileRepository.getProfile(event.getMember()).activeModSendInDm() == null) {
            // If the user does not have set the activeModSendInDm
            event.replyModal(new SelectActiveModSendTypeModal().build()).queue();
            return;
        }
        event.deferReply(true).queue();

        ActiveMod.registerModerator(event.getMember()).thenAccept(response -> {
            event.getHook().sendMessage(response).setEphemeral(true).queue();
        }).exceptionally(throwable -> {
            return ExceptionHandler.fail(throwable);
        });
    }
}
