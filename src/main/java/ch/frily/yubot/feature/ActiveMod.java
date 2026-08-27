package ch.frily.yubot.feature;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public record ActiveMod(
        Member member,
        LocalDateTime lastActivityAt,
        @Nullable LocalDateTime activityRequestedAt,
        @Nullable Long activityRequestMessageId,
        @Nullable Long requestedAttentionMessageId) {

    public static CompletableFuture<String> registerModerator(Member member) throws SQLException, ClassNotFoundException {
        Role activeMod = EnvResolver.getRoleById(1513639704870912130L);

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
