package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Basic feature class<br>
 * Define feature-based permissions
 */
@Slf4j
public abstract class Feature {

    // permission-key, list of roles, hint
    List<FeaturePermission> permission = new ArrayList<>();

    /**
     * Add a permission
     * @param key permission-key defining what part of a feature this acts for<br>
     *            Mostly that is the action name, such as <code>send</code>. When it's for the whole feature, <code>*</code> is recommended.
     * @param roleKeys A list of role-reference keys that are allowed to execute the key-reference
     * @param hint A hint for the error message to tell the user why they can't execute it
     */
    void addPermission(String key, List<EnvKey> roleKeys, String hint) {
        List<Role> permissionHolders = roleKeys.stream().map(envKey -> {
            if (envKey.name().startsWith("ROLE_")) {
                return EnvResolver.getRoleById(envKey);
            } else {
                throw new IllegalArgumentException("Key '" + envKey.name() + "' can not be resolved as a permission holder");
            }
        }).toList();
        permission.add(new FeaturePermission(resolveKey(key), permissionHolders, hint));
    }

    /**
     * Resolve the permission key
     * @param key
     * @return <code>class.key</code>
     */
    private String resolveKey(String key) {
        return getClass().getSimpleName() + "." + key;
    }

    /**
     * Check whether a member is permitted to execute an action
     * @param key The key representing the defined permission
     * @param member The member to check for permission
     * @return
     */
    public boolean isPermitted(String key, Member member) {
        FeaturePermission featurePerm = permission.stream().filter(featurePerms -> featurePerms.name().equals(resolveKey(key))).findFirst().orElseThrow();
        return Util.containsAny(featurePerm.roles(), member.getRoles());
    }

    /**
     * Check whether a member is permitted to execute an action
     * @param key The key representing the defined permission
     * @param member The member to check for permission
     * @throws PermissionException If the member is not permitted, it throws a permission exception
     */
    public void isPermittedElseThrow(String key, Member member) throws PermissionException {
        FeaturePermission featurePerm = permission.stream().filter(featurePerms -> featurePerms.name().equals(resolveKey(key))).findFirst().orElseThrow();
        if (Util.containsAny(featurePerm.roles(), member.getRoles())) {
            return;
        }
        throw new PermissionException(featurePerm.hint());
    }
}
