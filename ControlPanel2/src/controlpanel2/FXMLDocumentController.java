package controlpanel2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javax.swing.JOptionPane;
import net.java.games.input.*;

/**
 *
 * @author Joseph Arrigo
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML private WebView view_Camera;
    @FXML private MenuItem menu_Close, menu_Settings, menu_About;
    @FXML private Button button_Connect, button_FWD, button_BAK, button_LFT,
            button_RGT, button_Controller;
    @FXML private Circle circle_Indicator, circle_Indicator2;
    @FXML private Text text_A1, text_A2, text_A3, text_A4, text_Motor1,
            text_Motor2;
    
    Socket socket;
    String IP = "";
    PrintWriter out;
    BufferedReader in;
    ScheduledExecutorService executor, executor2;
    
    boolean gamepadOn;
    Controller gamepad;
    
    Properties properties;
    FileInputStream input;
    FileOutputStream output;
    
    @FXML
    public void showAboutMenu(){
        JOptionPane.showMessageDialog(null, "HAV Tech | 2017-18");
    }
    
    @FXML
    public void close(){
        try{
            socket.close();
        }catch(Exception ex){}
        try{
            out.close();
        }catch(Exception ex){}
        try{
            in.close();
        }catch(Exception ex){}
        try{
            executor.shutdown();
        }catch(Exception ex){}
        try{
            executor2.shutdown();
        }catch(Exception ex){}
        try{
            input.close();
        }catch(Exception ex){}
        try{
            output.close();
        }catch(Exception ex){}
        
    }
    
    @FXML
    public void connect(){
        try {
            socket = new Socket(IP, 4444);
            socket.setTcpNoDelay(true);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("<HAV_ts>");
            circle_Indicator.setFill(Color.LIME); 
            
            Runnable thrusterOutput = new Runnable() {
                public void run() {
                    float motor1 = 0, motor2 = 0;
                    
                    boolean fwd = button_FWD.isPressed();
                    boolean bak = button_BAK.isPressed();
                    boolean lft = button_LFT.isPressed();
                    boolean rgt = button_RGT.isPressed();
                    
                    if(fwd){
                        motor1 = 0.6f;
                        motor2 = 0.6f;
                    }else if(bak){
                        motor1= -0.6f;
                        motor2 = -0.6f;
                    }else if(lft){
                        motor1 = 0.6f;
                        motor2 = -0.6f;
                    }else if(rgt){
                        motor1 = -0.6f;
                        motor2 = 0.6f;
                    }else{
                        motor1 = 0.0f;
                        motor2 = 0.0f;
                    }
                    
                    if(gamepadOn){
                        gamepad.poll();
                        Component yC = gamepad.getComponent(Component.Identifier.Axis.Y);
                        Component ryC = gamepad.getComponent(Component.Identifier.Axis.RY);
                        Component zC = gamepad.getComponent(Component.Identifier.Axis.Z);
                        
                        float y = yC.getPollData();
                        float ry = ryC.getPollData();
                        float z = zC.getPollData();
                        
                        if(y < .1 && y > -.1){
                            y = 0.0f;
                        }else if(y > .94){
                            y = 1f;
                        }
                        if(ry < .1 && ry > -.1){
                            ry = 0.0f;
                        }else if(ry > .94){
                            ry = 1f;
                        }
                        if(z < .1 && z > -.1){
                            z = 0.0f;
                        }else if( z > .94){
                            z = 1f;
                        }
                        
                        motor1=y;
                        motor2=ry;
                    }
                    
                    text_Motor1.setText(""+motor1);
                    text_Motor2.setText(""+motor2);
                    out.println("<HAV_ts> " + motor1 + "," + motor2 +"\n");
                }
            };
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(thrusterOutput, 0, 2000, TimeUnit.MILLISECONDS);
            
            Runnable probewareInput = new Runnable() {
                public void run() {
                    try {
                        String strIn = in.readLine();
                        strIn = strIn.replace("<HAV_pi>", "");
                        
                        if(strIn.startsWith("<HAV_pw>")){
                            strIn = strIn.replace("<HAV_pw>", "") + ",";
                            String[] strValues = new String[4];
                            int it = 0;
                            for(char a : strIn.toCharArray()){
                                if(a == ','){
                                    it++;
                                }else{
                                    strValues[it] = strValues[it] + a;
                                }
                            }
                            float[] values = new float[4];
                            for(int i = 0; i < strValues.length; i++){
                                values[i] = Float.parseFloat(strValues[i]);
                            }
                            
                            text_A1.setText(""+values[1]);
                            text_A2.setText(""+values[2]);
                            text_A3.setText(""+values[3]);
                            text_A4.setText(""+values[4]);
                        }
                        
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            executor2 = Executors.newScheduledThreadPool(1);
            executor2.scheduleAtFixedRate(probewareInput, 0, 10, TimeUnit.MILLISECONDS);
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            circle_Indicator.setFill(Color.YELLOW);
        }
    }
    
    @FXML
    public void connectToController(){
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] cs = ce.getControllers();
        gamepad = null;
        for (int i = 0; i < cs.length; i++){
            if(cs[i].getType() == Controller.Type.GAMEPAD){
                    gamepad=cs[i];
                    break;
            }
        }
        
        if(gamepad != null){
            circle_Indicator2.setFill(Color.LIME);
        }else{
            
        }
    }
    
    @FXML
    public void settings(){
        IP = JOptionPane.showInputDialog("Enter LOCAL IP now", IP);
        properties.setProperty("IP", IP);
        try {
            properties.store(output, "--Weird, isn't it?");
        } catch (IOException ex) {}
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        properties = new Properties();
        try {
            input = new FileInputStream("settings.conf");
            properties.load(input);
            IP = properties.getProperty("IP");
            
            output = new FileOutputStream("settings.conf");
        } catch (FileNotFoundException ex) {} catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
