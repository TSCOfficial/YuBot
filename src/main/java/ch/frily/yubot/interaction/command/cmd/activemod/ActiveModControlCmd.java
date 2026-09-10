package ch.frily.yubot.interaction.command.cmd.activemod;

import ch.frily.yubot.database.repository.ActiveModControlRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ActiveModControlCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "control";
    }

    @Override
    public String getDescription() {
        return "Controlliere ob moderator*innen sich opt-in stellen können oder nicht.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "opt-in", "Kontrolliere den status des Opt-ins", true)
                        .addChoices(
                                new Command.Choice("erlauben", "allow"),
                                new Command.Choice("blockieren", "deny")
                        )
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SERVERLEITUNG
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        boolean state = event.getOption("opt-in").getAsString().equals("allow");
        ActiveModControlRepository.updateControl(state);
        TextChannel modIntern = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MODINTERN);
        if (state) {
            modIntern.sendMessage("🟢 **Opt-in-Funktion retabliert**\n-# Die Opt-in-Funktion ist wieder verfügbar und kann wieder wie gewohnt verwendet werden.").queue();
            event.reply("Die Opt-in Funktion wurde aktiviert und kann von den Moderator*innen wieder verwendet werden.").setEphemeral(true).queue();
        } else {
            modIntern.sendMessage("⚠️ **Opt-in-Funktion deaktiviert**\n-# Die Opt-in-Funktion wurde temporär deaktiviert und kann bis zur Reaktivierung nicht mehr verwendet werden.").queue();
            event.reply("Die Opt-in Funktion wurde deaktiviert.").setEphemeral(true).queue();
        }
    }
}
