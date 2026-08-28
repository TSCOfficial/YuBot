package ch.frily.yubot.util;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BannerResolver {

    private static final int DEFAULT_WIDTH = 940;
    private static final int DEFAULT_HEIGHT = 280;

    public static CompletableFuture<BufferedImage> resolveGlobalBanner(Member member) {
        return member.getUser().retrieveProfile().submit()
                .thenCompose(userProfile -> {
                    String globalBanner = userProfile.getBannerUrl();
                    if (globalBanner != null) {
                        return ImageFetcher.fetch(globalBanner + "?size=1024").thenApply(image -> scaleToFixedSizeCover(image, DEFAULT_WIDTH, DEFAULT_HEIGHT));
                    }
                    return CompletableFuture.completedFuture(generateColorBanner(userProfile));
                });
    }

    private static BufferedImage scaleToFixedSizeCover(BufferedImage source, int targetWidth, int targetHeight) {
        double scale = Math.max(
                (double) targetWidth / source.getWidth(),
                (double) targetHeight / source.getHeight()
        );

        int scaledWidth = (int) Math.ceil(source.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(source.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // zentrieren und beschneiden
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        g2d.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return scaled;
    }

    private static BufferedImage generateColorBanner(User.Profile profile) {
        java.awt.Color base = profile.getAccentColor() != null ? profile.getAccentColor() : new Color(88, 101, 242); // Discord-Blurple als Fallback

        BufferedImage banner = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = banner.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setPaint(base);
        g.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        g.dispose();
        return banner;
    }
}
