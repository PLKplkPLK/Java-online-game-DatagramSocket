import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.GraphicsContext.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

import java.util.*;

import javafx.concurrent.*;

import javafx.beans.*;
import javafx.beans.value.*;

import java.awt.event.KeyEvent;
import java.awt.event.*;

import java.net.*;

import java.io.IOException;





class Player
{
    int x,y,points;
    String color;
    
    public Player(int xc, int yc, String ccolor)
   {
    x = xc;
    y = yc;
    points = 0;
    color = ccolor;
   }
}


class P_move
 {
  Player p1;
  Player p2;
  int[] points_x;
  int[] points_y;
  
  public P_move()
   {
    p1 = new Player(100,100,"blue");
    p2 = new Player(700,700,"red");
    points_x = new int[]{200, 400, 600, 200};
    points_y = new int[]{300, 300, 700, 400};
   }
 }


class G_task extends Task<P_move>
 {
  P_move p_move;
  DatagramSocket ds;
    
  public G_task(DatagramSocket ds, P_move p_move)
   {   
    this.p_move = p_move;
    this.ds = ds;
   }
  
  @Override
  protected P_move call() throws Exception
  {
  while(true){
    
    byte[] key = new byte[2];
    this.ds.receive(new DatagramPacket(key, 2));
    System.out.println("Recieved it, Carlos");
    
        switch(key[0]){
         case 1:
             p_move.p1.y=p_move.p1.y-50;
             break;
         case 2:
             p_move.p1.x=p_move.p1.x+50;
             break;
         case 3:
             p_move.p1.y=p_move.p1.y+50;
             break;
         case 4:
             p_move.p1.x=p_move.p1.x-50;
             break;
     }

    
    System.out.println("P1 changed: "+p_move.p1.x);
    
    for(int i=0; i<4; ++i){
        if(p_move.p1.x==p_move.points_x[i] && p_move.p1.y==p_move.points_y[i]){
            p_move.points_x[i]=0;
            p_move.points_y[i]=0;
            p_move.p1.points++;
        }
        if(p_move.p2.x==p_move.points_x[i] && p_move.p2.y==p_move.points_y[i]){
            p_move.points_x[i]=0;
            p_move.points_y[i]=0;
            p_move.p2.points++;
        }
    }
    
    if(isCancelled()) break;
    
    updateValue(null);
    updateValue(p_move);
  }
  return p_move;
  }
}

 class Game_service extends Service<P_move>
 {
  
   Task t; 
   DatagramSocket ds;
   P_move p_move;
   
   public Game_service(DatagramSocket ds, P_move p_move)
    {
       this.ds = ds;
       this.p_move = p_move;
    }
   
   protected Task createTask() 
    {
     t = new G_task(this.ds, this.p_move);
   
     return t;
  
    }
 
 }

 
   





