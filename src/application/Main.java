package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class Main extends Application {
	Socket socket;
	TextArea textArea;
	
	//클라이언트 프로그램 동작 메소드
	public void startClient(String IP,int port) {
		//다중의 Thread를 사용하지 않기 때문에 Runnable대신 Thread를 사용한다
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP,port);
					
					//서버로부터 메세지를 전달받음
					receive();
				}catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						//프로그램 종료
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	//클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//서버로부터 메시지를 전달받는 메소드
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1 ) throw new IOException();
				String message = new String(buffer,0,length,"UTF-8");
				Platform.runLater(()->{
					textArea.appendText(message);
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	//서버로부터 전달받는 쓰레드 하나, 서버로 전송하는 쓰레드 하나 2개가 필요하다.
	//서버로 메세지를 전달하는 메소드
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					
					//버퍼의 메세지를 가져온다.
					out.write(buffer);
					//전송
					out.flush();
					
				} catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	//프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		//borderpane위에 추가될 레이아웃
		HBox hbox = new HBox();
		//여백
		hbox.setSpacing(5);
		TextField userName = new TextField();
		userName.setPromptText("닉네임을 입력");
		//hbox내부에서 해당 텍스트필드가 출력되도록 함
		HBox.setHgrow(userName,Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		//hbox내부에 세개의 텍스트필드 추가
		hbox.getChildren().addAll(userName,IPText,portText);
		
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event->{
			send(userName.getText()+":"+input.getText()+"\n");
			input.setText("");
			//다시 전송할 수 있도록 focus설정
			input.requestFocus();
		});
		//엔터를 눌러도, 버튼을 눌러도 전송되도록
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		sendButton.setOnAction(event->{
			send(userName.getText()+":"+input.getText()+"\n");
			input.setText("");
			//다시 전송할 수 있도록 focus설정
			input.requestFocus();
		});
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event->{
			if(connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				}catch(Exception e){
					e.printStackTrace();
				}
				startClient(IPText.getText(),port);
				Platform.runLater(()->{
					textArea.appendText("[채팅방 접속]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			}
			else {
				stopClient();
				Platform.runLater(()->{
					textArea.appendText("[채팅방 퇴장]\n");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[채팅 클라이언트]");
		primaryStage.setScene(scene);
		
		//닫기버튼을 눌렀을 때
		primaryStage.setOnCloseRequest(event->stopClient());
		primaryStage.show();
		//접속시 커넥션 버튼에 포커싱되도록
		connectionButton.requestFocus();
	}
	
	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
