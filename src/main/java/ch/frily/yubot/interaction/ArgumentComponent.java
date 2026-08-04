package ch.frily.yubot.interaction;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a component capable of handling arguments.
 */
@Slf4j
public abstract class ArgumentComponent {
    protected final Map<String, String> arguments = new HashMap<>();

    /** Discord rejects a custom ID longer than this */
    public static final int MAX_COMPONENT_ID_LENGTH = 100;

    public abstract String getId();

    /**
     * Get the full ID of the component
     * <p>This method uses the defined-ID from {@link #getId()} and populates the arguments on the ID.
     * <pre><code>
     *     my-btn-id?arg1=value1&arg2=value2
     * </code></pre></p>
     * To retrieve and dispatch the component, use {@link #getId()} to find the correct component class.
     * @return
     */
    public String getFullIdentification(){
        StringBuilder id = new StringBuilder(getId());
        if (!arguments.isEmpty()) {
            id.append("?").append(arguments.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse(""));
        }
        id.append("#").append(ThreadLocalRandom.current().nextInt(1, 100));

        if (id.length() > MAX_COMPONENT_ID_LENGTH) {
            log.warn("Component ID '{}' is {} characters long but discord only allows {}. Shorten the argument keys or values.",
                    id, id.length(), MAX_COMPONENT_ID_LENGTH);
        }
        return id.toString();
    };

    public void addArgument(String key, String value){
        arguments.put(key, value);
    }

    public boolean hasArgument(String componentId, String key){
        return parseArguments(componentId).containsKey(key);
    }

    /**
     * Get an argument from the component ID
     * @param componentId
     * @param key
     * @return
     * @throws IllegalStateException if the argument could not be found
     */
    public String getArgument(String componentId, String key){
        String value = parseArguments(componentId).get(key);
        if (value == null) {
            throw new IllegalStateException(String.format("Argument '%s' konnte nicht gefunden werden.", key));
        }
        return value;
    }

    /**
     * Get every argument of a component ID as key-value pairs
     * <p>
     * Values are only split at their first <code>=</code>, so a value may contain that character itself.
     * @param componentId
     * @return The arguments, empty when the ID carries none
     */
    public static Map<String, String> parseArguments(String componentId){
        String args = extractArgs(componentId);
        if (args.isEmpty()) {
            return Map.of();
        }

        Map<String, String> parsedArguments = new LinkedHashMap<>();
        for (String arg : args.split("&")) {
            String[] keyValue = arg.split("=", 2);
            if (keyValue.length == 2 && !keyValue[0].isEmpty()) {
                parsedArguments.put(keyValue[0], keyValue[1]);
            }
        }
        return parsedArguments;
    }

    /**
     * Get the parameters between the <code>?</code> and the <code>#</code>
     * <p>
     * The ? defines the beginning of the arguments (as key-value)<br>
     * The # defines the end of the arguments and serves as an uniqueifier
     * </p>
     * @param componentId
     * @return The raw argument string, empty when the ID carries no arguments
     */
    private static String extractArgs(String componentId){
        int argsStart = componentId.indexOf('?');
        if (argsStart < 0) {
            return "";
        }
        return componentId.substring(argsStart + 1).split("#")[0];
    }

    public static String extractId(String componentId){
        return componentId.split("[?#]")[0];
    }
}
