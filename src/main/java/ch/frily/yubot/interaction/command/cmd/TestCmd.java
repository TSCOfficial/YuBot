package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "testing";
    }

    @Override
    public String getDescription() {
        return "Für das testen von Funktionen oder Teilfunktionen.";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Role everyoneRole = EnvResolver.getRoleById(EnvKey.ROLE_EVERYONE);
        TextChannel serverClosedChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN);

        List<Permission> allowPerms = new ArrayList<>();
        List<Permission> denyPerms = new ArrayList<>();
        denyPerms.add(Permission.MESSAGE_SEND);

        if (false) {
            denyPerms.add(Permission.VIEW_CHANNEL);
        } else {
            allowPerms.add(Permission.VIEW_CHANNEL);
        }

        serverClosedChannel.getManager().putRolePermissionOverride(everyoneRole.getIdLong(), allowPerms, denyPerms).queue();
        event.reply("executed").setEphemeral(true).queue();
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }
}
