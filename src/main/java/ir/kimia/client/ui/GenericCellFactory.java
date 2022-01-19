package ir.kimia.client.ui;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

public class GenericCellFactory<T> implements Callback<T, TableCell> {

    ContextMenu menu;
    EventHandler click;
    String imageUrl;

    public GenericCellFactory(EventHandler click, ContextMenu menu, String imageUrl) {
        this.menu = menu;
        this.click = click;
        this.imageUrl = imageUrl;
    }


    public GenericCellFactory(EventHandler click, String imageUrl) {
        this(click, null, imageUrl);
    }

    public GenericCellFactory(EventHandler click, ContextMenu menu) {
        this(click, menu, null);
    }

    public TableCell call(T p) {
        TableCell cell = new TableCell() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                // calling super here is very important - don't skip this!
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.toString());
                }
                if (!empty) {
                    if (!StringUtils.isEmpty(imageUrl)) {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            Image removeImage = new Image(getClass().getResourceAsStream(imageUrl));
                            ImageView removeImageView = new ImageView(removeImage);
                            removeImageView.maxHeight(2);
                            removeImageView.maxWidth(2);
                            removeImageView.setFitHeight(25);
                            removeImageView.setFitWidth(25);
                            this.setGraphic(removeImageView);
                        }

                    }
                }
            }
        };

        // Right click
        if (menu != null) {
            cell.setContextMenu(menu);
        }
        // Double click
        if (click != null) {
            cell.setOnMouseClicked(click);
        }


        return cell;
    }
}