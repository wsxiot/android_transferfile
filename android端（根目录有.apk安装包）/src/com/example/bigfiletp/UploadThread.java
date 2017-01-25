package com.example.bigfiletp;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bluetoothtp.ServerSocketActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;



public class UploadThread extends Thread{
	private ServerSocket server=null;
	
	private static final String TAG="UploadThread";
	private File uploadFile;
	private int port;
	private Socket socket;
	private boolean quit=false;
	private ExecutorService executorService;//�̳߳������������߳�
	private UploadLogService logService;//�����ϴ������ݿ����
	private UploadProgressListener listener;//�ӿڣ�
	private Context context;
	/*UploadThread���캯��*/
	public UploadThread(Context context,File uploadFile,final int port){
		this.uploadFile=uploadFile;
		this.port=port;
		this.context=context;
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
	/*ģ��Ͽ�����*/
	public void closeLink2(){
		try{
			if(server!=null){
				server.close();
				this.quit=true;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/********�˳�ѭ������socket********/
	public void quit(){
		this.quit=true;
		/*try{//��Ӧ�ùرյ�ǰsocket
			socket.close();
		}catch(IOException e){
			System.out.println("quit�������쳣");
			e.printStackTrace();
		}*/
	}
	
	
	
	
	
/********************���������̳߳ص��߳�*****************************/
class SocketTask extends Thread{
    private Socket socket = null;  
    public  SocketTask(Socket socket)   
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
				System.out.println("���յ����ص�position��"+position+"�ļ�id:"+responseid);///////
				
				if(souceid==null){//��ʾû���ϴ���¼�������ݿ������һ������Ϣ
					logService.save(responseid,uploadFile);///���ݿ⺯��
					System.out.println("souceidΪ��,��һ���ϴ�,�������ݵ����ݿ�");
				}
				RandomAccessFile fileOutStream=new RandomAccessFile(uploadFile,"r");
				System.out.println("positionתint��"+Integer.valueOf(position));
				fileOutStream.seek(Integer.valueOf(position));//ʹָ��ָ��ϵ�λ��
				byte[] buffer=new byte[1024*10];
				int len = -1;
				int length=Integer.valueOf(position);//ʹpositionת��Ϊ����
				while((len=fileOutStream.read(buffer))!=-1){
					System.out.println("len="+len);
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
				System.out.println("socket���ͻ���ǿ�Ʊ��Ͽ�");
			}
		}
	 }   //*/
/********************���������̳߳ص��߳�*****************************/
	









	
	
/************************************************************************************************************************/
	/*run()*/
	@Override
	public void run(){
		try{
			//ServerSocket server = new ServerSocket(port);
			server=new ServerSocket(port);
			while(!quit){//**���ϵļ������socket����
				try{
					//ServerSocket server = new ServerSocket(port);
					//server.setSoTimeout(15000);
					System.out.println("serversocket����ͨ��");
					socket = server.accept();//�����ģ�
					server.setSoTimeout(100000);
					System.out.println("���ӳɹ�");
					System.out.println("���ӳɹ�");
					System.out.println("���ӳɹ�");
					executorService.execute(new SocketTask(socket));//Ϊ֧�ֶ��û��������ʣ���ȡ�̳߳ع���ÿһ���û�����������
				}catch(Exception e){
					System.out.println("15S��û�пͻ�������");
					System.out.println("setSoTimeout��ʱ7S����quit����");
					quit();
					server.close();
					e.printStackTrace();
					
					/****************�л������ϴ�**************************/
					/*if(i==0){
						BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
						_bluetooth.enable();//��������
						//�����ɷ��ֹ���
						Intent enabler2 = new Intent(
								BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						((Activity) context).startActivityForResult(enabler2, 0);//��REQUEST_DISCOVERABLE�ĳ�0�Ͳ������ѯ���Ƿ����ɼ��Ի�����
						_bluetooth.setName("�������������");
						//�����ɷ��ֹ���
						Thread.sleep(1500);
						Intent enabler = new Intent(context, ServerSocketActivity.class);
						context.startActivity(enabler);
					}*/
					/****************�л������ϴ�****************************/
				
				
				}
			 }		
				
				
				 
				                                                                                           
		}catch(Exception e){
			System.out.println("socket����������");
			e.printStackTrace();
		}
	}
	
	/*UploadProgressListener�ӿ�*/
	public interface UploadProgressListener{
		void onUploadSize(int size);
	}

}
