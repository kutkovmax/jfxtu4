module ru.kutkovmax.jfxtu4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    opens ru.kutkovmax.jfxtu4 to javafx.fxml, javafx.graphics;
    opens ru.kutkovmax.jfxtu4.controller to javafx.fxml, javafx.graphics;
    opens ru.kutkovmax.jfxtu4.view to javafx.fxml, javafx.graphics;
    opens ru.kutkovmax.jfxtu4.model to javafx.fxml;

    exports ru.kutkovmax.jfxtu4;
    exports ru.kutkovmax.jfxtu4.controller;
    exports ru.kutkovmax.jfxtu4.model;
    exports ru.kutkovmax.jfxtu4.view;
}
