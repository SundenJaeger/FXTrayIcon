package com.dustinredmond.fxtrayicon;

/*
 * Copyright (c) 2022 Dustin K. Redmond & contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.awt.*;
import java.util.StringJoiner;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;

class AWTUtils {

    /**
     * Converts a JavaFX MenuItem to a AWT MenuItem
     *
     * @param fxItem The JavaFX MenuItem
     * @return The converted AWT MenuItem
     * @throws UnsupportedOperationException If the user
     *                                       has called methods on the JavaFX MenuItem which are
     *                                       unable to be replicated using AWT or other means.
     */
    protected static MenuItem convertFromJavaFX(javafx.scene.control.MenuItem fxItem) {
        MenuItem awtItem;
        final String menuItemText = fxItem.getText() != null ? fxItem.getText() : "";

        if (fxItem instanceof CheckMenuItem) {
            CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(menuItemText);
            CheckMenuItem fxCheckItem = (CheckMenuItem) fxItem;

            // Set initial state
            checkboxMenuItem.setState(fxCheckItem.isSelected());

            // Sync from AWT to JavaFX when clicked
            checkboxMenuItem.addItemListener(e -> {
                boolean newState = checkboxMenuItem.getState();
                Platform.runLater(() -> {
                    // Only update if different to avoid infinite loops
                    if (fxCheckItem.isSelected() != newState) {
                        fxCheckItem.setSelected(newState);
                    }
                    // Always trigger the action
                    if (fxItem.getOnAction() != null) {
                        fxItem.getOnAction().handle(new ActionEvent());
                    }
                });
            });

            // Sync from JavaFX to AWT when property changes
            fxCheckItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (checkboxMenuItem.getState() != newValue) {
                    checkboxMenuItem.setState(newValue);
                }
            });

            awtItem = checkboxMenuItem;
        } else {
            awtItem = new MenuItem(menuItemText);

            // Set the onAction event for regular menu items
            if (fxItem.getOnAction() != null) {
                awtItem.addActionListener(e -> Platform.runLater(() ->
                        fxItem.getOnAction().handle(new ActionEvent())));
            }
        }

        // Validate unsupported features
//        validateSupportedFeatures(fxItem);

        // Disable the MenuItem if the FX item is disabled
        awtItem.setEnabled(!fxItem.isDisable());
        fxItem.disableProperty().addListener(e -> awtItem.setEnabled(!fxItem.isDisable()));

        fxItem.textProperty().addListener(e -> awtItem.setLabel(fxItem.getText()));

        return awtItem;
    }

    private static void validateSupportedFeatures(javafx.scene.control.MenuItem fxItem) {
        StringJoiner sj = new StringJoiner(",");
        if (fxItem.getGraphic() != null) {
            sj.add("setGraphic()");
        }
        if (fxItem.getAccelerator() != null) {
            sj.add("setAccelerator()");
        }
        if (!fxItem.getStyleClass().isEmpty()) {
            sj.add("setStyleClass()");
        }
        if (fxItem.getOnMenuValidation() != null) {
            sj.add("setOnMenuValidation()");
        }
        if (fxItem.getStyle() != null && !fxItem.getStyle().isEmpty()) {
            sj.add("setStyle()");
        }

        String errors = sj.toString();
        if (!errors.isEmpty()) {
            throw new UnsupportedOperationException(String.format(
                    "The following methods were called on the passed JavaFX MenuItem (%s), " +
                            "these methods are not supported by the tray icon.", errors));
        }
    }
}
