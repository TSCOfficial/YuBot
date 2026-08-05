package ch.frily.yubot.storage;

import net.dv8tion.jda.api.entities.Member;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record StorageData<T>(String key, @Nullable Member member, Storage<T> storage, LocalDateTime ttl) {
}
