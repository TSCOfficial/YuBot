package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Profile;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.feature.Setting;
import ch.frily.yubot.util.BannerResolver;
import ch.frily.yubot.util.ImageFetcher;
import ch.frily.yubot.util.ProfileImageComposer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public class ProfilContainer extends Container {

    @Getter
    private final Member member;

    @Getter
    private FileUpload profileBanner;

    public ProfilContainer(Member member) {
        this.member = member;
    }

    public CompletableFuture<ProfilContainer> buildAsync() {
        return this.buildProfileBanner()
                .thenApply(fileUpload -> {
                    try {
                        this.profileBanner = fileUpload;

                        // container content
                        MediaGallery gallery = MediaGallery.of(
                                MediaGalleryItem.fromFile(fileUpload)
                        );
                        addComponent(gallery);
                        addFormatedText("# %s's Profil", member.getEffectiveName());

                        addTextDisplay("**Einstellungen**");
                        Map<String, String> settings = mapSettings();
                        if (settings == null) {
                            addTextDisplay("-# Keine Einstellungen gefunden. Stelle sie mit </profile setting:1542519831729934447> ein");
                        } else {
                            StringBuilder settingsSB = new StringBuilder();
                            settings.entrySet().forEach(entry -> {
                                settingsSB.append(String.format("`%s`: %s", entry.getKey(), entry.getValue()));
                                settingsSB.append("\n");
                            });
                            addTextDisplay(settingsSB.toString());
                        }



                        return this;
                    } catch (Exception e) {
                        return ExceptionHandler.fail(e);
                    }
                })
                .exceptionally(ExceptionHandler::fail);
    }

    private Map<String, String> mapSettings() throws SQLException, ClassNotFoundException {
        Profile profile = ProfileRepository.getProfile(member);
        log.info("Profile: {}", profile);
        if (profile == null) {
            return null;
        }

        Map<String, String> settings = new HashMap<>();
        Arrays.stream(Setting.values()).forEach(setting -> {
            log.info("Setting: {}", setting.getLabel());

            try {
                String settingValue = String.valueOf(ProfileRepository.getSetting(member, setting, setting.getDataType()));
                // show "custom text" for custom text that is not a predefined option from autocomplete
                if (setting.getAutocompleteOptions() == null) {
                    settingValue = String.format("\"%s\"", settingValue);
                }
                if (settingValue != "null") {
                    settings.put(setting.getLabel(), settingValue);
                }

            } catch (Exception e) {
                log.info("Failed to retrieve setting value for {}: {}", setting.getLabel(), e.getMessage());
                ExceptionHandler.fail(e);
            }
        });

        return settings;
    }

    private CompletableFuture<FileUpload> buildProfileBanner() {
        CompletableFuture<BufferedImage> bannerFuture = BannerResolver.resolveGlobalBanner(member);
        String avatarUrl = member.getEffectiveAvatar(ImageFormat.PNG).getUrl(1024);
        CompletableFuture<BufferedImage> avatarFuture = ImageFetcher.fetch(avatarUrl);

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
