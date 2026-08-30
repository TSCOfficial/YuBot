package ch.frily.yubot.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public class ImageFetcher {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static CompletableFuture<BufferedImage> fetch(String url) {
        log.info("Fetching image from {}", url);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    try {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.body()));
                        return image;
                    } catch (IOException e) {
                        throw new CompletionException(new ImagingOpException("Konnte Bild nicht laden: " + url));
                    }
                });
    }
}
