package ch.frily.yubot.exception;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * For Consumers, use this Wrapper class to catch and handle exceptions.
 * <p></p>
 * When using async methods, exceptions can't be caught by the normal exception handler defined in {@link InteractionException}
 * or (at latest) in {@link ch.frily.yubot.Client}.
 * @param <T>
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;

    static <T> Consumer<T> wrap(@Nullable IReplyCallback event, ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                ExceptionHandler.handle(e, event);
            }
        };
    }
}
