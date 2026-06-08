package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static ch.frily.yubot.feature.Closure.buildContainer;

public class ClosureOpenCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Öffne alle Öffentlichen channels";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        event.getHook().editOriginalComponents(buildContainer(List.of(), true, false)).useComponentsV2().queue(hook -> {
            Closure.getInstance().openChannels(progressCategories ->
                            hook.editMessageComponents(buildContainer(progressCategories, true, false)).useComponentsV2().queue()
                    ).thenAccept(finalCategories -> {

                        hook.editMessageComponents(buildContainer(finalCategories, true, true)).useComponentsV2().queue();
                    })
                    .exceptionally(throwable -> {
                        throwable.getCause().printStackTrace();
                        return null;
                    });
        });
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
