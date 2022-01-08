module com.example.task2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires javafx.swing;
    requires javafx.graphics;

    opens com.example.task2 to javafx.fxml;
    exports com.example.task2;
}