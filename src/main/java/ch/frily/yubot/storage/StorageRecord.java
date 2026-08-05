package ch.frily.yubot.storage;

import java.time.LocalDateTime;

public record StorageRecord<V>(String key, Storage<V> storage, LocalDateTime ttl) {
}
