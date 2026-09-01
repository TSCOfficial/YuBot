package ch.frily.yubot.interaction.command.cmd.profile;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

@Slf4j
public class ProfileShowCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Öffne dein Profil";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        Member member = event.getMember();
        ProfilContainer container = new ProfilContainer(member);

        container.buildAsync().thenAccept(builtContainer -> {
            MessageCreateData message = new MessageCreateBuilder()
                    .useComponentsV2()
                    .setComponents(builtContainer.build())
                    .addFiles(builtContainer.getProfileBanner())
                    .build();
            event.getHook().sendMessage(message).queue();
        });
    }
}
