# PathToPasskeys, Quarkus edition

Passwordless login with passkeys for a [Vaadin](https://vaadin.com) app on
[Quarkus](https://quarkus.io). It's the same app as on the
[main branch](https://github.com/mstahv/pathtopasskeys), which uses Spring Boot
and Spring Security, just built on Quarkus instead:

 * Users sign in with **passkeys**, with **one-time tokens** ("magic links") as a fallback
 * Anybody can **sign up** with a passkey (and gets the USER role)
 * Users register and delete their passkeys in the Profile view
 * Admins can add and delete users
 * Views are protected with the standard `@AnonymousAllowed`, `@PermitAll` and
   `@RolesAllowed` annotations
 * JPA (with Panache repositories) on PostgreSQL

## Running the app

You need Java 21+ and Docker (or Podman). Start the app in development mode:

    mvn

Quarkus [Dev Services](https://quarkus.io/guides/dev-services) start PostgreSQL
in a container and load the demo data from `import.sql`. Open
http://localhost:8080 and sign in as `user` or `admin`: first with a one-time
token, then register a passkey in the Profile view, sign out, and sign back in
with the passkey.

## How it works

The [WebAuthn extension](https://quarkus.io/guides/security-webauthn) does the
heavy lifting. Compared to the Spring Security version:

 * **Login, logout and registration** use the built-in endpoints of the
   extension (`/q/webauthn/login`, `/q/webauthn/logout`, `/q/webauthn/register`).
   The browser side is the `webauthn.js` served by Quarkus, called from Java in
   `WebAuthnJs`.
 * **The login is an encrypted cookie**, not a part of the HTTP session.
   Quarkus checks it on each request, and `RolesAugmentor` adds the roles of the
   user from the database.
 * **Passkeys are stored** with JPA in `WebAuthnRecord`, through
   `PasskeyUserProvider`. Quarkus needs only a few fields of them. A passkey for
   a new username creates the user too, that's the sign-up.
 * **Registration is guarded:** the built-in endpoint would register a passkey
   for any username, also for an existing user. `PasskeyRegistrationGuard` runs
   before it and allows that only for the user themself, when signed in.
 * **One-time tokens** are not built into Quarkus, but they are simple:
   `OneTimeTokens` creates and checks them, and `OneTimeTokenLogin` turns a
   token into the same login cookie that a passkey login sets. The link opens a
   page with a "Sign in" button, as email clients and virus scanners may open
   links before the user does. `LoginLinkSender` is where a real app sends the
   link by email or SMS; this demo just shows it.
 * **Push and background tasks:** Quarkus knows the user only during an HTTP
   request, so in `UI.access()` from a background thread the `SecurityIdentity`
   is not available. `AuthenticatedUser` remembers the user in the
   `VaadinSession` for these cases, see the "Background task" view.
 * **View access control** is Vaadin's `NavigationAccessControl`, the same one
   that Spring Boot apps use. In Quarkus it is enabled with a few lines in
   `NavigationAccessControlSetup`.

## Running the E2E tests

The tests are Playwright tests, using the
[DramaFinder](https://github.com/parttio/dramafinder) page objects for Vaadin
components. They start the app with a database in a container (Docker needed)
and drive a Chromium browser, with a virtual authenticator for the passkeys:

    mvn test

## Production

Build with `mvn package` and run with `java -jar target/quarkus-app/quarkus-run.jar`.
No production profile is needed: the Vaadin Quarkus extension builds the
frontend for production itself, and the Vaadin development tools are in the
`test` scope, which Quarkus uses only in dev mode and tests.
Passkeys only work over HTTPS (localhost is an exception), on the domain they
were registered for. See `application.properties` for what to configure: the
domain, the database and the key that encrypts the login cookie.
