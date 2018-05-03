package controlpanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 *
 * @author Joseph Arrigo
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Slider slider_Updown;
    @FXML
    private Slider slider_Leftright; //Both
    @FXML
    private Slider slider_Fwdback; //Turning
    @FXML
    private CheckBox tickbox_Reverse;
    @FXML
    private Text text_Status;
    @FXML
    private Button button_Connect;
    
    @FXML
    private Button button_Stop;
    
    Boolean reverse;
    
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    ScheduledExecutorService executor, executor2;
    
    @FXML
    void mouseDragReleased(){
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        slider_Updown.setOnMouseReleased((MouseEvent event) -> {
            ((Slider)event.getSource()).setValue(50);
        });
        slider_Leftright.setOnMouseReleased((MouseEvent event) -> {
            ((Slider)event.getSource()).setValue(50);
        });
        slider_Fwdback.setOnMouseReleased((MouseEvent event) -> {
            ((Slider)event.getSource()).setValue(50);
        });
    }    
    
    public void start_socket(){
        try {
            socket = new Socket("192.168.50.56", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("<HAV_ts>");
            text_Status.setText("Status: Connected");
            
            System.out.println("Good1");
            
            Runnable helloRunnable = new Runnable() {
                public void run() {
                    if(slider_Fwdback.getValue() != 50){
                        double val = (slider_Fwdback.getValue()-50)/50;
                        out.println("<HAV_ts> " + val + "," + -val +"\n");
                    }else if(slider_Leftright.getValue() != 50){
                        double val = (slider_Leftright.getValue()-50)/50;
                        out.println("<HAV_ts> " + val + "," + val +"\n");
                    }else{
                        out.println("<HAV_ts> 0,0\n");
                    }
                }
            };
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(helloRunnable, 0, 2000, TimeUnit.MILLISECONDS);
            
            System.out.println("Good2");
            
            Runnable helloRunnable2 = new Runnable() {
                public void run() {
                    try {
                        System.out.println(in.readLine());
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            executor2 = Executors.newScheduledThreadPool(1);
            executor2.scheduleAtFixedRate(helloRunnable2, 0, 10, TimeUnit.MILLISECONDS);
            
            System.out.println("Good3");
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            text_Status.setText("Status: Errored");
        }
    }
    
    public void stop_butto(){
        try{
            executor.shutdown(); 
        }catch(Exception ex){}
        try{
            out.println("<HAV_ts>reboot");
        }catch(Exception ex){}
        try{
            socket.close();
        }catch(Exception ex){}
        text_Status.setText("Status: Disconnected");
       
    }
    
}