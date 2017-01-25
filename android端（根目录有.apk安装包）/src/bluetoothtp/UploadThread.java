package bluetoothtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

public class UploadThread extends Thread{
	private static final String TAG="UploadThread";
	private File uploadFile;
	private String dstName;
	private int dstPort;
	private boolean quit=false;
	private ExecutorService executorService;//�̳߳������������߳�
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";//����ServerSocket������
	private BluetoothServerSocket _serverSocket;
	private BluetoothSocket socket;
	
	private UploadLogService logService;//�����ϴ������ݿ����
	private UploadProgressListener listener;//�ӿڣ�
	/*UploadThread���캯��*/
	public UploadThread(Context context,File uploadFile/*final String dstName,final int dstPort*/){
		this.uploadFile=uploadFile;     //Ҫ�ϴ����ļ�
		executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50);
		logService=new UploadLogService(context);//���ݿ��ʼ��:�������ݿ⣬������|�ļ�·����String�����ļ�id�ţ�String��|
	}
	/*���ݽӿڵ�setListener����*/
	public void setListener(UploadProgressListener listener){
		this.listener=listener;
	}
	/*ģ��Ͽ�����*/
	public void closeLink(){
		try{
			if(socket!=null){
				socket.close();
				System.out.println("�ж�socket����");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/*�˳�ѭ������*/
	public void quit(){
		this.quit=true;//�ر�socketѭ������
	}
	/***ѡ���ļ�ʱ����*****/
	public void closeLink2(){
		try{
			if(_serverSocket!=null){
				_serverSocket.close();
				this.quit=true;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
/********************���������̳߳ص��߳�*****************************/
	class SocketTask extends Thread{
	    private BluetoothSocket socket = null;  
	    public  SocketTask(BluetoothSocket socket)   
	    {  
	       this.socket = socket;  
	    }  
			public void run(){
				try {
					/************����Э��ͷhead*************/
				    String souceid=logService.getBindId(uploadFile);///���ݿ⺯��
				    String head="Content-length="+uploadFile.length()+";filenname="+uploadFile.getName()
					+";sourceid="+(souceid==null ? "" :souceid);//+"%";
					System.out.println("head"+head);//////////////////////		   
					
							   
					/*******test*************/
		            //����Э��ͷ
					PrintWriter out = null;
					OutputStream outStream=socket.getOutputStream();
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)), true);
					try{
		            out.println(head);
		            System.out.println("�Է���Э��ͷ");
		            }catch(Exception e){
		         	   System.out.println("���������Э��ͷ�ڱ�����������");
		            }
					//����response
					String response=null;
					BufferedReader br=null;
					try{
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					response = br.readLine();
					System.out.println("���Կͻ��˵ĵ���Ϣ---respone=="+response);
					}catch(Exception e){
						System.out.println("�������ڽ���response�ڱ�����������");
					}
					System.out.println("���Կͻ��˵ĵ���Ϣ---respone=="+response);
					/*******test**********/
					String[] items=response.split(";");
					String responseid=items[0].substring(items[0].indexOf("=")+1);//id
					String position=items[1].substring(items[1].indexOf("=")+1);//position�ϵ�
					System.out.println("���յ����ص�position��"+position+"�ļ�id:"+responseid);/////////////////////////////////////////////////////////////////////////
					
					if(souceid==null){//��ʾû���ϴ���¼�������ݿ������һ������Ϣ
						logService.save(responseid,uploadFile);///���ݿ⺯��
						System.out.println("souceidΪ��,��һ���ϴ�,�������ݵ����ݿ�");
					}
					RandomAccessFile fileOutStream=new RandomAccessFile(uploadFile,"r");
					System.out.println("positionתint��"+Integer.valueOf(position));///////////////////////////////////////////////////////////
					fileOutStream.seek(Integer.valueOf(position));//ʹָ��ָ��ϵ�λ��
					byte[] buffer=new byte[1024*10];
					int len = -1;
					int length=Integer.valueOf(position);//ʹpositionת��Ϊ����
					while((len=fileOutStream.read(buffer))!=-1){
						System.out.println("len="+len);////////////////////////////////////////////////////////////////////////////
						outStream.write(buffer,0,len);
						length+=len;
						System.out.println("length="+length);
						listener.onUploadSize(length);//�ӿں����еķ�������������UI,���¶Ի��������Ի���
						System.out.println("�����ϴ��ļ�");
					}
					
					System.out.println("�ӳ�300����ִ��,�ȴ����շ����������ݺ��ڹر�socket");
					Thread.sleep(3500);/**�ӳ�3.5��ִ��,�ȴ����շ����������ݺ��ڹر�socket**/
					
					
					System.out.println("�ر��ļ���");
					fileOutStream.close();
					System.out.println("�Ͽ�socket");
					socket.close();
					
					if(length==uploadFile.length()){    //�ж��Ƿ��ϴ����
						System.out.println("�������ɾ�����ݿ���Ϣ");
						logService.delete(uploadFile);///���ݿ⺯����ɾ����Ϣ
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
				}
			}
		 }   //*/
/********************���������̳߳ص��߳�*****************************/
	
	
	
	
	
	
	
	/*run()*/
	@Override
	public void run(){
		try{
			
			/*********��������socket**********/
			 _serverSocket = _bluetooth.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
			 UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//00001101-0000-1000-8000-00805F9B34FB�����Լ���Ψһ��ʶ
			 while(!quit){
				 System.out.println("����ѭ��");
				 try{
					 System.out.println("����������socket����");
					 socket = _serverSocket.accept();//BluetoothSocket
					 System.out.println("������BlutoothSoket���ӳɹ�");
					 executorService.execute(new SocketTask(socket));//Ϊ֧�ֶ��û��������ʣ���ȡ�̳߳ع���ÿһ���û�����������
				 }catch(Exception e){
					 System.out.println("10S��û�пͻ�������");
					 System.out.println("setSoTimeout��ʱ10S����quit����");
					 quit();
					 e.printStackTrace();
				 }
			 }
			/*********��������socket**********/
			
			
			 
			
		}catch(Exception e){
			System.out.println("socket����������,�ر�socket");
				try {
					if(socket!=null ) socket.close();
				} catch (IOException e1) {} 
			e.printStackTrace();
		}
	}
	/*UploadProgressListener�ӿ�*/
	public interface UploadProgressListener{
		void onUploadSize(int size);
	}

}
