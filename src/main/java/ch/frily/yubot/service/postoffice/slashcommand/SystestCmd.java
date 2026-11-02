package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reflections.Reflections.log;

public class SystestCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "systest";
    }

    @Override
    public String getDescription() {
        return "Systemtesting - Ephemeral execution state";
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    private static String firstGroup(Pattern p, String s, String group) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(group) : null;
    }

    private static final String TABLE = "ticket";
    private static final DateTimeFormatter SQL_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String sqlStr(String value) {
        return value == null ? "NULL" : "'" + value.replace("'", "''") + "'";
    }

    private static String sqlTs(LocalDateTime value) {
        return value == null ? "NULL" : "'" + value.format(SQL_TS) + "'";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
//        event.reply("no content").queue();

        List<String> statements = new ArrayList<>();

        Pattern USER_ID = Pattern.compile("<@!?(?<id>\\d{17,20})>");
        Pattern TIMESTAMP = Pattern.compile("<t:(?<ts>\\d+):F>");

        TextChannel archive = EnvKey.CHANNEL_TICKETLOGS.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER);
        String selfId = Client.getInstance().getClient().getSelfUser().getId();

        archive.getIterableHistory().forEach(message -> {
            if (!message.getAuthor().getId().equals(selfId)) {
                log.info("Message {} is not from YuBot", message.getId());
                return;
            }

            String ticketname = null, ownerId = null, assigneeId = null;
            TicketType ticketType = null;
            LocalDateTime createdAt = null;

            for (var component : message.getComponentTree().getComponents()) {
                if (!(component instanceof Container container)) continue;

                for (var child : container.getComponents()) {
                    if (!(child instanceof TextDisplay textDisplay)) continue;

                    for (String line : textDisplay.getContent().split("\n")) {
                        line = line.trim();

                        if (line.startsWith("### Ticket")) {
                            ticketname = line.replace("### Ticket", "").replace("wurde geschlossen", "").trim();
                        } else if (line.startsWith("**Kategorie**:")) {
                            String raw = line.replace("**Kategorie**:", "").replace("`", "").trim();
                            String label = raw.contains("/") ? raw.split("/", 2)[1].trim() : raw;
                            ticketType = Arrays.stream(TicketType.values())
                                    .filter(t -> t.getLabel().equals(label))
                                    .findFirst().orElse(null);
                        } else if (line.startsWith("**Erstellt von**:")) {
                            ownerId = firstGroup(USER_ID, line, "id");
                        } else if (line.startsWith("**Verantwortlich**:")) {
                            assigneeId = firstGroup(USER_ID, line, "id");
                        } else if (line.startsWith("**Geöffnet am**:")) {
                            String ts = firstGroup(TIMESTAMP, line, "ts");
                            if (ts != null) {
                                createdAt = LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(Long.parseLong(ts)),
                                        EnvKey.TIMEZONE.getAsZoneId());
                            }
                        }
                    }
                }
            }

            LocalDateTime archivedAt = message.getTimeCreated()
                    .atZoneSameInstant(EnvKey.TIMEZONE.getAsZoneId())
                    .toLocalDateTime();

            log.info("ticketname={}, type={}, owner={}, assignee={}, created={}, archived={}, msgId={}",
                    ticketname, ticketType, ownerId, assigneeId, createdAt, archivedAt, message.getId());

            if (ticketname == null) {
                log.warn("Message {} enthält keinen Ticketnamen, wird übersprungen", message.getId());
                return;
            }

            statements.add(String.format(
                    "INSERT INTO %s (name, owner_id, assignee_id, type, last_activity_at, is_request_pending, " +
                            "close_request_count, status, updated_at, is_reminder_sent, created_at, archived_at, archive_message_id) " +
                            "VALUES (%s, %s, %s, %s, %s, false, 42, 'ARCHIVED', %s, false, %s, %s, %s);",
                    TABLE,
                    sqlStr(ticketname),
                    sqlStr(ownerId),
                    sqlStr(assigneeId),
                    sqlStr(ticketType == null ? null : ticketType.name()),
                    sqlTs(archivedAt), // last_activity_at
                    sqlTs(archivedAt), // updated_at
                    sqlTs(createdAt),
                    sqlTs(archivedAt), // archived_at
                    sqlStr(message.getId())
            ));
        });

        Collections.reverse(statements); // message retrieve gets the newest first

        StringBuilder script = new StringBuilder("BEGIN;\n");
        statements.forEach(s -> script.append(s).append('\n'));
        script.append("COMMIT;");
        System.out.println(script);
    }
}
