package com.example.application.e2e;

import io.quarkus.test.junit.QuarkusTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@QuarkusTest
public class UserAdminIT extends AbstractE2EIT {

    private UserAdminPage userAdminPage() {
        return new UserAdminPage(page, getUrl());
    }

    @Test
    public void adminCreatesUserWhoCanSignInAndIsThenDeleted() {
        loginPage().open().loginWithOneTimeToken("admin");
        var admin = userAdminPage().open();
        admin.addUser("tester", "Test Person");
        profilePage().open().signOut();

        // The new user has no password or passkey, the one-time token gets them in
        loginPage().open().loginWithOneTimeToken("tester");
        profilePage().open().assertLoggedInAs("tester");
        profilePage().signOut();

        loginPage().open().loginWithOneTimeToken("admin");
        userAdminPage().open().deleteUser("tester");
    }

    @Test
    public void usersViewIsOnlyForAdmins() {
        loginPage().open().loginWithOneTimeToken("user");
        profilePage().open().assertLoggedInAs("user");
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Users"))).hasCount(0);
    }
}
