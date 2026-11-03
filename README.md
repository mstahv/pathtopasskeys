# PathToPasskeys - the path

This branch shows, one commit at a time, how a "standard" Vaadin + Spring Boot + Spring Security
app with username-password login moves to passkeys. Each step is tagged, so you can check it out,
run it and compare it to the previous one:

| Tag | Step |
| --- | --- |
| [`path-1-password`](https://github.com/mstahv/pathtopasskeys/tree/path-1-password) | The starting point: an up-to-date stack (Vaadin 25, Spring Boot 4, JPA, PostgreSQL), but users log in with a username and a password. |
| [`path-2-one-time-tokens`](https://github.com/mstahv/pathtopasskeys/tree/path-2-one-time-tokens) | One-time tokens (a "magic link") as a fallback login. You probably have something similar already, e.g. for resetting passwords. ([diff](https://github.com/mstahv/pathtopasskeys/compare/path-1-password...path-2-one-time-tokens)) |
| [`path-3-passkeys`](https://github.com/mstahv/pathtopasskeys/tree/path-3-passkeys) | Passkeys in addition to passwords: users register passkeys in their profile and can then log in with them. ([diff](https://github.com/mstahv/pathtopasskeys/compare/path-2-one-time-tokens...path-3-passkeys)) |
| [`path-4-passwordless`](https://github.com/mstahv/pathtopasskeys/tree/path-4-passwordless) | Passwords are gone, only passkeys and one-time tokens remain. ([diff](https://github.com/mstahv/pathtopasskeys/compare/path-3-passkeys...path-4-passwordless)) |

The `main` branch continues from the last step with a more polished demo app.

## Running the application

Open the project in an IDE and run the `TestApplication` class from `src/test/java`. It starts the
app with a PostgreSQL database in a Testcontainer (Docker needed), populated with two users:
`user` and `admin`. While passwords are still around, their passwords are the same as their usernames.

The app opens at http://localhost:8081.

## Tests

Each step has browser tests for its login methods in `src/test/java/com/example/application/e2e`,
using [Playwright](https://playwright.dev/java/) and [DramaFinder](https://github.com/parttio/dramafinder)
page objects for the Vaadin components. Run them with:

```
mvn verify
```
