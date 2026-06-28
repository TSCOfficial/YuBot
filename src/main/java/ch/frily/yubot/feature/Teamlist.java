package ch.frily.yubot.feature;

import ch.frily.yubot.util.Color;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Teamlist {

    private static Teamlist instance;

    private static final List<EnvKey> ROLE_KEYWORDS = List.of(
            EnvKey.ROLE_OWNER,
            EnvKey.ROLE_SERVERLEITUNG,
            EnvKey.ROLE_ADMIN,
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SUPPORT,
            EnvKey.ROLE_AWARENESSLEITUNG,
            EnvKey.ROLE_AWARENESS,
            EnvKey.ROLE_DEVLEITUNG,
            EnvKey.ROLE_DEVELOPER,
            EnvKey.ROLE_EVENTLEITUNG,
            EnvKey.ROLE_EVENT,
            EnvKey.ROLE_ORGA
    );

    private static final Pattern ROLE_NAME_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\-\\s\\*]");

    public static Teamlist getInstance() {
        if (instance == null) {
            instance = new Teamlist();
        }
        return instance;
    }

    /**
     * Generate all the Fields sing the role keyword list
     */
    public MessageEmbed generateEmbed() {
        List<Role> roles = ROLE_KEYWORDS.stream().map(EnvResolver::getRoleById).toList();
        List<MessageEmbed.Field> fields = roles.stream().map(this::generateFieldByRole).toList();

        List<Member> teamMembers = getUsersByRole(EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM));

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(
                String.format("%s's Team *(%s)*", EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName(), teamMembers.size()));
        fields.forEach(embedBuilder::addField);
        embedBuilder.setTimestamp(new Date().toInstant());
        embedBuilder.setColor(new Color("cf3f05").get());
        fillWithBlankFields(embedBuilder);

        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        List<Member> activeMods = getUsersByRole(activeModRole);

        if (activeMods.isEmpty()) {
            embedBuilder.addField(
                    "Aktive Moderation *(0)*",
                    String.format("%s/n*Nicht besetzt*", activeModRole.getAsMention()), false);
        } else {
            embedBuilder.addField(String.format("Aktive Moderation *(%s)*", activeMods.size()),
                    activeModRole.getAsMention() + "\n" +
                    activeMods.stream().map(this::getFormattedUsername).collect(Collectors.joining(", ")), false);
        }

        return embedBuilder.build();
    }

    /**
     * Fill the embed with blank fields to keep the embed structured no matter how many holders there are.
     */
    private void fillWithBlankFields(EmbedBuilder embedBuilder) {
        int fieldCountOffset = embedBuilder.getFields().size() % 3;
        if (fieldCountOffset != 0) {
            int blanksNeeded = 3 - fieldCountOffset;
            for (int i = 0; i < blanksNeeded; i++) {
                embedBuilder.addBlankField(true);
            }
        }
    }

    /**
     * Generate a field by a given Role
     * @param role
     */
    private MessageEmbed.Field generateFieldByRole(Role role) {
        String userList = "*Nicht besetzt*";
        int userCount = 0;

        if (!getUsersByRole(role).isEmpty()) {
            userList = getUsersByRole(role).stream().map(this::getFormattedUsername).collect(Collectors.joining("\n"));
            userCount = getUsersByRole(role).size();
        }
        return new MessageEmbed.Field(
                String.format("%s *(%s)*", extractText(role.getName()), userCount),
                role.getAsMention() + "\n" + userList,
                true
        );
    }

    private String getFormattedUsername(Member member) {
        return "@" + Util.escapeMarkdown(member.getUser().getName());
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
