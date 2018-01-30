package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("SerialCommGUI");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(300);
        primaryStage.show();

        // FORM CLOSE EVENT
        primaryStage.setOnCloseRequest( e-> controller.formClose());

    }

    public static void main(String[] args) {
        launch(args);
    }
}
