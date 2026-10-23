package ch.frily.yubot.container.profile;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.profile.*;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.service.profile.button.AddProfileBtn;
import ch.frily.yubot.service.profile.button.DeleteProfileBtn;
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
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ProfilContainer extends Container {

    private static final String ACTIVE_TAG =
            "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>";

    private static final Map<Role, Emoji> PRONOUN_ROLES = Map.ofEntries(
            Map.entry(EnvKey.ROLE_ERIHM.getAsRole(), EnvKey.EMOJI_PRONOMEN_ERIHM.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ERSIE.getAsRole(), EnvKey.EMOJI_PRONOMEN_ERSIE.getAsEmoji()),
            Map.entry(EnvKey.ROLE_SIEER.getAsRole(), EnvKey.EMOJI_PRONOMEN_SIEER.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ERDEY.getAsRole(), EnvKey.EMOJI_PRONOMEN_ERDEY.getAsEmoji()),
            Map.entry(EnvKey.ROLE_SIEIHR.getAsRole(), EnvKey.EMOJI_PRONOMEN_SIEIHR.getAsEmoji()),
            Map.entry(EnvKey.ROLE_SIEDEY.getAsRole(), EnvKey.EMOJI_PRONOMEN_SIEDEY.getAsEmoji()),
            Map.entry(EnvKey.ROLE_DEYDEREN.getAsRole(), EnvKey.EMOJI_PRONOMEN_DEYDEREN.getAsEmoji()),
            Map.entry(EnvKey.ROLE_DEYER.getAsRole(), EnvKey.EMOJI_PRONOMEN_DEYER.getAsEmoji()),
            Map.entry(EnvKey.ROLE_DEYSIE.getAsRole(), EnvKey.EMOJI_PRONOMEN_DEYSIE.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ES.getAsRole(), EnvKey.EMOJI_PRONOMEN_ES.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ERES.getAsRole(), EnvKey.EMOJI_PRONOMEN_ERES.getAsEmoji()),
            Map.entry(EnvKey.ROLE_SIEES.getAsRole(), EnvKey.EMOJI_PRONOMEN_SIEES.getAsEmoji()),
            Map.entry(EnvKey.ROLE_DEYES.getAsRole(), EnvKey.EMOJI_PRONOMEN_DEYES.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ESER.getAsRole(), EnvKey.EMOJI_PRONOMEN_ESER.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ESSIE.getAsRole(), EnvKey.EMOJI_PRONOMEN_ESSIE.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ESDEY.getAsRole(), EnvKey.EMOJI_PRONOMEN_ESDEY.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ALLE.getAsRole(), EnvKey.EMOJI_PRONOMEN_ALLE.getAsEmoji()),
            Map.entry(EnvKey.ROLE_KEINE.getAsRole(), EnvKey.EMOJI_PRONOMEN_KEINE.getAsEmoji()),
            Map.entry(EnvKey.ROLE_ANDEREFRAGEN.getAsRole(), EnvKey.EMOJI_PRONOMEN_ANDEREFRAGEN.getAsEmoji())
    );

    private static final List<Role> DM_ROLES = Stream.of(
            EnvKey.ROLE_DMOFFEN,
            EnvKey.ROLE_DMFRAGEN,
            EnvKey.ROLE_DM_GESCHLOSSEN
    ).map(EnvKey::getAsRole).toList();

    /** Whether the profile is owned by the person viewing the profile details */
    @Getter
    private boolean isOwnProfile = false;

    @Getter
    private final Member target;

    @Setter
    @Getter
    private Profile profile;

    @Getter
    private FileUpload profileBanner;

    public ProfilContainer(Member initiator, Member target) {
        if (target == null) {
            target = initiator;
        }
        if (initiator == target) {
            this.isOwnProfile = true;
        }
        this.target = target;
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

    private void assembleContent() {;
        addComponent(MediaGallery.of(MediaGalleryItem.fromFile(profileBanner)));
        try {
            if (isOwnProfile) {
                assemblePrivateProfile();
            } else {
                assemblePublicProfile();
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private void assemblePublicProfile() {
        addFormatedText("# %s %s", target.getEffectiveName(), getMemberPronouns());

        if (getMemberDMStatus() != null) {
            addFormatedText("-# %s", getMemberDMStatus());
        }
    }

    /**
     * Defines the private part of a profile, which only the user themself can see.
     */
    private void assemblePrivateProfile() throws SQLException {
        Optional<Profile> currentProfile = ProfileRepository.getCurrentUserProfile(target);
        List<Profile> linkedProfiles = ProfileRepository.getProfilesFromAccount(target);
        boolean profileIsAlreadyInUse = (profile != null && currentProfile.isPresent() && Objects.equals(currentProfile.get().profileId(), profile.profileId())) || (profile == null && !linkedProfiles.isEmpty() && currentProfile.isEmpty());

        if (profile != null) {
            String proxy = Objects.nonNull(profile.proxy()) ? " (" + profile.proxy() + ")" : "";
            addFormatedText("# %s's Profil %s %s", profile.name(), proxy, profileIsAlreadyInUse ? ACTIVE_TAG : "");
        } else {
            addFormatedText("# %s %s%s", target.getEffectiveName(), getMemberPronouns(), profileIsAlreadyInUse ? ACTIVE_TAG : "");
            if (!linkedProfiles.isEmpty()) {
                addTextDisplay("-# Standardprofil");
            }
            if (getMemberDMStatus() != null) {
                addFormatedText("-# %s", getMemberDMStatus());
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

        // use profile
        if (!linkedProfiles.isEmpty()) {
            profileControl.add(useProfileBtn.build());
        }
        // add new profile btn
        if (Util.isPermitted(target, Stream.of(EnvKey.ROLE_DIDSYSTEM).map(EnvKey::getAsRole).toList())) {
            profileControl.add(new AddProfileBtn().build());
        }
        // edit & delete profile
        if (!linkedProfiles.isEmpty()) {

            if (profile != null) {
                EditProfileBtn editProfileBtn = new EditProfileBtn();
                editProfileBtn.addArgument("p", profile.profileId());
                profileControl.add(editProfileBtn.build());

                DeleteProfileBtn deleteProfileBtn = new DeleteProfileBtn();
                deleteProfileBtn.addArgument("p", profile.profileId());
                profileControl.add(deleteProfileBtn.build());
            }
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
            profileUseSelect.setMember(target);
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

    private String getMemberPronouns() {
        log.info("Getting member pronouns for {}", target);
        Map<Role, Emoji> userPronouns = PRONOUN_ROLES.entrySet().stream().filter(entry -> target.getRoles().contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        log.info("Member pronouns: {}", userPronouns);
        List<String> pronouns = userPronouns.entrySet().stream().map(entry -> {
            return entry.getValue().getFormatted();
        }).toList();
        if (pronouns.isEmpty()) {
            return " ";
        }
        return String.join(" ", pronouns);
    }

    private String getMemberDMStatus() {
        return target.getRoles().stream().filter(DM_ROLES::contains).findFirst().map(Role::getName).orElse(null);
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
        Settings settings = SettingRepository.getSettings(target);
        if (settings == null) {
            return null;
        }

        Map<String, String> mappedSettings = new HashMap<>();
        for (Setting setting : Setting.values()) {
            try {
                Object rawValue = SettingRepository.getSetting(target, setting, setting.getDataType());
                if (rawValue == null) {
                    continue; // skip setting
                }

                String settingValue;
                if (!setting.getChoices().isEmpty() && setting.getOption() != null) {
                    settingValue = setting.getChoiceByValue(rawValue).label();
                } else if (setting.getChoices().isEmpty() && setting.getOption() != null) {
                    // custom text gets concat with " "
                    String rawText = String.valueOf(rawValue);
                    settingValue = rawText.isBlank() ? rawText : String.format("\"%s\"", rawText);
                } else {
                    SettingOnDisplayCtx displayCtx = new SettingOnDisplayCtx(setting, String.valueOf(rawValue));
                    settingValue = setting.getOnDisplay().apply(displayCtx);
                }

                mappedSettings.put(setting.getLabel(), settingValue);
            } catch (Exception e) {
                ExceptionHandler.fail(e);
            }
        }

        return mappedSettings;
    }

    private CompletableFuture<FileUpload> buildProfileBanner() {
        CompletableFuture<BufferedImage> bufferedBanner = BannerResolver.resolveGlobalBanner(target);
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
        try {
            if (!forceMemberProfile && profile != null && profile.profilePicture() != null && !profile.profilePicture().isBlank()) {
                return new ImageProxy(profile.profilePicture()).getUrl(1024);
            }
            return target.getEffectiveAvatar(ImageFormat.PNG).getUrl(1024);
        } catch (Exception e) {
            return ExceptionHandler.fail(e);
        }
    }
}