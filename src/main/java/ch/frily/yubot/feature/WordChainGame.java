package ch.frily.yubot.feature;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WordChainGame {

    /**
     * Regex pattern matches
     * <code>\p{L}</code> = NO Letter<br>
     * <code>\p{N}</code> = NO Number<br>
     * <code>\p{P}</code> = NO Punctuation
     */
    public static final String PATTERN = "[^\\p{IsLatin}\\p{N}\\p{P}]";

    /** Message deletion delay in seconds */
    private static final int DELAY = 8;

    /**
     * Handle a sent message
     * @param messageEvent
     */
    public static void handleWord(MessageReceivedEvent messageEvent) {
        Message message = messageEvent.getMessage();


        String validateWord = validateWord(messageEvent);
        if (validateWord != null) {
            log.info("Invalid word: {}", message.getContentRaw());
            message.delete().queue();

            long timestamp = Instant.now().plusSeconds(DELAY).toEpochMilli() / 1000;

            String infoMessage = String.format("""
                    %s dein Wort ist ungültig.
                    -# Grund: %s
                    -# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*
                    """, message.getAuthor().getAsMention(), validateWord, timestamp);
            message.getChannel().sendMessage(infoMessage).queue(
                    msg -> msg.delete().queueAfter(DELAY, java.util.concurrent.TimeUnit.SECONDS)
            );
        }
    }
    /**
     * Check if a message is valid for the word chain game
     * <p>
     * @param messageEvent
     * @return null if valid, otherwise a string with the reason why it is invalid
     */
    private static String validateWord(MessageReceivedEvent messageEvent) {
        List<Message> history = messageEvent.getChannel().getHistory().retrievePast(2).complete();

        if (history.size() >= 2) {
            Message previousMessage = history.get(1);
            Member previousMember = previousMessage.getMember();
            Member currentMember = messageEvent.getMember();

            if (previousMember.getIdLong() == currentMember.getIdLong()) {
                return "Du kannst nicht zweimal hintereinander ein Wort senden.";
            }
        }


        String message = messageEvent.getMessage().getContentRaw();
        if (message.isBlank()) {
            return "Du musst ein Wort eingeben.";
        }
        if (message.contains(":/")) {
            return "URLs sind nicht erlaubt.";
        }

        String containsAtLeastOneLetter = containsAtLeastOneLetter(message);
        if (containsAtLeastOneLetter != null) {
            return containsAtLeastOneLetter;
        }
        String checkCase = checkCase(message);
        if (checkCase != null) {
            return checkCase;
        }

        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return "Das Wort darf keine Leer- oder Sonderzeichen enthalten.";
    }

    private static String containsAtLeastOneLetter(String message) {
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.results().count() > 1) {
            return null;
        } else {
            return "Das Wort muss mindestens ein Buchstabe enthalten.";
        }
    }

    private static String checkCase(String message) {
        Pattern lowerCasePattern = Pattern.compile("[a-z]"); // gets every lowercase letter separately
        Pattern upperCasePattern = Pattern.compile("[A-Z]"); // uppercase separately
        Matcher lowerCaseMatcher = lowerCasePattern.matcher(message);
        Matcher upperCaseMatcher = upperCasePattern.matcher(message);
        if(lowerCaseMatcher.results().count() >= upperCaseMatcher.results().count()) {
            return null;
        } else {
            return "Du verwendest zu viele Großbuchstaben.";
        }
    }
}
