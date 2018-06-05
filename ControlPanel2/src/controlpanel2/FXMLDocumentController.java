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
            button_RGT, button_Controller, button_ASC, button_DSC, button_DEC1,
            button_DEC2, button_INC1, button_INC2;
    @FXML private Circle circle_Indicator, circle_Indicator2;
    @FXML private Text text_A1, text_A2, text_A3, text_A4;
    
    Socket socket;
    String IP = "";
    PrintWriter out;
    BufferedReader in;
    ScheduledExecutorService executor, executor2;
    
    boolean gamepadOn;
    Controller gamepad;
    
    int servo1 = 90;
    int servo2 = 90;
    
    float motorL = 0;
    float motorR = 0;
    float motorV = 0;
    
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
            
            Runnable helloRunnable = new Runnable() {
                public void run() {
                    float val = 0.6f;
                    if(button_FWD.isPressed()){
                        motorL = val;
                        motorR = val;
                        motorV = 0;
                    }else if(button_LFT.isPressed()){
                        motorL = -val;
                        motorR = val;
                        motorV = 0;
                    }else if(button_RGT.isPressed()){
                        motorL = val;
                        motorR = -val;
                        motorV = 0;
                    }else if(button_BAK.isPressed()){
                        motorL = -val;
                        motorR = -val;
                        motorV = 0;
                    }else if(button_ASC.isPressed()){
                        motorL = 0;
                        motorR = 0;
                        motorV = val;
                    }else if(button_DSC.isPressed()){
                        motorL = 0;
                        motorR = 0;
                        motorV = -val;
                    }else{
                        motorL = 0;
                        motorR = 0;
                        motorV = 0;
                    }
                    
                    if(button_DEC1.isPressed()){
                        servo1-= 20;
                    }else if(button_INC1.isPressed()){
                        servo1+= 20;
                    }
                    
                    if(button_DEC2.isPressed()){
                        servo2-= 20;
                    }else if(button_INC2.isPressed()){
                        servo2+= 20;
                    }
                    
                    //GAMEPAD CODE
                    
                    if(gamepadOn) {
                        gamepad.poll();
                        Component yC = gamepad.getComponent(Component.Identifier.Axis.Y);
                        Component ryC = gamepad.getComponent(Component.Identifier.Axis.RY);
                        Component zC = gamepad.getComponent(Component.Identifier.Axis.Z);
                        
                        Component bL = gamepad.getComponent(Component.Identifier.Button._4);
                        Component bR = gamepad.getComponent(Component.Identifier.Button._5);
                        Component start = gamepad.getComponent(Component.Identifier.Button._7);
                        
                        float y2 = yC.getPollData();
                        float ry2 = ryC.getPollData();
                        float z2 = zC.getPollData();
                        
                        float y1 = Math.round(y2*100);
                        float ry1 = Math.round(ry2*100);
                        float z1 = Math.round(z2*100);
                        
                        float y = -y1/100;
                        float ry = -ry1/100;
                        float z = -z1/100;
                        
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
                        
                        motorL=y;
                        motorR=ry;
                        motorV=z;
                        
                        boolean flipped = (start.getPollData() == 1.0f);
                        
                        if(bL.getPollData() == 1.0f){
                            if(!flipped){
                                servo1 -= 10;
                            }else {
                                servo2 -= 10;
                            }
                        }else if(bR.getPollData() == 1.0f){
                            if(!flipped){
                                servo1 += 10;
                            }else {
                                servo2 += 10;
                            }
                        }
                    }
                    
                    if(servo1 > 180){
                        servo1 = 180;
                    }else if(servo1 < 0){
                        servo1 = 0;
                    }
                    
                    if(servo2 > 180){
                        servo2 = 180;
                    }else if(servo2 < 0){
                        servo2 = 0;
                    }
                    
                    send("<HAV_ts>th " + motorL + "," + motorR +"\n");
                    send("<HAV_ts>tv " + motorV + "\n");
                    send("<HAV_ts>s1 " + servo1);
                    send("<HAV_ts>s2 " + servo2);
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
            gamepadOn = true;
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
    
    @FXML
    public void stop_butto(){
        try{
            executor.shutdown(); 
        }catch(Exception ex){}
        try{
            out.println("<HAV_ts>reboot\n");
        }catch(Exception ex){}
        try{
            socket.close();
        }catch(Exception ex){}
        circle_Indicator.setFill(Color.RED);
        circle_Indicator2.setFill(Color.RED);
        System.exit(0);
       
    }
    
    public void send(String message){
        out.println("<"+message+">");
    }
    
}
