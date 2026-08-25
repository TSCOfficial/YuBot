package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.util.BannerResolver;
import ch.frily.yubot.util.ImageFetcher;
import ch.frily.yubot.util.ProfileImageComposer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public class TeamProfilContainer extends Container {

    @Getter
    private final Member member;

    @Getter
    private FileUpload profileBanner;

    public TeamProfilContainer(Member member) {
        this.member = member;
    }

    public CompletableFuture<TeamProfilContainer> buildAsync() {
        return this.buildProfileBanner()
                .thenApply(fileUpload -> {
                    this.profileBanner = fileUpload;
                    MediaGallery gallery = MediaGallery.of(
                            MediaGalleryItem.fromFile(fileUpload)
                    );
                    addComponent(gallery);
                    addFormatedText("# %s", member.getEffectiveName());
                    return this;
                })
                .exceptionally(ExceptionHandler::fail);
    }

    public CompletableFuture<FileUpload> buildProfileBanner() {
        CompletableFuture<BufferedImage> bannerFuture = BannerResolver.resolveGlobalBanner(member);
        CompletableFuture<BufferedImage> avatarFuture = ImageFetcher.fetch(member.getEffectiveAvatarUrl());

        return bannerFuture.thenCombine(avatarFuture, ProfileImageComposer::compose)
                .thenApply(composed -> {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(composed, "png", baos);
                        return FileUpload.fromData(baos.toByteArray(), "profile-banner.png");
                    } catch (IOException e) {
                        throw new CompletionException(new ImagingOpException("Failed to draw image"));
                    }
                });
    }
}
