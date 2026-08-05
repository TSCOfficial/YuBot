package ch.frily.yubot.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SessionStorage {

    private static SessionStorage instance;

    @Getter
    private List<StorageRecord<?>> sessionStorage = new ArrayList<>();

    public static SessionStorage getInstance() {
        if (instance == null) {
            instance = new SessionStorage();
            instance.addStorage("something-id18092720108", "Just a test ^^");
        }
        return instance;
    }

    public List<StorageRecord<?>> getSessionStorage() {
        return sessionStorage;
    }

    public Optional<StorageRecord<?>> getSessionStorage(String key) {
        return sessionStorage.stream()
                .filter(storage -> storage.key().equals(key))
                .findFirst();
    }

    public void addStorage(String key, String value){
        sessionStorage.add(new StorageRecord<>(key, new StringStorage(value), getTtl(1)));
    }

    public void addStorage(String key, boolean value){
        sessionStorage.add(new StorageRecord<>(key, new BooleanStorage(value), getTtl(1)));
    }

    public void addStorage(String key, Long value){
        sessionStorage.add(new StorageRecord<>(key, new LongStorage(value), getTtl(1)));
    }

    public void addStorage(StorageRecord<?> storageRecord){
        sessionStorage.add(storageRecord);
    }

    /**
     * Get the "time to live" [in Minutes] for that storage element
     * @param ttl Time to live [in Minutes]
     * @return the current time additioned with the given ttl
     */
    public LocalDateTime getTtl(int ttl){
        return LocalDateTime.now().plusMinutes(ttl);
    }

    /**
     * Remove expired storage elements
     */
    public void clean(){
        log.info("Cleaning expired storage elements");
        sessionStorage.removeIf(storage -> storage.ttl().isBefore(LocalDateTime.now()));
    }

}
