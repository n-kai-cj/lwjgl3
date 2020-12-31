import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LaunchJoystick {

    public static void main(String[] args) {
        Logger.initialize();
        Platform.startup(() -> {
            try {
                final Stage stage = new Stage();
                final FXMLLoader loader = new FXMLLoader(LaunchJoystick.class.getResource("joystick.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(LaunchJoystick.class.getResource("joystick.css").toURI().toString());
                stage.setScene(scene);
                stage.showingProperty().addListener((obs, ov, nv) -> {
                    if (ov && !nv) {
                        Joystick joystick = loader.getController();
                        joystick.closeAction();
                    }
                });
                stage.setTitle("JoystickSample");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
