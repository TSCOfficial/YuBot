package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.ProfilContainer;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
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
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Der User, dessen Profil geöffnet werden soll")
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SERVERLEITUNG
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Member member = event.getMember();
        if (event.getOption("user") != null) {
            member = event.getOption("user").getAsMember();
        }
        log.info("Opening profile for {}", member.getEffectiveName());
        ProfilContainer container = new ProfilContainer(member);

        container.buildAsync().thenAccept(builtContainer -> {
            MessageCreateData message = new MessageCreateBuilder()
                    .useComponentsV2()
                    .setComponents(builtContainer.build())
                    .addFiles(builtContainer.getProfileBanner())
                    .build();
            event.reply(message).setEphemeral(true).queue();
        });
    }
}
