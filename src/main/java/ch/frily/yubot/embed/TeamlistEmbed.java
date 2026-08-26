package ch.frily.yubot.embed;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeControlRepository;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ch.frily.yubot.util.Util.getUsersByRole;

@Slf4j
public class TeamlistEmbed implements IEmbed {

    private static final List<EnvKey> ROLE_KEYWORDS = List.of(
            EnvKey.ROLE_OWNER,
            EnvKey.ROLE_SERVERLEITUNG,
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_SUPPORTLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SUPPORT,
            EnvKey.ROLE_AWARENESSLEITUNG,
            EnvKey.ROLE_AWARENESS,
            EnvKey.ROLE_DEVLEITUNG,
            EnvKey.ROLE_DEVELOPER,
            EnvKey.ROLE_EVENTLEITUNG,
            EnvKey.ROLE_EVENT
    );

    private static final Pattern ROLE_NAME_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\-\\s\\*]");

    @Override
    public String getTitle() {
        Role teamRole = EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM);
        List<Member> teamMembers = teamRole == null ? List.of() : getUsersByRole(teamRole);
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        String guildName = guild == null ? "YuServer" : guild.getName();
        return String.format("%s's Team *(%s)*", guildName, teamMembers.size());
    }

    @Override
    public Color getColor() {
        return new ch.frily.yubot.util.Color("#7699a9").get();
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        ROLE_KEYWORDS.forEach(roleKey -> fields.add(generateFieldByRole(roleKey)));

        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        List<Member> activeMods = activeModRole == null ? List.of() : getUsersByRole(activeModRole);

        if (activeMods.isEmpty()) {
            fields.add(new Field(
                    "Aktive Moderation *(0)*",
                    String.format("%s\n*Nicht besetzt*", getRoleMention(activeModRole)),
                    false)
            );
        } else {
            fields.add(new Field(
                    String.format("Aktive Moderation *(%s)*", activeMods.size()),
                    getRoleMention(activeModRole) + "\n" +
                            activeMods.stream().map(mod -> Util.escapeMarkdown(mod.getEffectiveName())).collect(Collectors.joining(", ")),
                    false)
            );
        }

        List<TicketType> openedTypes = Arrays.stream(TicketType.values()).filter(type -> {
                    try {
                        if (type.getGroup() != TicketTypeGroup.BEWERBUNG) return false;
                        return !TicketTypeControlRepository.isTypeLocked(type);
                    } catch (Exception e) {
                        return false;
                    }
        }).toList();

        List<Role> mappedTypes = openedTypes.stream().map(type -> {
            return switch (type) {
                case BEWERBUNG_SUPPORT -> EnvResolver.getRoleById(EnvKey.ROLE_SUPPORT);
                case BEWERBUNG_MODERATION -> EnvResolver.getRoleById(EnvKey.ROLE_MODERATOR);
                case BEWERBUNG_EVENT -> EnvResolver.getRoleById(EnvKey.ROLE_EVENT);
                case BEWERBUNG_AWARENESS -> EnvResolver.getRoleById(EnvKey.ROLE_AWARENESS);
                default -> null;
            };
        }).filter(Objects::nonNull).toList();

        String searchedRoles = mappedTypes.isEmpty()
                ? "keinem Bereich"
                : mappedTypes.stream().map(Role::getAsMention).collect(Collectors.joining(", "));

        if (mappedTypes.isEmpty()) {}
        fields.add(new Field(
                "Wir suchen Teammitglieder*innen ✨",
                String.format("In %s suchen wir noch Teammitglieder. Bewerbe dich in <#%s>",
                searchedRoles,
                EnvResolver.getString(EnvKey.CHANNEL_SUPPORT)), false
        ));

        return fillWithBlankFields(fields);
    }

    private Field generateFieldByRole(EnvKey roleKey){
        String userList = "*Nicht besetzt*";
        int userCount = 0;
        Role role = EnvResolver.getRoleById(roleKey);

        if (role != null) {
            userList = getUsersByRole(role).stream().map(member -> Util.escapeMarkdown(member.getEffectiveName())).collect(Collectors.joining("\n"));
            userCount = getUsersByRole(role).size();
        }
        return new Field(
                String.format("%s *(%s)*", getRoleName(roleKey, role), userCount),
                getRoleMention(role) + "\n" + userList,
                true
        );
    }

    /**
     * Fill the embed with blank fields to keep the embed structured no matter how many holders there are.
     */
    private List<Field> fillWithBlankFields(List<Field> currentFields) {
        int fieldCountOffset = currentFields.size() % 3;
        if (fieldCountOffset != 0) {
            int blanksNeeded = 3 - fieldCountOffset;
            for (int i = 0; i < blanksNeeded; i++) {
                currentFields.add(new Field(true));
            }
        }
        return currentFields;
    }

    private String getRoleName(EnvKey roleKey, Role role) {
        if (role == null) {
            return roleKey.name();
        }
        return extractText(role.getName());
    }

    private String getRoleMention(Role role) {
        if (role == null) {
            return "`Role nicht gefunden`";
        }
        return role.getAsMention();
    }

    private static String extractText(String input) {
        if (input == null || input.isBlank()) return input;
        String roleName = ROLE_NAME_PATTERN.matcher(input).replaceAll("").trim();
        return Util.escapeMarkdown(roleName);
    }
}
