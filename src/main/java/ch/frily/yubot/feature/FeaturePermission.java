package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.IPermissionHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FeaturePermission(@NotNull String name, @NotNull List<IPermissionHolder> holders, String hint) {

}
