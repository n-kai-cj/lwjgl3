import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class Joystick implements Initializable {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    private final JoystickInputThread joystickInputThread = new JoystickInputThread();

    @FXML
    Button a_button;
    @FXML
    Button b_button;
    @FXML
    Button x_button;
    @FXML
    Button y_button;
    @FXML
    Button l1_button;
    @FXML
    Button r1_button;
    @FXML
    Button select_button;
    @FXML
    Button start_button;
    @FXML
    Button up_button;
    @FXML
    Button right_button;
    @FXML
    Button down_button;
    @FXML
    Button left_button;
    @FXML
    Circle left_circle;
    @FXML
    Circle left_stick;
    @FXML
    Circle right_circle;
    @FXML
    Circle right_stick;
    @FXML
    Rectangle l2_rectangle;
    @FXML
    Rectangle l2_stick;
    @FXML
    Rectangle r2_rectangle;
    @FXML
    Rectangle r2_stick;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.joystickInputThread.start();
    }

    public void closeAction() {
        this.joystickInputThread.stop();
    }

    private class JoystickInputThread implements Runnable {
        private boolean loopFlag = true;
        private final Thread thread = new Thread(this);

        void start() {
            this.thread.start();
        }

        void stop() {
            this.loopFlag = false;
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }

        private int initialize() {
            int ret = -1;
            if (!GLFW.glfwInit()) {
                return ret;
            }
            GLFW.glfwPollEvents();

            for (int i = 0; i <= GLFW.GLFW_JOYSTICK_LAST; i++) {
                if (!GLFW.glfwJoystickPresent(i)) {
                    log.warn("{} is not present", i);
                    continue;
                }
                log.info("[{}]: joystick_name={}, gamepad_name={}", i, GLFW.glfwGetJoystickName(i), GLFW.glfwGetGamepadName(i));
                ret = i;
                break;
            }
            return ret;
        }

        @Override
        public void run() {
            log.info("GLFW initialize start");
            int jid = initialize();
            while (this.loopFlag && jid < 0) {
                log.error("No Joystick is connected");
                sleep(5000);
                jid = initialize();
            }
            final ArrayList<Boolean> lastBtn = new ArrayList<>();
            final int left_circle_cx = (int) left_circle.getLayoutX();
            final int left_circle_cy = (int) left_circle.getLayoutY();
            final int right_circle_cx = (int) right_circle.getLayoutX();
            final int right_circle_cy = (int) right_circle.getLayoutY();
            final int l2_bottom = (int) l2_stick.getLayoutY();
            final int r2_bottom = (int) (r2_stick.getLayoutY());
            IntStream.range(0, 14).forEach(i -> lastBtn.add(false));
            log.info("loop start");
            while (this.loopFlag) {
                // get axes
                FloatBuffer fb = GLFW.glfwGetJoystickAxes(jid);
                assert fb != null;
                StringBuilder sb = new StringBuilder();
                int cnt = 0;
                sb.append(System.lineSeparator());
                ArrayList<Float> axesState = new ArrayList<>();
                sb.append("Axes: ");
                while (fb.hasRemaining()) {
                    float f = fb.get();
                    axesState.add(f);
                    sb.append(cnt++).append(":").append(f).append(", ");
                }
                sb.append(System.lineSeparator());
                sb.append("Buttons: ");
                // get buttons
                ByteBuffer bb = GLFW.glfwGetJoystickButtons(jid);
                assert bb != null;
                ArrayList<Boolean> btnState = new ArrayList<>();
                for (int i = 0; bb.hasRemaining(); i++) {
                    byte b = bb.get();
                    btnState.add(b == 1);
                    sb.append(i).append(":").append(b).append(", ");
                }

                // control left/right stick
                for (int i = 0; i < axesState.size() - 2; i += 2) {
                    if (i == 0) {
                        controlStick(left_stick, left_circle_cx, left_circle_cy, axesState.get(i), axesState.get(i + 1));
                    } else if (i == 2) {
                        controlStick(right_stick, right_circle_cx, right_circle_cy, axesState.get(i), axesState.get(i + 1));
                    }
                }

                // control L2/R2 axes
                for (int i = axesState.size() - 2; i < axesState.size(); i++) {
                    float val = axesState.get(i);
                    if (i == 4) {
                        Platform.runLater(() -> l2_stick.setLayoutY(l2_bottom - (val + 1.0f) * 30));
                    } else if (i == 5) {
                        Platform.runLater(() -> r2_stick.setLayoutY(r2_bottom - (val + 1.0f) * 30));
                    }
                }

                // control button press/release
                for (int i = 0; i < btnState.size(); i++) {
                    if (i == 0) {
                        buttonMouseEvent(a_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 1) {
                        buttonMouseEvent(b_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 2) {
                        buttonMouseEvent(x_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 3) {
                        buttonMouseEvent(y_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 4) {
                        buttonMouseEvent(l1_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 5) {
                        buttonMouseEvent(r1_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 6) {
                        buttonMouseEvent(select_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 7) {
                        buttonMouseEvent(start_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 10) {
                        buttonMouseEvent(up_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 11) {
                        buttonMouseEvent(right_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 12) {
                        buttonMouseEvent(down_button, lastBtn.get(i), btnState.get(i));
                    } else if (i == 13) {
                        buttonMouseEvent(left_button, lastBtn.get(i), btnState.get(i));
                    }
                }
                lastBtn.clear();
                lastBtn.addAll(btnState);
                log.trace(sb.toString());

                sleep(100);
            }
            log.info("loop end");
        }
    }

    private void controlStick(Circle circle, float cx, float cy, float x, float y) {
        Platform.runLater(() -> {
            circle.setLayoutX(cx + x * (float) 40.0);
            circle.setLayoutY(cy + y * (float) 40.0);
        });
    }

    private void buttonMouseEvent(Button button, boolean lastState, boolean currentState) {
        MouseEvent mousePress = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
        MouseEvent mouseRelease = new MouseEvent(MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
        Platform.runLater(() -> {
            if (!lastState && currentState) {
                MouseEvent.fireEvent(button, mousePress);
            } else if (lastState && !currentState) {
                MouseEvent.fireEvent(button, mouseRelease);
            }
        });
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
