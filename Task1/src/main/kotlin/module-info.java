module com.example.task1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.desktop;
    requires kotlinx.coroutines.core;
    requires javafx.swing;


    opens com.example.task1 to javafx.fxml;
    exports com.example.task1;
}