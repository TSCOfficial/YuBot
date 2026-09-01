package ch.frily.yubot.interaction.command.cmd.activemod;

import ch.frily.yubot.feature.activemod.Closure;
import ch.frily.yubot.interaction.button.btn.activemod.ActiveModApproveOptOutBtn;
import ch.frily.yubot.interaction.button.btn.activemod.ActiveModCancelOptOutBtn;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
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
        int activeModCount = Closure.getActiveMods().size() - 1;
        if (activeModCount == 0) {
            event.reply("**Bestätige dein Opt-out**\nDu bist der/die letzte aktive Moderator*in. Bitte bestätige dein Opt-out.\n-# Falls du bestätigst wird der Server sofort geschlossen.")
                    .addComponents(ActionRow.of(new ActiveModApproveOptOutBtn().build(), new ActiveModCancelOptOutBtn().build())).setEphemeral(true).queue();
            return;
        }
        event.getGuild().removeRoleFromMember(event.getMember(), activeMod).submit().thenAccept(_ -> {
            String countInfo = "Es sind nun **" + activeModCount + "** aktive Moderator\\*innen";
            if (activeModCount == 1) {
                countInfo = "Es ist nun nurnoch **" + activeModCount + "** aktive\\*r Modderator\\*in";
            }

            event.reply("Opt-out erfolgreich." + countInfo).setEphemeral(true).queue();
        });
    }
}
