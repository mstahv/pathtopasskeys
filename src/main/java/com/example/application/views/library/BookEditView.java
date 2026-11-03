package com.example.application.views.library;

import com.example.application.data.SampleBook;
import com.example.application.services.SampleBookService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.OptimisticLockException;

/**
 * Editor for a single book, shown in the detail area of {@link LibraryView}.
 */
@PageTitle("Library")
@Route(value = "master-detail/:" + BookEditView.BOOK_ID, layout = LibraryView.class)
@PermitAll
public class BookEditView extends VerticalLayout implements BeforeEnterObserver {

    static final String BOOK_ID = "bookId";

    private final SampleBookService service;

    // Bound to SampleBook properties by name
    private final ImageField image = new ImageField();
    private final TextField name = new TextField("Name");
    private final TextField author = new TextField("Author");
    private final DatePicker publicationDate = new DatePicker("Publication date");
    private final IntegerField pages = new IntegerField("Pages");
    private final TextField isbn = new TextField("ISBN");

    private final BeanValidationBinder<SampleBook> binder = new BeanValidationBinder<>(SampleBook.class);
    private SampleBook book;

    public BookEditView(SampleBookService service) {
        this.service = service;
        binder.bindInstanceFields(this);
        addAndExpand(new Scroller(new FormLayout(image, name, author, publicationDate, pages, isbn) {{
            setExpandColumns(true);
            setExpandFields(true);
        }}) {{
            setWidthFull();
        }});
        add(new HorizontalLayout(
                new Button("Save", e -> save()) {{
                    addThemeVariants(ButtonVariant.PRIMARY);
                }},
                new Button("Cancel", e -> LibraryView.showList())));
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().getLong(BOOK_ID).flatMap(service::get).ifPresentOrElse(
                found -> {
                    book = found;
                    binder.readBean(book);
                },
                () -> {
                    Notification.show("The requested book was not found");
                    event.forwardTo(LibraryView.class);
                });
    }

    private void save() {
        try {
            binder.writeBean(book);
            service.save(book);
            Notification.show("Book saved");
            LibraryView.showList();
        } catch (ValidationException e) {
            Notification.show("Check the highlighted fields");
        } catch (OptimisticLockException e) {
            Notification.show("Somebody else has updated the book while you were editing it.")
                    .addThemeVariants(NotificationVariant.ERROR);
        }
    }

    /**
     * The cover image of a book as a field, so it binds like any other property.
     */
    static class ImageField extends CustomField<byte[]> {
        private final BookImage preview = new BookImage(null) {{
            setMaxWidth("100%");
            setMaxHeight("240px");
        }};
        private final Upload upload = new Upload();
        private byte[] content;

        ImageField() {
            setLabel("Image");
            upload.setAcceptedFileTypes("image/*");
            upload.setMaxFiles(1);
            upload.setUploadHandler(UploadHandler.inMemory((metadata, data) -> {
                // Called with the UI locked, so the component can be updated directly
                content = data;
                preview.setContent(data);
                updateValue();
                upload.clearFileList();
            }));
            add(preview, upload);
        }

        @Override
        protected byte[] generateModelValue() {
            return content;
        }

        @Override
        protected void setPresentationValue(byte[] newValue) {
            content = newValue;
            preview.setContent(newValue);
            upload.clearFileList();
        }
    }
}
