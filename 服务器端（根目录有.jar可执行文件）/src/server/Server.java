package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static void main(String[] args) {
		try {
			ServerSocket serversocket=new ServerSocket(8888);//����һ��serversocket
			Socket socket=null;
			System.out.println("Server is booting");
			while(true){
				 socket=serversocket.accept();//����
				System.out.println("welcome!!!");
				ServerThread thread=new ServerThread(socket);//�½��߳̽��и�socketͨ��
				thread.start();//�߳�����
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
