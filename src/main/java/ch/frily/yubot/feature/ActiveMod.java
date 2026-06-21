package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

public record ActiveMod(Member member, LocalDateTime lastActivityAt, @Nullable LocalDateTime activityRequestedAt, @Nullable Long activityRequestMessageId) {

}
