package org.be.desktopjavafx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
// COntroller xuw ly logic ui
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
