package ch.frily.yubot.container.profile;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.setting.Settings;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.feature.setting.Setting;
import ch.frily.yubot.interaction.button.btn.profile.AddProfileBtn;
import ch.frily.yubot.interaction.button.btn.profile.EditProfileBtn;
import ch.frily.yubot.interaction.button.btn.profile.UseProfileBtn;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.interaction.select.select.ProfileUseSelect;
import ch.frily.yubot.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

@Slf4j
public class ProfilContainer extends Container {

    private static final String ACTIVE_TAG =
            "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>";

    @Getter
    private final Member member;

    @Setter
    @Getter
    private Profile profile;

    @Getter
    private FileUpload profileBanner;

    public ProfilContainer(Member member) {
        this.member = member;
    }

    public CompletableFuture<ProfilContainer> buildAsync() {
        return buildProfileBanner()
                .thenApply(fileUpload -> {
                    this.profileBanner = fileUpload;
                    return this;
                })
                .exceptionally(ExceptionHandler::fail)
                .thenApply(container -> {
                    container.assembleContent();
                    return container;
                });
    }

    private void assembleContent() {
        addComponent(MediaGallery.of(MediaGalleryItem.fromFile(profileBanner)));
        try {
            Optional<Profile> currentProfile = ProfileRepository.getCurrentUserProfile(member);
            List<Profile> linkedProfiles = ProfileRepository.getProfilesFromAccount(member);
            log.info("profile: {}", profile);
            boolean profileIsAlreadyInUse = profile != null && currentProfile.isPresent() && Objects.equals(currentProfile.get().profileId(), profile.profileId());

            if (profile != null) {
                String proxy = !profile.proxy().isBlank() ? " (" + profile.proxy() + ")" : "";
                addFormatedText("# %s's Profil %s %s", profile.name(), proxy, profileIsAlreadyInUse ? ACTIVE_TAG : "");
            } else {
                addFormatedText("# %s %s", member.getEffectiveName(), profileIsAlreadyInUse ? ACTIVE_TAG : "");
                if (!linkedProfiles.isEmpty()) {
                    addTextDisplay("-# Standardprofil");
                }
            }


            if (profile != null) {
                addTextDisplay("**Kontoeinstellungen**");
            } else {
                addTextDisplay("**Einstellungen**");
            }
            addTextDisplay(buildSettingsText());

            UseProfileBtn useProfileBtn = new UseProfileBtn();
            if (profileIsAlreadyInUse) {
                useProfileBtn.disable(true);
            } else if (profile != null) {
                useProfileBtn.addArgument("p", profile.profileId());
            }

            List<Button> profileControl = new ArrayList<>();
            // use & edit profile
            if (!linkedProfiles.isEmpty()) {
                profileControl.add(useProfileBtn.build());

                if (profile != null) {
                    EditProfileBtn editProfileBtn = new EditProfileBtn();
                    editProfileBtn.addArgument("p", profile.profileId());
                    profileControl.add(editProfileBtn.build());
                }
            }
            // add new profile btn
            if (Util.isPermitted(member, Stream.of(EnvKey.ROLE_DIDSYSTEM).map(EnvResolver::getRoleById).toList())) {
                profileControl.add(new AddProfileBtn().build());
            }

            if (!profileControl.isEmpty()) {
                addLineSeparator(Separator.Spacing.SMALL);
                addComponent(
                        ActionRow.of(profileControl)
                );
            }

            // profile select
            if (!linkedProfiles.isEmpty()) {
                ProfileUseSelect profileUseSelect = new ProfileUseSelect();
                profileUseSelect.setMember(member);
                profileUseSelect.setProfile(profile);

                addComponent(
                        ActionRow.of(
                                profileUseSelect.build()
                        )
                );

                if (linkedProfiles.size() >= ISelect.MAX_SELECT_OPTIONS - 1) { // -1 because of the default profile in the selection
                    addFormatedText("-# Es können nur 25 Optionen angezeigt werden. Verwende </profile show:1542519831729934447> um ein spezifisches Profil anzusehen.");
                }
            }


        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private String buildSettingsText() {
        Map<String, String> settings;
        try {
            settings = mapSettings();
        } catch (SQLException | ClassNotFoundException e) {
            return ExceptionHandler.fail(e);
        }

        if (settings == null) {
            return "-# Keine Einstellungen gefunden. Stelle sie mit </profile setting:1542519831729934447> ein";
        }

        StringBuilder settingsSB = new StringBuilder();
        settings.forEach((key, value) -> {
            if (!"null".equals(value)) {
                settingsSB.append(String.format("`%s`: %s", key, value)).append("\n");
            }
        });
        return settingsSB.toString();
    }

    private Map<String, String> mapSettings() throws SQLException, ClassNotFoundException {
        Settings settings = SettingRepository.getSettings(member);
        if (settings == null) {
            return null;
        }

        Map<String, String> mappedSettings = new HashMap<>();
        for (Setting setting : Setting.values()) {
            try {
                Object rawValue = SettingRepository.getSetting(member, setting, setting.getDataType());
                if (rawValue == null) {
                    continue; // skip setting
                }

                String settingValue;
                if (!setting.getAutocompleteOptions().isEmpty()) {
                    settingValue = setting.getOptionByValue(rawValue).label();
                } else {
                    // custom text gets concat with " "
                    String rawText = String.valueOf(rawValue);
                    settingValue = rawText.isBlank() ? rawText : String.format("\"%s\"", rawText);
                }

                mappedSettings.put(setting.getLabel(), settingValue);
            } catch (Exception e) {
                ExceptionHandler.fail(e);
            }
        }

        return mappedSettings;
    }

    private CompletableFuture<FileUpload> buildProfileBanner() {
        CompletableFuture<BufferedImage> bufferedBanner = BannerResolver.resolveGlobalBanner(member);
        CompletableFuture<BufferedImage> bufferedProfilePicture = fetchProfilePicture();

        return bufferedBanner.thenCombine(bufferedProfilePicture, (banner, profilePicture) -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedImage composed = ProfileImageComposer.compose(banner, profilePicture);
                ImageIO.write(composed, "png", baos);
                return FileUpload.fromData(baos.toByteArray(), "profile-banner.png");
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new CompletionException(new ImagingOpException("Failed to draw image"));
            }
        });
    }


    /**
     * fecthes the profilepicture of the current profile. If no profile is defined or the picture is invalid (null), the member-avatar is taken as fallback
     * @return Profile's-/Member's Avatar
     */
    private CompletableFuture<BufferedImage> fetchProfilePicture() {
        return ImageFetcher.fetch(getProfilePicture(false))
                .thenCompose(picture -> picture != null
                        ? CompletableFuture.completedFuture(picture)
                        : ImageFetcher.fetch(getProfilePicture(true)))
                .exceptionallyCompose(e -> {
                    return ImageFetcher.fetch(getProfilePicture(true));
                });
    }

    private String getProfilePicture(boolean forceMemberProfile) {
        try {
            if (!forceMemberProfile && profile != null && profile.profilePicture() != null && !profile.profilePicture().isBlank()) {
                return new ImageProxy(profile.profilePicture()).getUrl(1024);
            }
            return member.getEffectiveAvatar(ImageFormat.PNG).getUrl(1024);
        } catch (Exception e) {
            return ExceptionHandler.fail(e);
        }
    }
}