package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static void main(String[] args) {
		try {
			ServerSocket serversocket=new ServerSocket(8888);//建立一个serversocket
			Socket socket=null;
			System.out.println("Server is booting");
			while(true){
				 socket=serversocket.accept();//阻塞
				System.out.println("welcome!!!");
				ServerThread thread=new ServerThread(socket);//新建线程进行该socket通信
				thread.start();//线程启动
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
