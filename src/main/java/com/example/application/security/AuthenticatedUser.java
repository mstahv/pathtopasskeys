package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecord;
import com.example.application.data.WebAuthnRecordRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@SessionScope
public class AuthenticatedUser {

    private static JsonMapper jm;
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;
    private final WebAuthnRecordRepository webAuthnRecordRepository;
    private final WebAuthnRelyingPartyOperations rpOperations;
    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository, UserCredentialRepository userCredentialRepository, WebAuthnRecordRepository webAuthnRecordRepository, WebAuthnRelyingPartyOperations rpOperations) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
        this.rpOperations = rpOperations;

        jm = JsonMapper.builder()
                .addModule((new WebauthnJacksonModule()))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    @Transactional
    public Optional<User> get() {
        try {
            return authenticationContext.getAuthenticatedUser(UserDetails.class)
                    .flatMap(userDetails -> userRepository.findByUsername(userDetails.getUsername()));

        } catch (ClassCastException e) {
            // In case of WebAuthn authentication, principal is PublicKeyCredentialUserEntity
            // See WebAuthnPublicKeyCredentialUserEntityRepositoryImpl
            return authenticationContext.getAuthenticatedUser(PublicKeyCredentialUserEntity.class)
                    .flatMap(userEntity -> userRepository.findByWebAuthnId(userEntity.getId().getBytes()));
        }
    }

    public void logout() {
        authenticationContext.logout();
    }

    public List<WebAuthnRecord> getPasskeys() {
        User user = get().get();
        return webAuthnRecordRepository.findByUser(user);
    }

    /**
     * Start the WebAuthn registration process for the current user. This method does pretty much
     * the same as Spring Security's default filters can do, but in a more SPA friendly way.
     *
     * @return a {@link CompletableFuture} that completes when the registration is done.
     */
    public CompletableFuture<Void> startWebAuthnRegistration() {
        // These contain the user identity and the relying party information
        publicKeyCredentialCreationOptions = this.rpOperations.createPublicKeyCredentialCreationOptions(
                new ImmutablePublicKeyCredentialCreationOptionsRequest(SecurityContextHolder.getContext().getAuthentication()));
        loadWebauthJs();
        return UI.getCurrent().getPage().executeJs("""
                            const label = "%s";
                            const creds = %s;
                            const pk = await window.register(creds, label);
                            return JSON.stringify(pk);
                            """.formatted(
                        LocalDateTime.now().toString(),
                        jm.writeValueAsString(publicKeyCredentialCreationOptions)))
                .toCompletableFuture(String.class)
                .thenAccept(publicKeyJson -> {
                    RelyingPartyPublicKey publicKey = jm.readValue(publicKeyJson, RelyingPartyPublicKey.class);
                    // this passkey negotiated with browser and the server now needs to be stored in the database
                    rpOperations.registerCredential(new ImmutableRelyingPartyRegistrationRequest(publicKeyCredentialCreationOptions, publicKey));
                });
    }

    public static void loadWebauthJs() {
        try {
            String string = new String(Objects.requireNonNull(AuthenticatedUser.class.getResourceAsStream("/webauthn_vf.js")).readAllBytes(), StandardCharsets.UTF_8);
            UI.getCurrent().getPage().executeJs(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void deletePasskey(WebAuthnRecord passkey) {
        webAuthnRecordRepository.delete(passkey);
    }
}
