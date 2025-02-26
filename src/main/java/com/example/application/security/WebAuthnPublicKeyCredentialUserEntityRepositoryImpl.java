package com.example.application.security;

import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.example.application.data.WebAuthnRecordRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * PublicKeyCredentialUserEntity is essentially a user object that is used by WebAuthn(4J).
 *
 * The purpose of PublicKeyCredentialUserEntityRepository is essentially to map between your
 * apps user domain objects and the WebAuthn id's assigned to users (PublicKeyCredentialUserEntity).
 * In the domain model you save the id (which is essentially a short byte array), to your user object.
 * <p>
 * Impl note, ideally in our example User would implement {@link PublicKeyCredentialUserEntity}, but its implementation
 * and this interface could be implemented byt UserService, but these are
 * is kind of tied to WebAuthn4J APIs and methods would collide with User domain object methods.
 * Now probably best to have this and UserCredentialRepository in a separate classes to keep
 * the example more readable.
 */
@Service
public class WebAuthnPublicKeyCredentialUserEntityRepositoryImpl implements PublicKeyCredentialUserEntityRepository {

    private final UserRepository userRepository;
    private final WebAuthnRecordRepository webAuthnRecordRepository;

    public WebAuthnPublicKeyCredentialUserEntityRepositoryImpl(UserRepository repository, WebAuthnRecordRepository webAuthnRecordRepository) {
        this.userRepository = repository;
        this.webAuthnRecordRepository = webAuthnRecordRepository;
    }

    @Override
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        //  Bytes is essentially just id with rat byte[], but SpringSecurity/WebAuthn4J uses
        //  their custom Bytes for some reason...
        Optional<User> byUsername = userRepository.findByWebAuthnId(id.getBytes());

        if (byUsername.isPresent()) {
            User user = byUsername.get();
            if (user.getWebAuthnId() == null) {
                return null;
            }
            // We can't make User directly implement PublicKeyCredentialUserEntity,
            // so we need to map it here.
            return new PublicKeyCredentialUserEntity() {
                @Override
                public String getName() {
                    return user.getUsername();
                }

                @Override
                public Bytes getId() {
                    return new Bytes(user.getWebAuthnId());
                }

                @Override
                public String getDisplayName() {
                    return user.getUsername();
                }
            };
        }
        return null;
    }

    @Override
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        Optional<User> byUsername = userRepository.findByUsername(username);

        if (byUsername.isPresent()) {
            User user = byUsername.get();
            if (user.getWebAuthnId() == null) {
                return null;
            }
            // Ideally User entity would implement this (less code would be needed) to handle different
            // types of principal types (this will be used as such when logging in with passkey, User otherwise)
            return new PublicKeyCredentialUserEntity() {
                @Override
                public String getName() {
                    return user.getUsername();
                }

                @Override
                public Bytes getId() {
                    return new Bytes(user.getWebAuthnId());
                }

                @Override
                public String getDisplayName() {
                    return user.getUsername();
                }
            };
        }
        return null;
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        // Creates a mapping from the user entity to the WebAuthn id
        Optional<User> byUsername = userRepository.findByUsername(userEntity.getName());
        if (byUsername.isPresent()) {
            User user = byUsername.get();
            user.setWebAuthnId(userEntity.getId().getBytes());
            userRepository.save(user);
        }
    }

    @Override
    public void delete(Bytes credentialId) {
        // No idea where this would be used, implemented anyways...
        webAuthnRecordRepository.findByCredentialId(credentialId.getBytes()).ifPresent(entity -> {
            webAuthnRecordRepository.delete(entity);
        });
        userRepository.findByWebAuthnId(credentialId.getBytes()).ifPresent(user -> {
            user.setWebAuthnId(null);
            userRepository.save(user);
        });
    }

}
