package ch.frily.yubot.interaction;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a component capable of handling arguments.
 */
public abstract class ArgumentComponent {
    protected final Map<String, String> arguments = new HashMap<>();

    public abstract String getId();

    /**
     * Get the full ID of the component
     * <p>This method uses the defined-ID from {@link #getId()} and populates the arguments on the ID.
     * <pre><code>
     *     my-btn-id?arg1=value1&arg2=value2
     * </code></pre></p>
     * To retrieve and dispatch the component, use {@link getId()} to find the correct component class.
     * @return
     */
    public String getFullIdentification(){
        String idWithArgs = getId() + "?" + arguments.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
        String uniqueifier = "#" + ThreadLocalRandom.current().nextInt(1, 100);
        return idWithArgs + uniqueifier;
    };

    public void addArgument(String key, String value){
        arguments.put(key, value);
    }

    public boolean hasArgument(String componentId, String key){
        String arguments = extractArgs(componentId);
        Optional<String> searchedKeyValue = Arrays.stream(arguments.split("&"))
                .filter(arg -> arg.startsWith(key + "=")).findFirst();
        return searchedKeyValue.isPresent();
    }

    /**
     * Get an argument from the component ID
     * @param componentId
     * @param key
     * @return
     * @throws IllegalStateException if the argument could not be found
     */
    public String getArgument(String componentId, String key){
        String arguments = extractArgs(componentId);
        Optional<String> searchedKeyValue = Arrays.stream(arguments.split("&"))
                .filter(arg -> arg.startsWith(key + "=")).findFirst();
        if (searchedKeyValue.isPresent()) {
            return searchedKeyValue.get().split("=")[1];
        } else {
            throw new IllegalStateException(String.format("Argument '%s' konnte nicht gefunden werden.", key));
        }
    }

    /**
     * Get the parameters between the <code>?</code> and the <code>#</code>
     * <p>
     * The ? defines the beginning of the arguments (as key-value)<br>
     * The # defines the end of the arguments and serves as an uniqueifier
     * </p>
     * @param componentId
     * @return
     */
    private static String extractArgs(String componentId){
        return componentId.split("\\?")[1].split("#")[0];
    }

    public static String extractId(String componentId){
        return componentId.split("\\?")[0];
    }
}
