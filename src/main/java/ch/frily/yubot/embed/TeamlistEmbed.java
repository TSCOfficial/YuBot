package ch.frily.yubot.embed;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        List<Member> teamMembers = getUsersByRole(EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM));
        return String.format("%s's Team *(%s)*", EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName(), teamMembers.size());
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
        List<Member> activeMods = getUsersByRole(activeModRole);

        if (activeMods.isEmpty()) {
            fields.add(new Field(
                    "Aktive Moderation *(0)*",
                    String.format("%s\n*Nicht besetzt*", activeModRole.getAsMention()),
                    false)
            );
        } else {
            fields.add(new Field(
                    String.format("Aktive Moderation *(%s)*", activeMods.size()),
                    activeModRole.getAsMention() + "\n" +
                            activeMods.stream().map(mod -> Util.escapeMarkdown(mod.getEffectiveName())).collect(Collectors.joining(", ")),
                    false)
            );
        }

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
                String.format("%s *(%s)*", extractText(role.getName()), userCount),
                role.getAsMention() + "\n" + userList,
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
                log.info("Added blank field to keep embed structured");
            }
        }
        return currentFields;
    }

    /**
     * Find members that have a given role<br>
     * @param role
     * @return true | false : Returns true when the list is completed
     */
    private List<Member> getUsersByRole(Role role) {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        return guild.getMembersWithRoles(role);
    }

    private static String extractText(String input) {
        if (input == null || input.isBlank()) return input;
        String roleName = ROLE_NAME_PATTERN.matcher(input).replaceAll("").trim();
        return Util.escapeMarkdown(roleName);
    }
}
