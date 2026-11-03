package com.example.application.security;

import com.example.application.services.UserService;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * The built-in registration endpoint of Quarkus registers a passkey for any
 * username. That is fine for signing up with a new username, but otherwise
 * anybody could add their passkey to somebody else's account. This check runs
 * before the endpoint: a passkey for an existing user can only be added by that
 * user, when signed in.
 */
@ApplicationScoped
public class PasskeyRegistrationGuard {

    private final UserService userService;

    public PasskeyRegistrationGuard(UserService userService) {
        this.userService = userService;
    }

    void guard(@Observes Router router) {
        // A negative order to run before the built-in endpoint (but after authentication)
        router.post("/q/webauthn/register").order(-1).handler(this::check);
    }

    private void check(RoutingContext ctx) {
        String username = ctx.queryParams().get("username");
        if (username != null && username.equals(currentUsername(ctx))) {
            ctx.next(); // a new passkey for yourself
            return;
        }
        // JPA is blocking, so not on the I/O thread
        ctx.vertx().executeBlocking(() -> username == null || userService.findByUsername(username).isEmpty())
                .onSuccess(newUsername -> {
                    if (newUsername) {
                        ctx.next(); // sign-up
                    } else {
                        ctx.response().setStatusCode(403).end();
                    }
                })
                .onFailure(ctx::fail);
    }

    private static String currentUsername(RoutingContext ctx) {
        return ctx.user() instanceof QuarkusHttpUser user && !user.getSecurityIdentity().isAnonymous()
                ? user.getSecurityIdentity().getPrincipal().getName()
                : null;
    }
}
