import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class Main {
	private static Map<Long, FileLogInfo> datas = new HashMap<Long, FileLogInfo>();//��Ŷϵ�����,�Ժ��Ϊ���ݿ���  
	private static FileServer fileserver=null;
	private static MyThread my=null;
	private static String localip;
	public static void main(String[] args){
		InetAddress ia=null;
		try {
			ia=ia.getLocalHost();
			String localname=ia.getHostName();
			localip=ia.getHostAddress();
			System.out.println("���������ǣ�"+ localname);
			System.out.println("������ip�� ��"+localip);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		fileserver=new FileServer(8750);
		//MyThread my=new MyThread(fileserver);
		//my.start();
		try {
			fileserver.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//fileserver.quit();
		System.out.println("��ʼ");
	}
}
