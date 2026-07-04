package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActiveModKillCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "kill";
    }

    @Override
    public String getDescription() {
        return "Entferne alle aktive Moderator*innen und schliesse den Server";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_MODERATOR
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        if (!Closure.isMod(event.getMember())) {
            throw new PermissionDeniedException("Nur Moderator*innen können diesen Befehl ausführen.");
        }
        Role activeMod = EnvResolver.getRoleById(1513639704870912130L);

        List<CompletableFuture<Void>> removeRoleFutures = event.getGuild().getMembersWithRoles(activeMod).stream().map(member -> {
            return event.getGuild().removeRoleFromMember(member, activeMod).submit();
        }).toList();
        CompletableFuture<Void> allRoleFutures = CompletableFuture.allOf(removeRoleFutures.toArray(new CompletableFuture[0]));

        allRoleFutures.thenAccept(_ -> {
            event.reply("✅ Alle aktiven Moderator*innen wurden entfernt und der Server wird nun geschlossen.").setEphemeral(true).queue();
        }).exceptionally(_ -> {
            throw new InvalidStateException("Fehler beim Entfernen der aktiven Moderator*innen.");
        });
    }
}
