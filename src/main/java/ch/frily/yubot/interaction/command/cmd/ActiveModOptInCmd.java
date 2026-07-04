package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

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
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        Role activeMod = EnvResolver.getRoleById(1513639704870912130L);

        if (event.getMember().getRoles().contains(activeMod)) {
            throw new InvalidStateException("Du bist bereits als aktiver moderator\\*in markiert.", null);
        }
        event.getGuild().addRoleToMember(event.getMember(), activeMod).submit().thenAccept(_ -> {
            int activeModCount = Closure.getActiveMods().size();

            String countInfo = "Es sind nun **" + activeModCount + "** aktive Moderator\\*innen.";
            if (activeModCount == 1) {
                countInfo = "Es ist nun nurnoch **" + activeModCount + "** aktive\\*r Moderator\\*in";
            }

            event.reply("✅ Du wurdest als aktive\\*r moderator\\*in markiert.\n-# " + countInfo).setEphemeral(true).queue();
        });
    }
}
