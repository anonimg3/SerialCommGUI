//add clearLogList();
package sample;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TwoWaySerialComm {
    private SerialWriter serialWriter;
    private SerialReader serialReader;
    private WebEngine we;
    private StringBuilder html;
    private ArrayList<String> logList;
    private boolean run;
    private SerialPort serialPort;
    private int baudRate;
    private String portName;
    private int dataBits;
    private int stopBits;
    private int parity;
    public Thread thread;


    public TwoWaySerialComm(String portName, WebEngine we)  throws Exception{
        this.html = new StringBuilder();
        this.run = true;
        this.logList = new ArrayList<String>();
        this.we = we;
        this.baudRate = 115200;
        this.portName = portName;
        this.dataBits = 8;
        this.stopBits = 1;
        this.parity = 0; //parity none
        addTemplateCSS();

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );

        if( portIdentifier.isCurrentlyOwned() ) {
            addStatus( "Error: Port is currently in use" );
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );

            if( commPort instanceof SerialPort ) {
                serialPort = ( SerialPort )commPort;
                serialPort.setSerialPortParams( baudRate, dataBits, stopBits, parity );

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();

                ( new Thread( serialWriter = new SerialWriter( out ) ) ).start();
                ( new Thread( serialReader = new SerialReader( in ) ) ).start();
                ( thread = new Thread( new SerialRefreshStatus() ) ).start();
                serialReader.setStatusList( logList );
                serialWriter.setStatusList( logList );
                addStatus("Serial port is ready");
            } else {
                System.out.println( "Error: Only serial ports are handled by this example." );
            }

        }
    }

    public void setBaudRate(int baudRate) {
    // 110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600
        this.baudRate = baudRate;
        setSerialCommParam();
    }


    public int getBaudRate(){
        return serialPort.getBaudRate();
    }

    public void setDataBit(int dataBits){
    // DATABITS_5, DATABITS_6, DATABITS_7, or DATABITS_8
        if(dataBits == 5 || dataBits == 6 || dataBits == 7 || dataBits == 8){
            this.dataBits = dataBits;
            setSerialCommParam();
        }else{
            addStatus("Error: wrong data bits. Default is 8");
        }
    }

    public int getDataBits(){
        return serialPort.getDataBits();
    }

    public void setStopBits(int stopBits){
    // STOPBITS_1, STOPBITS_2, or STOPBITS_1_5
        if(stopBits == 1 || stopBits == 2 || stopBits == 3){
            this.stopBits = stopBits;
            setSerialCommParam();
        }else{
            addStatus("Error: wrong stop bits. Default is 1");
        }
    }

    public int getStopBitsIndex(){
        return serialPort.getStopBits();
    }

    public String getStopBits(){
        String returnStopBits;

        switch( serialPort.getStopBits() ){
            case 1:
                returnStopBits = "STOPBITS_1";
                break;
            case 2:
                returnStopBits = "STOPBITS_2";
                break;
            case 3:
                returnStopBits = "STOPBITS_1.5";
                break;
            default:
                returnStopBits = "STOP_BITS_ERROR";
                break;
        }
        return returnStopBits;
    }

    public void setParity(int parity){
        // PARITY_NONE 0, PARITY_ODD 1, PARITY_EVEN 2, PARITY_MARK 3 or PARITY_SPACE 4
        if(parity == 0 || parity == 1 || parity == 2 || parity == 3 || parity == 4){
            this.parity = parity;
            setSerialCommParam();
        }else{
            addStatus("Error: wrong parity. Default is parity none");
        }
    }

    public String getParity(){
        String returnParity;

        switch( serialPort.getParity() ){
            case 0:
                returnParity = "PARITY_NONE";
                break;
            case 1:
                returnParity = "PARITY_ODD";
                break;
            case 2:
                returnParity = "PARITY_EVEN";
                break;
            case 3:
                returnParity = "PARITY_MARK";
                break;
            case 4:
                returnParity = "PARITY_SPACE";
                break;
            default:
                returnParity = "PARITY_ERROR";
                break;
        }
        return returnParity;
    }

    public int getBaudRateIndex(){
        // 110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600
        int returnBaudRateIndex;

        switch( serialPort.getBaudRate() ){
            case 110:
                returnBaudRateIndex = 0;
                break;
            case 300:
                returnBaudRateIndex = 1;
                break;
            case 600:
                returnBaudRateIndex = 2;
                break;
            case 1200:
                returnBaudRateIndex = 3;
                break;
            case 2400:
                returnBaudRateIndex = 4;
                break;
            case 4800:
                returnBaudRateIndex = 5;
                break;
            case 9600:
                returnBaudRateIndex = 6;
                break;
            case 19200:
                returnBaudRateIndex = 7;
                break;
            case 38400:
                returnBaudRateIndex = 8;
                break;
            case 57600:
                returnBaudRateIndex = 9;
                break;
            case 115200:
                returnBaudRateIndex = 10;
                break;
            case 230400:
                returnBaudRateIndex = 11;
                break;
            case 460800:
                returnBaudRateIndex = 12;
                break;
            case 921600:
                returnBaudRateIndex = 13;
                break;

            default:
                returnBaudRateIndex = 0;
                break;
        }
        return returnBaudRateIndex;
    }

    public int getParityIndex(){
        return serialPort.getParity() ;
    }

   synchronized private void setSerialCommParam(){
        try {
            serialPort.setSerialPortParams( this.baudRate,
                    this.dataBits,
                    this.stopBits,
                    this.parity );
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    public void showSerialCommParam(){
        addStatus( "Baud rate: <strong>"+getBaudRate()+
                "</strong>,   Data bits: <strong>"+getDataBits()+
                "</strong>,   Stop bits: <strong>"+getStopBits()+
                "</strong>,   Parity:  <strong>"+getParity()+
                "</strong>");
    }

    private void addTemplateCSS(){
        html.append("<html>");
        html.append("<head>");
        html.append("   <script language=\"javascript\" type=\"text/javascript\">");
        html.append("       function toBottom(){");
        html.append("           window.scrollTo(0, document.body.scrollHeight);");
        html.append("       }");
        html.append("   </script>");
        html.append("</head>");
        html.append("<body onload='toBottom()'>");
        html.append("<style>");
        html.append("body { margin:1px; background-color:black; color:black;}");
        html.append("#read {font-size:12px; color:black; margin:2px 0px 2px 0px; background-color:#4dff88; padding:2px;}");
        html.append("#read:hover {font-size:12px; margin:2px 0px 2px 0px; background-color:#b3ffcc; padding:2px;}");
        html.append("#error {font-size:12px; margin:2px 0px 2px 0px; background-color:#ff6666; padding:2px;}");
        html.append("#error:hover {font-size:12px; margin:2px 0px 2px 0px; background-color:#ff8080; padding:2px;}");
        html.append("#other {font-size:12px; margin:2px 0px 2px 0px; background-color:#8080ff; padding:2px;}");
        html.append("#other:hover  {font-size:12px; margin:2px 0px 2px 0px; background-color:#b3b3ff; padding:2px;}");
        html.append("#write  {font-size:12px; margin:2px 0px 2px 0px; background-color:#808080; padding:2px;}");
        html.append("#write:hover  {font-size:12px; margin:2px 0px 2px 0px; background-color:#b3b3b3; padding:2px;}");
        html.append("</style></body></html>");
    }

    public void sendText(String text){
        serialWriter.setTxTxt(text);
    }

    public void kill(){
        this.run = false;
        addStatus("Error: Transmission stops");
        serialWriter.kill();
        serialReader.kill();
        serialPort.close();
    }

    synchronized private void addStatus( String statusTxt) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logList.add(dtf.format(now) + " " + statusTxt);
    }

    // REFRESH STATUS IN WEBVIEW ( 10 Hz)
    public class SerialRefreshStatus implements Runnable {

        private void refreshStatus(){
            if (logList.size() != 0){
                if (logList.get(0).charAt(9) == 'W') {
                    html.append("<div id=\"write\">  " + logList.get(0) + "</div>");
                } else if (logList.get(0).charAt(9) == 'R') {
                    html.append("<div id=\"read\"> " + logList.get(0) + "</div>");
                } else if (logList.get(0).charAt(9) == 'E') {
                    html.append("<div id=\"error\"> " + logList.get(0) + "</div>");
                    kill(); // stop SerialWriter, SerialReader SerialRefreshStatus thread
                } else {
                    html.append("<div id=\"other\"> " + logList.get(0) + "</div>");
                }

                if (logList.size() > 0) logList.remove(0);

                we.loadContent(html.toString(),"text/html");
                we.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            }
        }

        @Override
        public void run()
        {
            try {
                while (run) {
                    Thread.sleep(100);
                    Platform.runLater(
                            () -> {
                                refreshStatus();
                            }
                    );
                }
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
            System.out.println("status end");
        }
    }





    // READING ONLY STRING WITH END LINE SING '\r\n'
    public static class SerialReader implements Runnable {

        private InputStream in;
        private String rxTxt;
        private boolean run;
        private ArrayList<String> rxLogList;

        public SerialReader( InputStream in ) {
            this.in = in;
            rxTxt = "";
            this.run = true;
        }

        public void setStatusList( ArrayList<String> logList){
            rxLogList = logList;
        }

        synchronized public void addStatus( String statusTxt){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            rxLogList.add(dtf.format(now) + " " +statusTxt);

        }

        private void setRxTxt(String rxTxt){
            this.rxTxt = rxTxt;
        }

        synchronized public String getRxTxt(){
            return rxTxt;
        }

        private void kill(){
            this.run = false;
        }

        @Override
        public void run() {
            StringBuilder rxTxtTemp = new StringBuilder();
            byte[] buffer = new byte[ 1024 ];
            int len = -1;
            try {
                while (this.run){
                    while ( this.in.available() > 0 ) {
                        len = this.in.read(buffer);
                        rxTxtTemp.append(new String(buffer, 0, len));
                        if (rxTxtTemp.charAt(rxTxtTemp.length() - 1) == '\n') {
                            setRxTxt(rxTxtTemp.toString());
                            this.addStatus( "Read: <strong>"+getRxTxt()+"</strong>" );
                            rxTxtTemp.setLength(0);
                        }
                    }
                }
            } catch( IOException e ) {
                e.printStackTrace();
            }
            System.out.println("read end");
            this.addStatus("Error: Problem with connections");
        }


    }

    //SEND STRING TEXT WITH END LINE SING (\r\n)
    public static class SerialWriter implements Runnable {

        private OutputStream out;
        private String txTxt;
        private boolean run;
        private ArrayList<String> txLogList;

        public SerialWriter( OutputStream out ) {
            this.out = out;
            this.txTxt = "";
            this.run = true;
        }

        public void setTxTxt(String txTxt){
            this.txTxt = txTxt;
        }

        synchronized private String getTxTxt()
        {
            return txTxt;
        }

        public void setStatusList( ArrayList<String> logList){
            txLogList = logList;
        }

        synchronized public void addStatus( String statusTxt) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            txLogList.add(dtf.format(now) + " " + statusTxt);
        }

        private void kill(){
            this.run = false;
        }

        @Override
        public void run() {
            String tempTxt;

                try {
                    int len = 0;
                    while (this.run)
                    {
                        tempTxt =  getTxTxt();
                        if (tempTxt.length() > 0) {
                            tempTxt = tempTxt + "\r\n";
                            while (len < tempTxt.length()) {
                                out.write((int) tempTxt.charAt(len));
                                len++;
                            }
                            this.addStatus("Write: <strong>" + tempTxt+"</strong>");
                            setTxTxt("");
                            len = 0;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("write end");
                this.addStatus("Error: end write");


        }
    }

    public static void main( String[] args ) {

    }
}