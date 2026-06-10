package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

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
     * @param key permission-key defining what part of a feature this acts for
     * @param roles A list of permission holder (Roles, Members, ...)
     * @param hint A hint for the error message to tell the user why they can't execute it
     */
    void addPermission(String key, List<Role> roles, String hint) {
        permission.add(new FeaturePermission(resolveKey(key), roles, hint));
    }

    void addPermission(String key, List<Role> roles) {
        permission.add(new FeaturePermission(resolveKey(key), roles, null));
    }

    void addPermission(String key, Role... holders){
        addPermission(key, Arrays.stream(holders).toList());
    }

    void addPermission(String key, EnvKey... roleKeys) {
        List<Role> permissionHolders = Arrays.stream(roleKeys).map(envKey -> {
            if (envKey.name().startsWith("ROLE_")) {
                return EnvResolver.getRoleById(envKey);
            } else {
                throw new IllegalArgumentException("Key '" + envKey.name() + "' can not be resolved as a permission holder");
            }
        }).toList();
        addPermission(key, permissionHolders);
    }

    private String resolveKey(String key) {
        return getClass().getSimpleName() + "." + key;
    }

    public boolean isPermitted(String execution, Member member) {
        log.debug("Executing: {} ({})", execution, resolveKey(execution));
        log.debug("Possible options: {}", permission.stream().map(FeaturePermission::name).collect(Collectors.joining(", ")));
        FeaturePermission featurePerm = permission.stream().filter(featurePerms -> featurePerms.name().equals(resolveKey(execution))).findFirst().orElseThrow();
        log.debug(featurePerm.name());
        log.debug(featurePerm.roles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        log.debug(member.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        log.debug(String.valueOf(Util.containsAny(featurePerm.roles(), member.getRoles())));
        return Util.containsAny(featurePerm.roles(), member.getRoles());
    }
}
