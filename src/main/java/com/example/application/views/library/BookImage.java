package com.example.application.views.library;

import com.vaadin.flow.component.html.Image;

/**
 * A book cover from bytes, served with a DownloadHandler. Hidden when there is no image.
 */
class BookImage extends Image {

    BookImage(byte[] content) {
        setAlt("Cover");
        setContent(content);
    }

    void setContent(byte[] content) {
        setVisible(content != null);
        if (content != null) {
            setSrc(event -> {
                event.setContentType("image/jpeg"); // browsers detect the actual image format
                event.getOutputStream().write(content);
            });
        }
    }
}
