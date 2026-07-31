package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.IPermissionHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @deprecated This feature related permissionsystem was taken over by the slashcommand role-permission system and will be deleted
 */
@Deprecated
public record FeaturePermission(@NotNull String name, @NotNull List<IPermissionHolder> holders, String hint) {

}
