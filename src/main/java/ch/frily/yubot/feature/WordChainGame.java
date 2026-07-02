package ch.frily.yubot.feature;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
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
     * @param message
     */
    public static void handleWord(Message message) {
        String validateWord = validateWord(message.getContentRaw());
        if (validateWord != null) {
            log.info("Invalid word: {}", message.getContentRaw());
            message.delete().queue();

            long timestamp = Instant.now().plusSeconds(DELAY).toEpochMilli() / 1000;

            String infoMessage = String.format("""
                    %s dein Wort ist ungültig.
                    Bitte überprüfe, dass es nur ein Wort ist.
                    -# Grund: %s
                    -# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*
                    """, message.getAuthor().getAsMention(), validateWord, timestamp);
            message.getChannel().sendMessage(infoMessage).queue(
                    msg -> msg.delete().queueAfter(DELAY, java.util.concurrent.TimeUnit.SECONDS)
            );
        } else {
            log.info("Valid word: {}", message.getContentRaw());
        }
    }
    /**
     * Check if a message is valid for the word chain game
     * <p>
     * @param message
     * @return null if valid, otherwise a string with the reason why it is invalid
     */
    private static String validateWord(String message) {
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
        return "Dein Wort darf keine Leer- oder Sonderzeichen enthalten.";
    }

    private static String containsAtLeastOneLetter(String message) {
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.results().count() > 1) {
            return null;
        } else {
            return "Dein Wort muss mindestens ein Buchstabe enthalten.";
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
