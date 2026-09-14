package ch.frily.yubot.container.profile;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.setting.Settings;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.feature.setting.Setting;
import ch.frily.yubot.interaction.button.btn.profile.AddProfileBtn;
import ch.frily.yubot.interaction.button.btn.profile.UseProfileBtn;
import ch.frily.yubot.interaction.select.select.ProfileUseSelect;
import ch.frily.yubot.util.BannerResolver;
import ch.frily.yubot.util.ImageFetcher;
import ch.frily.yubot.util.ProfileImageComposer;
import ch.frily.yubot.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public class ProfilContainer extends Container {

    @Getter
    private final Member member;

    @Getter
    private Profile profile;

    @Getter
    private FileUpload profileBanner;

    public ProfilContainer(Member member) {
        this.member = member;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
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

                        Optional<Profile> currentProfile = ProfileRepository.getCurrentUserProfile(profile.parentAccount());
                        boolean profileIsAlreadyInUse = currentProfile.isPresent() && Objects.equals(currentProfile.get().profileId(), profile.profileId());
                        String activeTag = profileIsAlreadyInUse ? "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>" : "";
                        if (profile != null) {
                            addFormatedText("# %s's Profil %s", profile.name(), activeTag);
                        } else {
                            addFormatedText("# %s's Profil", member.getEffectiveName());
                        }


                        addTextDisplay("**Einstellungen**");
                        if (profile != null) {
                            addTextDisplay("-# Einstellungen sind Konto-, nicht Profilspezifisch.");
                        }
                        Map<String, String> settings = mapSettings();
                        if (settings == null) {
                            addTextDisplay("-# Keine Einstellungen gefunden. Stelle sie mit </profile setting:1542519831729934447> ein");
                        } else {
                            StringBuilder settingsSB = new StringBuilder();
                            settings.forEach((key, value) -> {
                                if (!value.equals("null")) {
                                    settingsSB.append(String.format("`%s`: %s", key, value)).append("\n");
                                }

                            });
                            addTextDisplay(settingsSB.toString());
                        }

                        addLineSeparator(Separator.Spacing.SMALL);

                        UseProfileBtn useProfileBtn = new UseProfileBtn();
                        if (profileIsAlreadyInUse) {
                            useProfileBtn.disable(true);
                        } else {
                            useProfileBtn.addArgument("p", profile.profileId()); // transfer profile ID
                        }


                        addComponent(
                                ActionRow.of(
                                        useProfileBtn.build(),
                                        new AddProfileBtn().build()
                                )
                        );

                        ProfileUseSelect profileUseSelect = new ProfileUseSelect();
                        profileUseSelect.setProfile(profile);

                        addComponent(
                                ActionRow.of(
                                        profileUseSelect.build()
                                )
                        );

                        return this;
                    } catch (Exception e) {
                        return ExceptionHandler.fail(e);
                    }
                })
                .exceptionally(ExceptionHandler::fail);
    }

    private Map<String, String> mapSettings() throws SQLException, ClassNotFoundException {
        Settings settings = SettingRepository.getSettings(member);
        if (settings == null) {
            return null;
        }

        Map<String, String> mappedSettings = new HashMap<>();
        Arrays.stream(Setting.values()).forEach(setting -> {

            try {
                String settingValue = String.valueOf(SettingRepository.getSetting(member, setting, setting.getDataType()));
                // show "custom text" for custom text that is not a predefined option from autocomplete
                if (setting.getAutocompleteOptions() == null) {
                    settingValue = String.format("\"%s\"", settingValue);
                }
                if (settingValue != "null") {

                    switch (setting) {
                        case Setting.ACTIVEMOD_SEND_IN_DM:
                            settingValue = setting.getOptionByValue(Boolean.valueOf(settingValue)).label();
                            break;
                    }
                    mappedSettings.put(setting.getLabel(), settingValue);
                }

            } catch (Exception e) {
                ExceptionHandler.fail(e);
            }
        });

        return mappedSettings;
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
