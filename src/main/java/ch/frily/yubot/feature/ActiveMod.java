package ch.frily.yubot.feature;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.reflections.Reflections.log;

public record ActiveMod(
        Member member,
        LocalDateTime lastActivityAt,
        @Nullable LocalDateTime activityRequestedAt,
        @Nullable Long activityRequestMessageId,
        @Nullable Long requestedAttentionMessageId) {

    public static CompletableFuture<String> registerModerator(Member member) throws SQLException, ClassNotFoundException {
        log.info("ActiveMod registerModerator onlinestatus for {}: {}", member.getEffectiveName(), member.getOnlineStatus());
        if (member.getOnlineStatus() != OnlineStatus.ONLINE && member.getOnlineStatus() != OnlineStatus.UNKNOWN) {
            return CompletableFuture.failedFuture(
                    new InvalidStateException("Du musst als <:status_online:1543868572609159239> Online, <:status_idle:1543868571443265557> Idle oder <:status_dnd:1543868569513623683> Do not disturb markiert sein, um deine Aktivität zu bestätigen.", "Wenn du <:statusoffline:1543871842186567750> offline bist, sehen dich die Leute nicht.")
            );
        }

        Role activeMod = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        if (member.getRoles().contains(activeMod)) {
            ActiveModRepository.updateModeratorActivity(member);
            return CompletableFuture.completedFuture("✅ Du hast deine Aktivität erfolgreich bestätigt.");
        }
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        return guild.addRoleToMember(member, activeMod).submit().thenApply(_ -> {

            try {
                Closure.deleteRequestedAttentionMessages();
                int activeModCount = Closure.getActiveMods().size();
                String countInfo = "Es sind nun **" + activeModCount + "** aktive Moderator\\*innen.";
                if (activeModCount == 1) {
                    countInfo = "Du moderierst den server momentan alleine.";
                }

                return "✅ Du wurdest als aktive\\*r moderator\\*in markiert.\n-# " + countInfo;

            } catch (Exception e) {
                return ExceptionHandler.fail(e);
            }
        });
    }
}
