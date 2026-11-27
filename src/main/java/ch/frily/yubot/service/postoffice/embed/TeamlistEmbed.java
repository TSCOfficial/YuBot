package ch.frily.yubot.service.postoffice.embed;

import ch.frily.yubot.embed.Field;
import ch.frily.yubot.embed.IEmbed;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.service.ticket.repository.TicketTypeControlRepository;
import ch.frily.yubot.service.ticket.TicketTypeGroup;
import ch.frily.yubot.EnvKey;
import ch.frily.yubot.util.MessageUtil;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.frily.yubot.util.Util.getUsersByRole;

@Slf4j
public class TeamlistEmbed implements IEmbed {

    private static final LinkedHashMap<String, List<EnvKey>> ROLES = new LinkedHashMap<>();

    public TeamlistEmbed() {
        if (ROLES.isEmpty()) {
            ROLES.put("Yu", List.of(EnvKey.ROLE_OWNER));
            ROLES.put("Serverleitung", List.of(EnvKey.ROLE_SERVERLEITUNG));
            ROLES.put("Moderator*innen-Leitung", List.of(EnvKey.ROLE_MODLEITUNG, EnvKey.ROLE_STVMODLEITUNG));
            ROLES.put("Moderator*in", List.of(EnvKey.ROLE_MODERATOR));
            ROLES.put("Awareness-Leitung", List.of(EnvKey.ROLE_AWARENESSLEITUNG, EnvKey.ROLE_STVAWARENESSLEITUNG));
            ROLES.put("Awareness-Team", List.of(EnvKey.ROLE_AWARENESS));
            ROLES.put("Event-Leitung", List.of(EnvKey.ROLE_EVENTLEITUNG, EnvKey.ROLE_STVEVENTLEITUNG));
            ROLES.put("Event-Team", List.of(EnvKey.ROLE_EVENT));
            ROLES.put("Connector*innen-Leitung", List.of(EnvKey.ROLE_CONNECTORLEITUNG, EnvKey.ROLE_STVCONNECTORLEITUNG));
            ROLES.put("Connector*in", List.of(EnvKey.ROLE_CONNECTOR));
            ROLES.put("Development-Leitung", List.of(EnvKey.ROLE_DEVLEITUNG));
            ROLES.put("Development-Team", List.of(EnvKey.ROLE_DEVELOPER));
            ROLES.put("Design-Team", List.of(EnvKey.ROLE_DESIGN));
        }
    }

    @Override
    public String getTitle() {
        Role teamRole = EnvKey.ROLE_YUTEAM.getAsRole();
        List<Member> teamMembers = teamRole == null ? List.of() : getUsersByRole(teamRole);
        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
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
        ROLES.forEach((name, roles) -> fields.add(generateFieldByRole(name, roles)));

        Role activeModRole = EnvKey.ROLE_ACTIVEMOD.getAsRole();
        List<Member> activeMods = activeModRole == null ? List.of() : getUsersByRole(activeModRole);

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
                            activeMods.stream().map(mod -> MessageUtil.escapeMarkdown(mod.getEffectiveName())).collect(Collectors.joining(", ")),
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

        List<Role> mappedTypes = openedTypes.stream().map(type -> switch (type) {
            case BEWERBUNG_CONNECTION ->  EnvKey.ROLE_CONNECTOR.getAsRole();
            case BEWERBUNG_MODERATION ->  EnvKey.ROLE_MODERATOR.getAsRole();
            case BEWERBUNG_EVENT ->  EnvKey.ROLE_EVENT.getAsRole();
            case BEWERBUNG_AWARENESS ->  EnvKey.ROLE_AWARENESS.getAsRole();
            default -> null;
        }).filter(Objects::nonNull).toList();

        if (!mappedTypes.isEmpty()) {
            fields.add(new Field(
                    "Wir suchen Teammitglieder*innen ✨",
                    String.format("Bist du daran Interessiert %s zu werden? Bewerbe dich in %s",
                            mappedTypes.stream().map(Role::getAsMention).collect(Collectors.joining(", ")),
                            EnvKey.CHANNEL_SUPPORT.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER).getAsMention()), false
            ));
        }


        return fillWithBlankFields(fields);
    }

    private Field generateFieldByRole(String name, List<EnvKey> roleKeys){
        String userList = "*Nicht besetzt*";
        int userCount = 0;
        List<Role> roles = roleKeys.stream().map(EnvKey::getAsRole).toList();
        List<Member> teammembers = new ArrayList<>();

        roles.forEach(role -> {
            teammembers.addAll(Util.getUsersByRole(role));
        });
        if (!teammembers.isEmpty()) {
            userList = teammembers.stream().map(member -> MessageUtil.escapeMarkdown(member.getEffectiveName())).collect(Collectors.joining("\n"));
            userCount = teammembers.size();
        }
        String roleMentions = String.join(", ", roles.stream().map(Role::getAsMention).toList());
        return new Field(
                String.format("%s *(%s)*", MessageUtil.extractText(name), userCount),
                 roleMentions + "\n" + userList,
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
}
