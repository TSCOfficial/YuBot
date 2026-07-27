package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ActiveModOptOutCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "opt-out";
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
        event.getGuild().removeRoleFromMember(event.getMember(), activeMod).submit().thenAccept(_ -> {
            int activeModCount = Closure.getActiveMods().size();

            String countInfo = "Es sind nun **" + activeModCount + "** aktive Moderator\\*innen";
            if (activeModCount == 1) {
                countInfo = "Es ist nun nurnoch **" + activeModCount + "** aktive\\*r Modderator\\*in";
            } else if (activeModCount == 0) {
                countInfo = "Es sind nun keine aktive Moderator*innen mehr da - der Server wird geschlossen.";
            }

            event.reply("Dein aktiver moderator\\*innen Status wurde entfernt.\n-# " + countInfo).setEphemeral(true).queue();
        });
    }
}
