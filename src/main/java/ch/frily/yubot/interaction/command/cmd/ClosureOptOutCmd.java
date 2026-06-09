package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClosureOptOutCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "opt-out";
    }

    @Override
    public String getDescription() {
        return "Opt-in als aktive*r moderator*in";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        try {
            Role activeMod = EnvResolver.getRoleById(1513639704870912130L);
            event.getGuild().removeRoleFromMember(event.getMember(), activeMod).submit().thenAccept(_ -> {
                int activeModCount = Closure.getActiveMods().size();

                String countInfo = "Es sind nun **" + activeModCount + "** aktive Moderator\\*innen";
                if (activeModCount == 1) {
                    countInfo = "Es ist nun nurnoch **" + activeModCount + "** aktive*r Modderator*in";
                } else if (activeModCount == 0) {
                    countInfo = "Es sind nun keine aktive Moderator*innen mehr da - der Server wird geschlossen.";
                }

                event.reply("Dein aktiver moderator\\*innen Status wurde entfernt.\n-# " + countInfo).setEphemeral(true).queue();
            });


        } catch (Exception e) {
            event.reply(e.getMessage()).queue();
        }
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
