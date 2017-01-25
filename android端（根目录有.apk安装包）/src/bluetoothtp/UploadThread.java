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
	private ExecutorService executorService;//线程池用来管理传输线程
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";//蓝牙ServerSocket的名称
	private BluetoothServerSocket _serverSocket;
	private BluetoothSocket socket;
	
	private UploadLogService logService;//储存上传的数据库对象
	private UploadProgressListener listener;//接口；
	/*UploadThread构造函数*/
	public UploadThread(Context context,File uploadFile/*final String dstName,final int dstPort*/){
		this.uploadFile=uploadFile;     //要上传的文件
		executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50);
		logService=new UploadLogService(context);//数据库初始化:生成数据库，建立表|文件路径（String），文件id号（String）|
	}
	/*传递接口的setListener函数*/
	public void setListener(UploadProgressListener listener){
		this.listener=listener;
	}
	/*模拟断开连接*/
	public void closeLink(){
		try{
			if(socket!=null){
				socket.close();
				System.out.println("中断socket连接");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/*退出循环监听*/
	public void quit(){
		this.quit=true;//关闭socket循环监听
	}
	/***选择文件时调用*****/
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
	
	
/********************用于塞入线程池的线程*****************************/
	class SocketTask extends Thread{
	    private BluetoothSocket socket = null;  
	    public  SocketTask(BluetoothSocket socket)   
	    {  
	       this.socket = socket;  
	    }  
			public void run(){
				try {
					/************创建协议头head*************/
				    String souceid=logService.getBindId(uploadFile);///数据库函数
				    String head="Content-length="+uploadFile.length()+";filenname="+uploadFile.getName()
					+";sourceid="+(souceid==null ? "" :souceid);//+"%";
					System.out.println("head"+head);//////////////////////		   
					
							   
					/*******test*************/
		            //发送协议头
					PrintWriter out = null;
					OutputStream outStream=socket.getOutputStream();
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)), true);
					try{
		            out.println(head);
		            System.out.println("以发送协议头");
		            }catch(Exception e){
		         	   System.out.println("输出流发送协议头内保护出现问题");
		            }
					//接收response
					String response=null;
					BufferedReader br=null;
					try{
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					response = br.readLine();
					System.out.println("来自客户端的的消息---respone=="+response);
					}catch(Exception e){
						System.out.println("输入流内接收response内保护出现问题");
					}
					System.out.println("来自客户端的的消息---respone=="+response);
					/*******test**********/
					String[] items=response.split(";");
					String responseid=items[0].substring(items[0].indexOf("=")+1);//id
					String position=items[1].substring(items[1].indexOf("=")+1);//position断点
					System.out.println("接收到返回的position："+position+"文件id:"+responseid);/////////////////////////////////////////////////////////////////////////
					
					if(souceid==null){//表示没有上传记录，往数据库里添加一条绑定信息
						logService.save(responseid,uploadFile);///数据库函数
						System.out.println("souceid为空,第一次上传,存入数据到数据库");
					}
					RandomAccessFile fileOutStream=new RandomAccessFile(uploadFile,"r");
					System.out.println("position转int型"+Integer.valueOf(position));///////////////////////////////////////////////////////////
					fileOutStream.seek(Integer.valueOf(position));//使指针指向断点位置
					byte[] buffer=new byte[1024*10];
					int len = -1;
					int length=Integer.valueOf(position);//使position转换为整形
					while((len=fileOutStream.read(buffer))!=-1){
						System.out.println("len="+len);////////////////////////////////////////////////////////////////////////////
						outStream.write(buffer,0,len);
						length+=len;
						System.out.println("length="+length);
						listener.onUploadSize(length);//接口函数中的方法用来更新主UI,更新对话进度条对话框
						System.out.println("正在上传文件");
					}
					
					System.out.println("延迟300毫秒执行,等待接收方接收完数据后在关闭socket");
					Thread.sleep(3500);/**延迟3.5秒执行,等待接收方接收完数据后在关闭socket**/
					
					
					System.out.println("关闭文件流");
					fileOutStream.close();
					System.out.println("断开socket");
					socket.close();
					
					if(length==uploadFile.length()){    //判断是否上传完成
						System.out.println("传输完成删除数据库信息");
						logService.delete(uploadFile);///数据库函数，删除信息
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
				}
			}
		 }   //*/
/********************用于塞入线程池的线程*****************************/
	
	
	
	
	
	
	
	/*run()*/
	@Override
	public void run(){
		try{
			
			/*********监听连接socket**********/
			 _serverSocket = _bluetooth.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
			 UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//00001101-0000-1000-8000-00805F9B34FB创建自己的唯一标识
			 while(!quit){
				 System.out.println("经过循环");
				 try{
					 System.out.println("接下来监听socket连接");
					 socket = _serverSocket.accept();//BluetoothSocket
					 System.out.println("监听的BlutoothSoket连接成功");
					 executorService.execute(new SocketTask(socket));//为支持多用户并发访问，采取线程池管理每一个用户的连接请求
				 }catch(Exception e){
					 System.out.println("10S内没有客户端连接");
					 System.out.println("setSoTimeout超时10S调用quit函数");
					 quit();
					 e.printStackTrace();
				 }
			 }
			/*********监听连接socket**********/
			
			
			 
			
		}catch(Exception e){
			System.out.println("socket流出现问题,关闭socket");
				try {
					if(socket!=null ) socket.close();
				} catch (IOException e1) {} 
			e.printStackTrace();
		}
	}
	/*UploadProgressListener接口*/
	public interface UploadProgressListener{
		void onUploadSize(int size);
	}

}
