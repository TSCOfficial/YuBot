package ch.frily.yubot.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The SessionStorage allows the system to save some data for a limited time.
 * <p>
 *     Each storage record has a TTL (Time to live) that defines its lifetime. At latest when the Bot stops, all data are lost.
 * </p>
 */
@Slf4j
public class SessionStorage {

    private static SessionStorage instance;

    @Getter
    private List<StorageData<?>> sessionStorage = new ArrayList<>();

    public static SessionStorage getInstance() {
        if (instance == null) {
            instance = new SessionStorage();
        }
        return instance;
    }

    public List<StorageData<?>> getSessionStorage() {
        return sessionStorage;
    }

    public <T> T getValue(String key, Member member, Class<T> type) {
        return sessionStorage.stream()
                .filter(data -> data.key().equals(key) && (member == null || data.member().getId().equals(member.getId())))
                .filter(data -> data.ttl().isAfter(LocalDateTime.now()))
                .max(Comparator.comparing(StorageData::ttl)) // takes the biggest ttl (most recent)
                .map(StorageData::storage)
                .filter(storage -> type.isInstance(storage.getValue()))
                .map(storage -> type.cast(storage.getValue()))
                .orElse(null);
    }

    public <T> void addStorage(String key, Member member, T value, int ttl) {
        sessionStorage.add(new StorageData<>(key, member, new Storage<>(value), resolveTtl(ttl)));
    }

    public void removeStorage(String key, Member member) {
        sessionStorage.removeIf(storage -> storage.key().equals(key) && (member == null || storage.member().getId().equals(member.getId())));
    }

    /**
     * Get the "time to live" [in Minutes] for that storage element
     * @param ttl Time to live [in Minutes]
     * @return the current time additioned with the given ttl
     */
    private LocalDateTime resolveTtl(int ttl){
        return LocalDateTime.now().plusMinutes(ttl);
    }

    /**
     * Remove expired storage elements
     */
    public void clean(){
        sessionStorage.removeIf(storage -> storage.ttl().isBefore(LocalDateTime.now()));
    }

}
