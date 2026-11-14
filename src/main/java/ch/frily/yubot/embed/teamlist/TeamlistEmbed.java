package ch.frily.yubot.embed.teamlist;

import ch.frily.yubot.Client;
import ch.frily.yubot.embed.Field;
import ch.frily.yubot.embed.IEmbed;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.service.ticket.repository.TicketTypeControlRepository;
import ch.frily.yubot.service.ticket.TicketTypeGroup;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.frily.yubot.util.Util.getUsersByRole;

@Slf4j
public class TeamlistEmbed implements IEmbed {

    private static final List<Role> ROLES = Stream.of(
            Client.EnvKey.ROLE_OWNER,
            Client.EnvKey.ROLE_SERVERLEITUNG,
            Client.EnvKey.ROLE_MODLEITUNG,
            Client.EnvKey.ROLE_SUPPORTLEITUNG,
            Client.EnvKey.ROLE_MODERATOR,
            Client.EnvKey.ROLE_SUPPORT,
            Client.EnvKey.ROLE_AWARENESSLEITUNG,
            Client.EnvKey.ROLE_AWARENESS,
            Client.EnvKey.ROLE_DEVLEITUNG,
            Client.EnvKey.ROLE_DEVELOPER,
            Client.EnvKey.ROLE_EVENTLEITUNG,
            Client.EnvKey.ROLE_EVENT,
            Client.EnvKey.ROLE_DESIGN
    ).map(Client.EnvKey::getAsRole).toList();

    @Override
    public String getTitle() {
        Role teamRole = Client.EnvKey.ROLE_YUTEAM.getAsRole();
        List<Member> teamMembers = teamRole == null ? List.of() : getUsersByRole(teamRole);
        Guild guild = Client.EnvKey.GUILD_YUSERVER.getAsGuild();
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
        ROLES.forEach(role -> fields.add(generateFieldByRole(role)));

        Role activeModRole = Client.EnvKey.ROLE_ACTIVEMOD.getAsRole();
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

        List<Role> mappedTypes = openedTypes.stream().map(type -> switch (type) {
            case BEWERBUNG_SUPPORT ->  Client.EnvKey.ROLE_SUPPORT.getAsRole();
            case BEWERBUNG_MODERATION ->  Client.EnvKey.ROLE_MODERATOR.getAsRole();
            case BEWERBUNG_EVENT ->  Client.EnvKey.ROLE_EVENT.getAsRole();
            case BEWERBUNG_AWARENESS ->  Client.EnvKey.ROLE_AWARENESS.getAsRole();
            default -> null;
        }).filter(Objects::nonNull).toList();

        if (!mappedTypes.isEmpty()) {
            fields.add(new Field(
                    "Wir suchen Teammitglieder*innen ✨",
                    String.format("In %s suchen wir noch Teammitglieder. Bewerbe dich in %s",
                            mappedTypes.stream().map(Role::getAsMention).collect(Collectors.joining(", ")),
                            Client.EnvKey.CHANNEL_SUPPORT.getAsChannel(TextChannel.class, Client.EnvKey.GUILD_YUSERVER).getAsMention()), false
            ));
        }


        return fillWithBlankFields(fields);
    }

    private Field generateFieldByRole(Role role){
        Objects.requireNonNull(role);

        String userList = "*Nicht besetzt*";
        int userCount = 0;

        if (!getUsersByRole(role).isEmpty()) {
            userList = getUsersByRole(role).stream().map(member -> Util.escapeMarkdown(member.getEffectiveName())).collect(Collectors.joining("\n"));
            userCount = getUsersByRole(role).size();
        }

        return new Field(
                String.format("%s *(%s)*", Util.extractText(role.getName()), userCount),
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

    private String getRoleMention(Role role) {
        if (role == null) {
            return "`Role nicht gefunden`";
        }
        return role.getAsMention();
    }
}
