package ch.frily.yubot.service.forummanager.slashcommand;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.forummanager.modal.CreatePostModal;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Slashcommand to create a new forum post
 * @since v2.5.0
 */
public class ForumCreatePostCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "post";
    }

    @Override
    public String getDescription() {
        return "Erstelle ein neuen Post in einem Forum";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.CHANNEL, "forum", "In welchem Forum der Post erstellt werden soll.", true).setChannelTypes(ChannelType.FORUM)

        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return List.of(
                EnvKey.ROLE_SERVERLEITUNG.getAsRole()
        );
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Objects.requireNonNull(event.getOption("forum"));
        ForumChannel forumChannel = event.getOption("forum").getAsChannel().asForumChannel();

        CreatePostModal modal = new CreatePostModal();
        modal.setForumChannel(forumChannel);
        modal.addArgument("forumId", forumChannel.getId());

        event.replyModal(modal.build()).queue();
    }
}
