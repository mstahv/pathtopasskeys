package com.example.application.security;

import com.example.application.services.UserService;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.regex.Pattern;

/**
 * Logs the user in with a one-time token from {@link OneTimeTokens}.
 * <p>
 * The link sent to the user opens a page with a button that posts the token.
 * Using up the token already when the link is opened would be simpler, but
 * email clients and virus scanners may open links before the user does.
 * <p>
 * The login itself is the same cookie a passkey login sets, by
 * {@link WebAuthnSecurity#rememberUser}, so the rest of the app doesn't need to
 * care how the user signed in.
 */
@ApplicationScoped
public class OneTimeTokenLogin {

    public static final String PATH = "/login/ott";

    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_-]{1,100}");

    private static final String PAGE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Sign in</title>
              <style>body { font-family: system-ui, sans-serif; text-align: center; margin-top: 20vh; }</style>
            </head>
            <body>
              <h1>Sign in to PathToPasskeys</h1>
              <form method="post" action="%s">
                <input type="hidden" name="token" value="%s">
                <button type="submit">Sign in</button>
              </form>
            </body>
            </html>
            """;

    private final OneTimeTokens tokens;
    private final UserService userService;
    private final WebAuthnSecurity security;

    public OneTimeTokenLogin(OneTimeTokens tokens, UserService userService, WebAuthnSecurity security) {
        this.tokens = tokens;
        this.userService = userService;
        this.security = security;
    }

    void routes(@Observes Router router) {
        router.get(PATH).handler(this::page);
        router.post(PATH).handler(BodyHandler.create()).handler(this::login);
    }

    private void page(RoutingContext ctx) {
        String token = ctx.queryParams().get("token");
        if (token == null || !TOKEN.matcher(token).matches()) {
            ctx.redirect("/login?error");
            return;
        }
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .end(PAGE.formatted(PATH, token));
    }

    private void login(RoutingContext ctx) {
        String token = ctx.request().getFormAttribute("token");
        // Checking that the user exists is JPA, which is blocking, so not on the I/O thread
        Uni.createFrom().item(() -> tokens.consume(token)
                        .filter(username -> userService.findByUsername(username).isPresent()))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribe().with(username -> {
                    if (username.isPresent()) {
                        security.rememberUser(username.get(), ctx);
                        ctx.redirect("/");
                    } else {
                        ctx.redirect("/login?error");
                    }
                }, ctx::fail);
    }
}
