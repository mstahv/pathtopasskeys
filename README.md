# PathToPasskeys - an example to get rid of ancient username-password authentication

This projects shows how to derive a "standard app" (Spring Boot, Spring Security, Vaadin, JPA, PostgreSQL and username-password style authentication) to utilize passkeys, because that's what
we should only use in 2025. Even if your passwords would be correctly hashed and salted in the DB, the greatest problem of password based logins, the user, is hard to get rid of.

The final step in the project no more supports username-password authentication, but only passkeys and one time tokens as a fallback. When modernizing your app, you probably wish to support them both for a while. See the [commit timeline for the steps I did](https://github.com/mstahv/pathtopasskeys/commits/main/) (and check out them if you want to inspect them locally in IDE). Roughly the steps were:

 * A dummy project downloaded from start.vaadin.com
 * Same sanitations like using TestContainers for easier development/testing and added a Profile view to e.g. change the password.
 * Add OTT (one time token) support to the project. There is a high chance you have this already e.g. to reset passwords. If you are starting from scratch today, the optimal setup is probably to have only passkeys and OTT support as a fallback (and for user registration process).
   * Add DB table for storing OTTs and map them to the user.
   * Add required Spring Security beans and configuration.
 * Add WebAuthn support to the project.
   *  Add DB table for storing passkeys and map them to the user (this example uses JPA, see "Storing passkeys" below for a ready-made alternative).
   *  Add required Spring Security configuration. Spring Security provides the HTTP endpoints for both passkey registration and login, the app only needs a bit of JavaScript to call them from Vaadin UI (see `WebAuthnJs` and `webauthn_vf.js`).
   *  Added functionality to Profile view to register passkeys (you can have many of course) and to remove them.
   * Added a button to the login view to initiate login using a passkey.
 * Remove username-password authentication. The superclass of LoginView chages altogether as it designed for "form based login" only. This is the final step in the project. You might want to keep it for a while, but in the end, you should get rid of them for simplicity!

See also my [year-old post about passkeys](https://vaadin.com/blog/forget-passwords-accessing-webauthn-api-with-vaadin). Compared to that example, this now utilises Spring Security and WebAuthn4J (this was not supported year ago) instead of directly implementing the WebAuthn with Yubico's library.

## Storing passkeys

By default, Spring Security keeps passkeys in memory, which is never what you want: they are lost on restart and don't work in a cluster. You need to provide implementations of two interfaces:

 * `UserCredentialRepository` stores the credential records (the actual passkeys, a user can have several).
 * `PublicKeyCredentialUserEntityRepository` maps your users to their WebAuthn user id.

This example implements them with JPA (`WebAuthnUserCredentialRepositoryImpl`, `WebAuthnPublicKeyCredentialUserEntityRepositoryImpl` and the `WebAuthnRecord` entity), so that passkeys live next to the rest of the domain model.

**If it fits your architecture, use the ready-made implementations instead.** Since Spring Security 6.5, there are `JdbcUserCredentialRepository` and `JdbcPublicKeyCredentialUserEntityRepository`, with table definitions shipped in the `spring-security-web` jar (`org/springframework/security/user-credentials-schema.sql`, `user-credentials-schema-postgres.sql` and `user-entities-schema.sql`). Then the passkey storage is just two beans:

```java
@Bean
UserCredentialRepository userCredentialRepository(JdbcOperations jdbc) {
    return new JdbcUserCredentialRepository(jdbc);
}

@Bean
PublicKeyCredentialUserEntityRepository userEntityRepository(JdbcOperations jdbc) {
    return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
}
```

## Running the application

Open the project in an IDE. Once opened in the IDE, locate the `TestApplication` class (from `src/test/java`) and run the main method using "Debug". This will start the Spring Boot application and initialize PostgreSQL database using TestContainers.

The app opens to http://localhost:8080. You can login with OTT method first with "user" or "admin" and then register passkeys to them. After that, you can login with passkeys.

TIP: You can use [the EntityExplorer](https://github.com/viritin/entityexplorer) mapped to http://localhost:8080/entityexplorer/ in test runs to inspect the database state during the hacking (development time tooling so no access control configured for that). Note that even though the last step don't support username-password authentication anymore, I left them to the DB because I'm lazy and didn't want to create a new set of test data dump 🤓

## Running the E2E tests

Browser tests for the basic flows (access control, one-time token login, passkey registration, passkey login, sign out) are in `src/test/java/com/example/application/e2e`. They use [Playwright](https://playwright.dev/java/) with Chromium's virtual WebAuthn authenticator in place of a real passkey, and [DramaFinder](https://github.com/parttio/dramafinder) page objects for the Vaadin components. Docker is needed for the PostgreSQL Testcontainer.

```
mvn verify
```

Add `-Dheadless=false` to watch the browser.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorial at [vaadin.com/docs/latest/tutorial/overview](https://vaadin.com/docs/latest/tutorial/overview).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/docs/latest/components](https://vaadin.com/docs/latest/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes). 
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Forum](https://vaadin.com/forum).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).