public class JavaFXApp extends Application implements ChangeListener<P_move> 
 {   
  Stage stage;
  Canvas canvas = new Canvas(1000, 1000);
  GraphicsContext gc;
  DatagramSocket ds = null;
  P_move p_move = new P_move();

  Game_service g_s;

  public static void main(String[] args) {
    
    launch(args);
  }

@Override
  public void start(Stage primaryStage) {
  primaryStage.setTitle("JavaFX App");
  
  stage = primaryStage;

  Menu menu1 = new Menu("File");
    
  MenuItem menuItem1 = new MenuItem("Item 1");

  MenuItem menuItem2 = new MenuItem("Exit");
  
  menuItem2.setOnAction(e -> {
                              System.out.println("Exit Selected");
                              
                              exit_dialog();

                             });
  
  menu1.getItems().add(menuItem1);
  menu1.getItems().add(menuItem2);

    
  MenuBar menuBar = new MenuBar();
  
  menuBar.getMenus().add(menu1);
                      
  VBox vBox = new VBox(menuBar);
  
  gc = canvas.getGraphicsContext2D();
  
  gc.setFill(Color.GREEN);
  gc.fillRect(0, 0, 900, 900);
  
  vBox.getChildren().add(canvas);
  Scene scene = new Scene(vBox, 1000, 1000);
                            
  primaryStage.setScene(scene);
  
  try{
       ds = new DatagramSocket(7172);//7171, InetAddress.getByName("127.0.0.1"));
                                     //gracz 2: 7172
    
                } catch (SocketException excep){
                    System.out.println("SocketException onkeypress "+excep);
                }
                catch (IOException excep) {
                    System.out.println("IOException");
                }
   
  primaryStage.setOnCloseRequest(e -> {
                                       e.consume();
                                       exit_dialog();
                                      });

  scene.setOnKeyPressed(e -> {
    byte[] key = new byte[]{0,1};
                 switch(e.getCode()){
                     case RIGHT:
                         key[0] = 2;
                         p_move.p2.x=p_move.p2.x+50;
                         break;
                     case LEFT:
                         key[0] = 4;
                         p_move.p2.x=p_move.p2.x-50;
                         break;
                     case UP:
                         key[0] = 1;
                         p_move.p2.y=p_move.p2.y-50;
                         break;
                     case DOWN:
                         key[0] = 3;
                         p_move.p2.y=p_move.p2.y+50;
                         break;
                 }
                 try
                 {
                     //send datagram
                     System.out.println("Did I sent it or did I don't... didn't send it? AHH! SENT IT");
                     ds.send(new DatagramPacket(key, 2, InetAddress.getByName("127.0.0.1"), 7171));
                     for(int i=0; i<4; ++i){
                            if(p_move.p1.x==p_move.points_x[i] && p_move.p1.y==p_move.points_y[i]){
                                p_move.points_x[i]=0;
                                p_move.points_y[i]=0;
                                p_move.p1.points++;
                            }
                            if(p_move.p2.x==p_move.points_x[i] && p_move.p2.y==p_move.points_y[i]){
                                p_move.points_x[i]=0;
                                p_move.points_y[i]=0;
                                p_move.p2.points++;
                            }
                        }
                     changedp(p_move);
                }
                 catch (IOException ioe)
                 {
                     ioe.printStackTrace();
                 }
  });
  
  if(ds!=null) g_s = new Game_service(ds, p_move);
                                      
  g_s.valueProperty().addListener(this::changed);
  
  g_s.start();    

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 1000);
        gc.setFill(Color.BLUE);
        gc.fillRect(100, 100, 50, 100);
        gc.setFill(Color.RED);
        gc.fillRect(700, 700, 50, 100);
        gc.setFill(Color.BROWN);
        gc.fillRect(200, 300, 50, 50);
        gc.fillRect(400, 300, 50, 50);
        gc.fillRect(600, 700, 50, 50);
        gc.fillRect(200, 400, 50, 50);
  
  primaryStage.setWidth(900);
  primaryStage.setHeight(900);
  primaryStage.show();
  
 }
 
 public void changedp(P_move newValue){
     System.out.println("Printin' gaem");
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 1000);
        gc.setFill(Color.BLUE);
        gc.fillRect(newValue.p1.x, newValue.p1.y, 50, 100);
        gc.setFill(Color.RED);
        gc.fillRect(newValue.p2.x, newValue.p2.y, 50, 100);
        gc.setFill(Color.BROWN);
        for(int i=0; i<4; ++i){
            gc.fillRect(newValue.points_x[i], newValue.points_y[i], 50, 50);
        }
 }
 
 
  public void changed(ObservableValue<? extends P_move> observable,
                      P_move oldValue,
                      P_move newValue) 
   {
        System.out.println(newValue.p1.x);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 1000);
        gc.setFill(Color.BLUE);
        gc.fillRect(newValue.p1.x, newValue.p1.y, 50, 100);
        gc.setFill(Color.RED);
        gc.fillRect(newValue.p2.x, newValue.p2.y, 50, 100);
        gc.setFill(Color.BROWN);
        for(int i=0; i<4; ++i){
            gc.fillRect(newValue.points_x[i], newValue.points_y[i], 50, 50);
        }
   }
 
 

 public void item_1()
  {
   System.out.println("item 1");
  } 
 
 public void exit_dialog()
  {
   System.out.println("exit dialog");

   this.ds.close();

   Alert alert = new Alert(AlertType.CONFIRMATION,
                           "Do you really want to exit the program?.", 
                 ButtonType.YES, ButtonType.NO);

   alert.setResizable(true);
   alert.onShownProperty().addListener(e -> { 
                                             Platform.runLater(() -> alert.setResizable(false)); 
                                            });

  Optional<ButtonType> result = alert.showAndWait();
  if (result.get() == ButtonType.YES)
   {
    Platform.exit();
   } 
  else 
   {
   }

  }
}
