package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClosureOptInCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "opt-in";
    }

    @Override
    public String getDescription() {
        return "Opt-in als aktive*r moderator*in";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        try {
            Role activeMod = EnvResolver.getRoleById(1513639704870912130L);
            event.getGuild().addRoleToMember(event.getMember(), activeMod).queue();
            event.reply("Du wurdest als aktive\\*r moderator\\*in markiert.").queue();
        } catch (Exception e) {
            event.reply(e.getMessage()).queue();
        }
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
