package ch.frily.yubot.feature;

import ch.frily.yubot.util.Color;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
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
            EnvKey.ROLE_ORGALEITUNG,
            EnvKey.ROLE_ORGA,
            EnvKey.ROLE_DEVLEITUNG,
            EnvKey.ROLE_DEVELOPER,
            EnvKey.ROLE_MODLEITUNG,
            EnvKey.ROLE_MODERATOR,
            EnvKey.ROLE_SUPPORT,
            EnvKey.ROLE_AWARENESSLEITUNG,
            EnvKey.ROLE_AWARENESS,
            EnvKey.ROLE_EVENTLEITUNG,
            EnvKey.ROLE_EVENT
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

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName() + " Team");
        fields.forEach(embedBuilder::addField);
        embedBuilder.setTimestamp(new Date().toInstant());
        embedBuilder.setColor(new Color("cf3f05").get());
        fillWithBlankFields(embedBuilder);

//        List<Member> teammembers = getUsersByRole(EnvResolver.getRoleById(EnvKey.ROLE_TEAM));
//        embedBuilder.addField("Gesammtes Team (" + teammembers.size() + ")",
//                                teammembers.stream().map(Member::getAsMention).collect(Collectors.joining(", ")), false);

        return embedBuilder.build();
    }

    /**
     * Fill the embed with blank fields to keep the embed structured no matter how many roles there are.
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
            userList = getUsersByRole(role).stream().map(Member::getAsMention).collect(Collectors.joining("\n"));
            userCount = getUsersByRole(role).size();
        }
        return new MessageEmbed.Field(
                extractText(role.getName()) + " (" + userCount + ")",
                role.getAsMention() + "\n" + userList,
                true
        );
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
        return ROLE_NAME_PATTERN.matcher(input).replaceAll("").trim();
    }
}
