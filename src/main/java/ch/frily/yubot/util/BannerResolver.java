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
                        return ImageFetcher.fetch(globalBanner);
                    }
                    return CompletableFuture.completedFuture(generateColorBanner(userProfile));
                });
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
