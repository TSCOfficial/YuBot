package ch.frily.yubot.database;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Message.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Manages the image Server
 */
public class ImageServer {

    /**
     * Persist an {@link Attachment} on the image-server
     * <p>
     *     To be able to keep track and control the uploaded image, the returned path needs to be saved in the database
     * </p>
     * @param attachment
     * @return
     */
    protected static CompletableFuture<String> downloadAndPersist(Attachment attachment) {
        UUID uuid = UUID.randomUUID();
        Path target = Path.of(EnvResolver.getString(EnvKey.IMAGESERVER_PATH) + uuid + ".png");
        return attachment.getProxy().download()
                .thenApplyAsync(in -> {
                    try (in) {
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                    return "https://img.einfachyu.de/" + uuid + ".png";
                });
    }
}
