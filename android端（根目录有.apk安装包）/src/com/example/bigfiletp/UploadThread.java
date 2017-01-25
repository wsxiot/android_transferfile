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
	private ExecutorService executorService;//线程池用来管理传输线程
	private UploadLogService logService;//储存上传的数据库对象
	private UploadProgressListener listener;//接口；
	private Context context;
	/*UploadThread构造函数*/
	public UploadThread(Context context,File uploadFile,final int port){
		this.uploadFile=uploadFile;
		this.port=port;
		this.context=context;
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
	/*模拟断开连接*/
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
	
	/********退出循环监听socket********/
	public void quit(){
		this.quit=true;
		/*try{//不应该关闭当前socket
			socket.close();
		}catch(IOException e){
			System.out.println("quit函数的异常");
			e.printStackTrace();
		}*/
	}
	
	
	
	
	
/********************用于塞入线程池的线程*****************************/
class SocketTask extends Thread{
    private Socket socket = null;  
    public  SocketTask(Socket socket)   
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
				System.out.println("接收到返回的position："+position+"文件id:"+responseid);///////
				
				if(souceid==null){//表示没有上传记录，往数据库里添加一条绑定信息
					logService.save(responseid,uploadFile);///数据库函数
					System.out.println("souceid为空,第一次上传,存入数据到数据库");
				}
				RandomAccessFile fileOutStream=new RandomAccessFile(uploadFile,"r");
				System.out.println("position转int型"+Integer.valueOf(position));
				fileOutStream.seek(Integer.valueOf(position));//使指针指向断点位置
				byte[] buffer=new byte[1024*10];
				int len = -1;
				int length=Integer.valueOf(position);//使position转换为整形
				while((len=fileOutStream.read(buffer))!=-1){
					System.out.println("len="+len);
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
				System.out.println("socket被客户端强制被断开");
			}
		}
	 }   //*/
/********************用于塞入线程池的线程*****************************/
	









	
	
/************************************************************************************************************************/
	/*run()*/
	@Override
	public void run(){
		try{
			//ServerSocket server = new ServerSocket(port);
			server=new ServerSocket(port);
			while(!quit){//**不断的监听多个socket请求
				try{
					//ServerSocket server = new ServerSocket(port);
					//server.setSoTimeout(15000);
					System.out.println("serversocket创建通过");
					socket = server.accept();//阻塞的；
					server.setSoTimeout(100000);
					System.out.println("连接成功");
					System.out.println("连接成功");
					System.out.println("连接成功");
					executorService.execute(new SocketTask(socket));//为支持多用户并发访问，采取线程池管理每一个用户的连接请求
				}catch(Exception e){
					System.out.println("15S内没有客户端连接");
					System.out.println("setSoTimeout超时7S调用quit函数");
					quit();
					server.close();
					e.printStackTrace();
					
					/****************切换蓝牙上传**************************/
					/*if(i==0){
						BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
						_bluetooth.enable();//开启蓝牙
						//开启可发现功能
						Intent enabler2 = new Intent(
								BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						((Activity) context).startActivityForResult(enabler2, 0);//把REQUEST_DISCOVERABLE改成0就不会出现询问是否开启可见对话框了
						_bluetooth.setName("蓝牙传输服务器");
						//开启可发现功能
						Thread.sleep(1500);
						Intent enabler = new Intent(context, ServerSocketActivity.class);
						context.startActivity(enabler);
					}*/
					/****************切换蓝牙上传****************************/
				
				
				}
			 }		
				
				
				 
				                                                                                           
		}catch(Exception e){
			System.out.println("socket流出现问题");
			e.printStackTrace();
		}
	}
	
	/*UploadProgressListener接口*/
	public interface UploadProgressListener{
		void onUploadSize(int size);
	}

}
