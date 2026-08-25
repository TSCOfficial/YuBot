package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.container.TeamProfilContainer;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ProfileCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getDescription() {
        return "Öffne dein Profil";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        TeamProfilContainer container = new TeamProfilContainer(event.getMember());

        container.buildAsync().thenAccept(builtContainer -> {
            MessageCreateData message = new MessageCreateBuilder()
                    .useComponentsV2()
                    .setComponents(builtContainer.build())
                    .addFiles(builtContainer.getProfileBanner())
                .build();
            event.reply(message).setEphemeral(true).queue();
        });

    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SERVERLEITUNG
        ).map(EnvResolver::getRoleById).toList();
    }
}
