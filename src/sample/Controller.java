package sample;
// add save settings

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.JFXFillTransition;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.util.Builder;
import javafx.util.Duration;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private TwoWaySerialComm twsc;
    @FXML
    private JFXButton initBtn;
    @FXML
    private JFXButton stopBtn;
    @FXML
    private JFXButton paramBtn;
    @FXML
    private JFXButton infoBtn;
    @FXML
    private JFXTextField textToSend;
    @FXML
    private WebView logWebView;
    @FXML
    private JFXComboBox<String> baudRateComBx;
    @FXML
    private JFXComboBox<String> parityComBx;
    @FXML
    private JFXComboBox<String> stopBitsComBx;
    @FXML
    private JFXComboBox<String> dataBitsComBx;
    @FXML
    private JFXComboBox<String> portComBx;
    @FXML
    private JFXButton applyBtn;
    @FXML
    private JFXButton refreshBtn;
    @FXML
    private Pane statusLed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set default parameters of GUI interface
        stopBtn.setDisable(true);
        paramBtn.setDisable(true);
        applyBtn.setDisable(true);
        refreshBtn.setDisable(true);
        textToSend.setDisable(true);

        baudRateComBx.getItems().addAll(
                "110",
                "300",
                "600",
                "1200",
                "2400",
                "4800",
                "9600",
                "19200",
                "38400",
                "57600",
                "115200",
                "230400",
                "460800",
                "921600"
        );

        parityComBx.getItems().addAll(
                "PARITY_NONE",
                "PARITY_ODD",
                "PARITY_EVEN",
                "PARITY_MARK",
                "PARITY_SPACE"
        );

        stopBitsComBx.getItems().addAll(
                "STOPBITS_1",
                "STOPBITS_2",
                "STOPBITS_1.5"
        );

        dataBitsComBx.getItems().addAll(
                "DATABITS_5",
                "DATABITS_6",
                "DATABITS_7",
                "DATABITS_8"
        );

        portComBx.getItems().addAll(
                "/dev/ttyUSB0"
        );
        portComBx.setEditable(true);
        portComBx.getSelectionModel().select(0);

    }

    @FXML
    public void initBtnCliked() {
        // if twsc object wasn't initialized create a new TwoWaySerialComm object
        if(twsc == null ){
            try {
                twsc = new TwoWaySerialComm(portComBx.getSelectionModel().getSelectedItem(), logWebView.getEngine() );
                refreshBtn_Clicked();
                new Thread(() -> {
                    try {
                        stopBtn.setDisable(false);
                        initBtn.setDisable(true);
                        paramBtn.setDisable(false);
                        applyBtn.setDisable(false);
                        refreshBtn.setDisable(false);
                        new JFXFillTransition(Duration.millis(500),statusLed,Color.RED,Color.GREEN).play();
                        textToSend.setDisable(false);
                        twsc.thread.join(); // wait for stop thread (twsc.thread => twsc.SerialRefreshStatus.thread)
                        stopBtn.setDisable(true);
                        initBtn.setDisable(false);
                        paramBtn.setDisable(true);
                        applyBtn.setDisable(true);
                        refreshBtn.setDisable(true);
                        new JFXFillTransition(Duration.millis(500),statusLed,Color.GREEN,Color.RED).play();
                        textToSend.setDisable(true);
                        twsc = null;
                    } catch (InterruptedException e) {
                        System.out.println("Error: " + e);
                    }
                }).start();
            } catch( Exception e ) {
                e.printStackTrace();
                showErrorAlert("Connect error","The requested Port does not exist", "Check your connection and try again.", e);
            }
        }
    }

    @FXML
    public void infoBtnClicked(){

    }

    @FXML
    public void paramBtnClicked(){
        twsc.showSerialCommParam(); // display all information in console about connection parameters
    }

    @FXML
    public void stopBtnClicked() {
        if(twsc != null) {  // if twsc object wasn't initialized
            twsc.kill();    // stop all thread in twsc object
            twsc = null;    // delete twsc object
        }
    }

    @FXML
    public void applyBtn_Clicked(){
        twsc.setBaudRate( Integer.valueOf( baudRateComBx.getSelectionModel().getSelectedItem() ) );
        twsc.setParity( parityComBx.getSelectionModel().getSelectedIndex() );
        twsc.setStopBits( stopBitsComBx.getSelectionModel().getSelectedIndex()+1 );
        twsc.setDataBit( dataBitsComBx.getSelectionModel().getSelectedIndex()+5);
        twsc.showSerialCommParam();
    }

    @FXML
    public void refreshBtn_Clicked(){
        baudRateComBx.getSelectionModel().select(twsc.getBaudRateIndex());
        parityComBx.getSelectionModel().select(twsc.getParityIndex());
        stopBitsComBx.getSelectionModel().select(twsc.getStopBitsIndex()-1);
        dataBitsComBx.getSelectionModel().select(twsc.getDataBits()-5);
        twsc.showSerialCommParam();
    }

    public void formClose() {
        stopBtnClicked();   // stop all thread in twsc object and delete this object
    }

    public void txtFieldKeyPressedEvent(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendSerialText(textToSend.getText());
            textToSend.clear();
        }
    }

    synchronized private void sendSerialText(String text) {
        twsc.sendText(text);
    }

    private void showErrorAlert(String title, String header, String content, Exception moreDetails){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        moreDetails.printStackTrace(pw);

        alert.getDialogPane().setPrefSize(500,300);
        TextArea area = new TextArea(sw.toString());
        alert.getDialogPane().setExpandableContent(area);

        alert.showAndWait();
    }

}


