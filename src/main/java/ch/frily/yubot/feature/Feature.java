package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IPermissionHolder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Basic feature class<br>
 * Define feature-based permissions
 */
@Slf4j
public abstract class Feature {

    // execution-name, list of permissionHolder
    List<FeaturePermission> permission = new ArrayList<>();

    /**
     * Add a permission
     * @param execution permission-key defining what part of a feature this acts for
     * @param holder A list of permission holder (Roles, Members, ...)
     * @param hint A hint for the error message to tell the user why they can't execute it
     */
    void addPermission(String execution, List<IPermissionHolder> holder, String hint) {
        permission.add(new FeaturePermission(resolveName(execution), holder, hint));
    }

    void addPermission(String execution, List<IPermissionHolder> holder) {
        permission.add(new FeaturePermission(resolveName(execution), holder, null));
    }

    void addPermission(String execution, IPermissionHolder... holders){
        addPermission(execution, Arrays.stream(holders).toList());
    }

    void addPermission(String execution, EnvKey... holderKeys) {
        List<IPermissionHolder> permissionHolders = Arrays.stream(holderKeys).map(key -> {
            if (key.name().startsWith("ROLE_")) {
                return (IPermissionHolder) EnvResolver.getRoleById(key);
            } else {
                throw new IllegalArgumentException("Key '" + key.name() + "' can not be resolved as a permission holder");
            }
        }).toList();
        addPermission(execution, permissionHolders);
    }

    private String resolveName(String execution) {
        return getClass().getSimpleName() + "." + execution;
    }

    public boolean isPermitted(String execution, IPermissionHolder holder) {
        log.debug("Executing: {} ({})", execution, resolveName(execution));
        log.debug("Possible options: {}", permission.stream().map(FeaturePermission::name).collect(Collectors.joining(", ")));
        FeaturePermission featurePerm = permission.stream().filter(featurePerms -> featurePerms.name().equals(resolveName(execution))).findFirst().orElseThrow();
        log.debug(featurePerm.name());
        log.debug(String.valueOf(featurePerm.holders().contains(holder))); // false?!!
        return featurePerm.holders().contains(holder);
    }
}
