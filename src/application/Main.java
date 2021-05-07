package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;


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
		try {
			
		} catch(Exception e) {
			
		}
	}
	
	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
