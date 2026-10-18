package ch.frily.yubot.container.profile;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.service.profile.Settings;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.profile.button.AddProfileBtn;
import ch.frily.yubot.service.profile.button.EditProfileBtn;
import ch.frily.yubot.service.profile.button.UseProfileBtn;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.service.profile.select.ProfileUseSelect;
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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Slf4j
public class ProfilContainer extends Container {

    private static final String ACTIVE_TAG =
            "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>";

    private static final List<Role> PRONOUN_ROLES = Stream.of(
            EnvKey.ROLE_ERIHM,
            EnvKey.ROLE_ERSIE,
            EnvKey.ROLE_SIEER,
            EnvKey.ROLE_ERDEY,
            EnvKey.ROLE_SIEIHR,
            EnvKey.ROLE_SIEDEY,
            EnvKey.ROLE_DEYDEREN,
            EnvKey.ROLE_DEYER,
            EnvKey.ROLE_DEYSIE,
            EnvKey.ROLE_ES,
            EnvKey.ROLE_ERES,
            EnvKey.ROLE_SIEES,
            EnvKey.ROLE_DEYES,
            EnvKey.ROLE_ESER,
            EnvKey.ROLE_ESSIE,
            EnvKey.ROLE_ESDEY,
            EnvKey.ROLE_ALLE,
            EnvKey.ROLE_KEINE,
            EnvKey.ROLE_ANDEREFRAGEN
    ).map(EnvKey::getAsRole).toList();

    @Getter
    private final Member initiator;

    @Getter
    @Setter
    private Member target;

    @Setter
    @Getter
    private Profile profile;

    @Getter
    private FileUpload profileBanner;

    public ProfilContainer(Member initiator) {
        this.initiator = initiator;
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
        Member member = target != null ? target : initiator;
        addComponent(MediaGallery.of(MediaGalleryItem.fromFile(profileBanner)));
        try {
            if (target == null) {
                assemblePrivateProfile(member);
            } else {
                assemblePublicProfile(member);
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private void assemblePublicProfile(Member member) {
        List<Role> userPronouns = PRONOUN_ROLES.stream().filter(role -> member.getRoles().contains(role)).toList();
        addFormatedText("# %s", member.getEffectiveName());

        List<String> pronouns = userPronouns.stream().map(role -> {
            return String.format("%s %s", role.getIcon().getEmoji(), role.getName()); // todo add mapping for pronoun Icons, can then be placec next to the name directly
        }).toList();
        addFormatedText("-# %s", String.join(", ", pronouns));
    }

    /**
     * Defines the private part of a profile, which only the user themself can see.
     */
    private void assemblePrivateProfile(Member member) throws SQLException {
        Optional<Profile> currentProfile = ProfileRepository.getCurrentUserProfile(member);
        List<Profile> linkedProfiles = ProfileRepository.getProfilesFromAccount(member);
        boolean profileIsAlreadyInUse = target == null && ((profile != null && currentProfile.isPresent() && Objects.equals(currentProfile.get().profileId(), profile.profileId())) || (profile == null && !linkedProfiles.isEmpty() && currentProfile.isEmpty()));

        if (profile != null) {
            String proxy = !profile.proxy().isBlank() ? " (" + profile.proxy() + ")" : "";
            addFormatedText("# %s's Profil %s %s", profile.name(), proxy, profileIsAlreadyInUse ? ACTIVE_TAG : "");
        } else {
            addFormatedText("# %s %s", member.getEffectiveName(), profileIsAlreadyInUse ? ACTIVE_TAG : "");
            if (target == null && !linkedProfiles.isEmpty()) {
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
        if (Util.isPermitted(initiator, Stream.of(EnvKey.ROLE_DIDSYSTEM).map(EnvKey::getAsRole).toList())) {
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
            profileUseSelect.setMember(initiator);
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
    }

    private String buildSettingsText() {
        Map<String, String> settings;
        try {
            settings = mapSettings();
        } catch (SQLException e) {
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

    private Map<String, String> mapSettings() throws SQLException {
        Settings settings = SettingRepository.getSettings(initiator);
        if (settings == null) {
            return null;
        }

        Map<String, String> mappedSettings = new HashMap<>();
        for (Setting setting : Setting.values()) {
            try {
                Object rawValue = SettingRepository.getSetting(initiator, setting, setting.getDataType());
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
        Member member = target != null ? target : initiator;
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
                .exceptionallyCompose(_ -> {
                    return ImageFetcher.fetch(getProfilePicture(true));
                });
    }

    private String getProfilePicture(boolean forceMemberProfile) {
        Member member = target != null ? target : initiator;
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