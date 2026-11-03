package com.example.application.security;

import com.example.application.data.Role;
import com.example.application.services.UserService;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;

/**
 * The login cookie only tells who the user is, this adds their roles from the
 * database, so that {@code @RolesAllowed("ADMIN")} works.
 * <p>
 * This runs for each request, so changes in roles take effect right away, and a
 * deleted user is no longer signed in. If the extra query per request matters
 * in your app, cache the roles.
 */
@ApplicationScoped
public class RolesAugmentor implements SecurityIdentityAugmentor {

    private final UserService userService;

    public RolesAugmentor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        // JPA is blocking, so off the I/O thread
        return context.runBlocking(() -> userService.findByUsername(identity.getPrincipal().getName())
                .<SecurityIdentity>map(user -> QuarkusSecurityIdentity.builder(identity)
                        .addRoles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                        .build())
                .orElseGet(() -> QuarkusSecurityIdentity.builder().setAnonymous(true).build()));
    }
}
