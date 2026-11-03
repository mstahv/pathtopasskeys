package com.example.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

    private String username;
    private String name;
    /**
     * Not used anymore, left only so that the old demo data still loads. Drop it
     * from your schema once all users have moved to passkeys.
     */
    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    /**
     * The random WebAuthn id of the user, created when registering the first passkey
     */
    @Column(columnDefinition = "bytea")
    private byte[] webAuthnId;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
    public byte[] getWebAuthnId() {
        return webAuthnId;
    }
    public void setWebAuthnId(byte[] webAuthnId) {
        this.webAuthnId = webAuthnId;
    }
}
