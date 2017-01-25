package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class ServerThread extends Thread{
	private Socket s=null;
	private InputStream is=null;
	private String name=null;
	private final String filelocate="f://test//";///root/trans/
	
	public ServerThread(Socket socket) {
		this.s = socket;
	}
	@Override
	public void run() {
		try {
			ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
			name=(String)ois.readObject();
			if(name.charAt(0)==':'){
				netin();
			}else{
				netout();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public boolean netin(){//传进
		try {
		    File file=new File(filelocate+name.substring(1,name.length()));
		    RandomAccessFile rafout=new RandomAccessFile(file, "rw");
		    byte[] buf=new byte[1024];
		    int l;
		    is=s.getInputStream();
		    while((l=is.read(buf,0,buf.length))!=-1){
		        rafout.write(buf, 0, l);
		    }
		    rafout.close();
		    is.close();
		    s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		
	}
	public boolean netout(){//传出
		try {
			File file=new File(filelocate+name);
			 RandomAccessFile rafin=new RandomAccessFile(file, "r");
			 byte[] buf=new byte[1024];
			 int l;
			 OutputStream os=s.getOutputStream();
			 while((l=rafin.read(buf,0,buf.length))!=-1){
			     os.write(buf, 0, l);
			     System.out.println("han shu li chuan");
			     os.flush();
			 }
			 rafin.close();
			 os.close();
			 s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
