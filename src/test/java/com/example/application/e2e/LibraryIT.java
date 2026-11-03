package com.example.application.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.IntegerFieldElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextFieldElement;
import org.vaadin.addons.dramafinder.element.UploadElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LibraryIT extends AbstractE2EIT {

    private static final String BOOK = "10 important facts about being a bird";

    private void openBook(String name) {
        loginPage().open().loginWithOneTimeToken("user");
        page.navigate(getUrl() + "/master-detail");
        page.locator("vaadin-grid-cell-content").getByText(name, new Locator.GetByTextOptions().setExact(true)).click();
        assertThat(page).hasURL(Pattern.compile("/master-detail/\\d+$"));
        TextFieldElement.getByLabel(page, "Name").assertValue(name);
    }

    @Test
    public void editAndSaveBook() {
        openBook(BOOK);
        IntegerFieldElement.getByLabel(page, "Pages").setValue("321");
        ButtonElement.getByText(page, "Save").click();
        NotificationElement.getByText(page, "Book saved").assertOpen();
        assertThat(page).hasURL(Pattern.compile("/master-detail$"));

        page.locator("vaadin-grid-cell-content").getByText(BOOK, new Locator.GetByTextOptions().setExact(true)).click();
        IntegerFieldElement.getByLabel(page, "Pages").assertValue("321");
    }

    @Test
    public void uploadCoverImage() throws IOException {
        openBook("10 important facts about creating software");
        Path png = Files.createTempFile("cover", ".png");
        ImageIO.write(new BufferedImage(40, 60, BufferedImage.TYPE_INT_RGB), "png", png.toFile());

        UploadElement.getByButtonText(page, "Upload File...").uploadFiles(png);
        // The preview is served by a DownloadHandler from the uploaded bytes
        assertThat(page.locator("vaadin-custom-field > img")).hasJSProperty("naturalWidth", 40);

        ButtonElement.getByText(page, "Save").click();
        NotificationElement.getByText(page, "Book saved").assertOpen();
    }
}
