package 服务器类;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bluetoothtp.ClientSocketActivity;

import com.example.bigfiletp.UploadActivity;
import com.example.bigfiletp.UploadLogService;
import com.example.bigfiletp.UploadThread.UploadProgressListener;
import com.example.bigfiletp.wifi功能类.Constant;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.ContactsContract.Data;
import android.widget.Toast;

public class FileServer {
public int FileLeng=0;/////////返回给主类的下载文件长度
private ExecutorService executorService;
private boolean quit=false;
private String dstName;
private int dstPort;
private Socket socket=null;
private SQLServe sqlserve;//数据库类
private Context context;
//private Map<Long,FlieLogInfo> datas=new HashMap<Long,FileLogInfo>();


public FileServer(Context context,final String dstName,final int dstPort){//构造函数,context是主activity因为是主activity创建数据库
	this.context=context;
	this.dstName=dstName;
	this.dstPort=dstPort;
	executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50);
	sqlserve = new SQLServe(context,"Servedatabasename","Servetablename"); 
}

public void quit(){//退出
	this.quit=true;
	try{
		if(socket!=null){
		socket.close();
		System.out.println("中断socket连接");
		}
	}catch(IOException e){
		e.printStackTrace();
	}
}



public void start(Context ctx) throws IOException {
	socket=new Socket(dstName,dstPort);
	executorService.execute(new SocketTask(socket));//为支持多用户并发访问，采取线程池管理每一个用户的连接请求
	System.out.println("客户端以连上服务器");
	System.out.println("客户端以连上服务器");
}
/****以上的线程内部没用到****/
/*
public FileLogInfo find(Long sourceid){//查找记录中是否存在sourceid的文件
	return datas.get(sourceid);
}
public void save(Long id,File saveFile){//保存上传记录:为了是否需要去找文件断点配置position
	System.out.println("save logfile"+id);
	datas.put(id,new FileLongInfo(id,saveFile.getAbsoluteFile()));
}
public void delete(long sourceid){//删除指定记录
	System.out.println("delete logfile"+sourceid);
	if(datas.containsKey(soruceid)){
		datas.remove(sourceid);
	}
}*/
public int getFileLeng(){//从head中获得要发送文件的长度，用来设置客户端对话框的最大长度；
	 return this.FileLeng;
}

public String find(Long sourceid){//查找记录中是否存在sourceid对应的文件路径
	String result=null;
	result=sqlserve.GetPath(sourceid);
	return result;
}

public void save(long id,File saveFile){//保存上传记录:为了是否需要去找文件断点配置position
	sqlserve.saveserve(id, saveFile);
}

public void delete(long sourceid){//删除指定记录
	sqlserve.delete(sourceid);
	}
/********服务器线程************/
private final class SocketTask implements Runnable  
{  
   private Socket socket = null;  
   public SocketTask(Socket socket)   
   {  
       this.socket = socket;  
   }  
   @Override  
   public void run()   
   {  
       try   
       {  
           System.out.println("FileServer accepted connection "+ socket.getInetAddress()+ ":"+ socket.getPort());  
           //得到客户端发来的第一行协议数据：Content-Length=143253434;filename=xxx.3gp;sourceid=  
           //如果用户初次上传文件，sourceid的值为空。  
           
          
           /******接收headtest***********/
           String head="";
           InputStream inStream=socket.getInputStream();
           BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
           try{
			head = br.readLine();
			//br.close();
			}catch(Exception e){
				System.out.println("输入流内接收response内保护出现问题");
			}
           System.out.println("接收到协议头："+head);
           /******接收headtest**********/
           
			
           if(head!=null)  
           {  
               //下面从协议数据中提取各项参数值  
               String[] items = head.split(";");  
               String filelength = items[0].substring(items[0].indexOf("=")+1);
               String filename = items[1].substring(items[1].indexOf("=")+1);  
               String sourceid = items[2].substring(items[2].indexOf("=")+1);
               System.out.println("协议头不为空"+"文件长度:"+filelength+"文件名:"+filename+"文件id:"+sourceid);///////////////////////////////////
               
               FileLeng=Integer.parseInt(filelength);/////////////////////////////////设置要传递的接受文件长度
               
               //生成资源id，如果需要唯一性，可以采用UUID  
               long id = System.currentTimeMillis();  
               //FileLogInfo log = null;//改为String型  
               String log = null;//改为String型
               if(sourceid!=null && !"".equals(sourceid))  
               {  
                   id = Long.valueOf(sourceid);  /////////不需要转类型了
                   //查找上传的文件是否存在上传记录  
                   log = find(id);  ////////////////////////////应该是find(sourceid);
                   System.out.println("上传记录:"+log);
               }  
               File file = null;  
               int position = 0;  
               System.out.println("是否存在上传记录："+log+" null是第一次上传");/////////////////////////////////////////////////////////////////////////////////////////
               //如果上传的文件不存在上传记录,为文件添加跟踪记录  
               if(log==null)  
               {  
                   //设置存放的位置与当前应用的位置有关  
                   File dir = new File(Environment.getExternalStorageDirectory().getPath());  
                   if(!dir.exists()) dir.mkdirs();  
                   file = new File(dir, filename);  
                   //如果上传的文件发生重名，然后进行改名  
                   if(file.exists())  
                   {  
                       filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                       file = new File(dir, filename);  
                   }  
                   save(id, file);  
                   System.out.println("第一上传该文件，保存上传记录到数据库");
               }  
               // 如果上传的文件存在上传记录,读取上次的断点位置  
               else  
               {  
                   System.out.println("FileServer have exits log not null");  
                   //从上传记录中得到文件的路径  
                   file = new File(log);  ///***********************************数据库传回路径，全路径
                   if(file.exists())  
                   {  
                       File logFile = new File(file.getParentFile(), file.getName()+".log");  
                       if(logFile.exists())  
                       {  
                           Properties properties = new Properties();  
                           properties.load(new FileInputStream(logFile));  
                           //读取断点位置  
                           position = Integer.valueOf(properties.getProperty("length"));  
                       }  
                   }  
               }  
               //***************************上面是对协议头的处理，下面正式接收数据***************************************  


               
               
               
               /***************test************************/
               PrintWriter out = null;
               try{
               out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
               String response = "sourceid="+ id+ ";position="+ position;
               out.println(response);
               //out.close(); //
               //br.close();  //
               System.out.println("返回response给客户端");
               }catch(Exception e){
            	   System.out.println("流内保护出现问题");
               }
               /***************test***********************/
               
               
               RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");  
               //设置文件长度  
               if(position==0) fileOutStream.setLength(Integer.valueOf(filelength));
               //移动文件指定的位置开始写入数据  
               fileOutStream.seek(position);
               byte[] buffer = new byte[1024*100];  
               int len = -1;  
               int length = position;  
               //从输入流中读取数据写入到文件中，并将已经传入的文件长度写入配置文件，实时记录文件的最后保存位置  
               while( (len=inStream.read(buffer)) != -1)  
               {  
            	   
            	   /*******************切换蓝牙传输功能**********************************/
            	   WifiManager wifiManage = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            	   WifiInfo wifiinfo = wifiManage.getConnectionInfo();
            	   String WifiHotName=wifiinfo.getSSID();
            	   System.out.println("热点名:"+WifiHotName);
            	   String a= new String("\"HotSpotRobin\"");
            	   if(!WifiHotName.equals(a)){
            		   	///这种切换方法接受方的缓冲区<发送方的缓冲区
            		   System.out.println("是目的热点"); 
            		   BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
            		   _bluetooth.enable();//开启蓝牙,是耗时的任务，其实是在非主线程中完成的
            		   try {
            			   TimeUnit.SECONDS.sleep(1);      //为了先让蓝牙开启,让主线程延迟1秒在执行,不然的话下面的先执行了蓝牙还没开启
            		   } catch (InterruptedException e) {
            			   e.printStackTrace();
            		   }
            		   Intent enabler = new Intent(context, ClientSocketActivity.class);
            		   context.startActivity(enabler);
            		   quit();
            		   break;
            	   }
            	   else{
            		   System.out.println("是目的热点");
            	   }
            	   /********************切换蓝牙传输功能********************************/
            	   
            	   
            	   System.out.println("写入文件len="+len);
                   fileOutStream.write(buffer, 0, len);  
                   length+=len;
                   try{//进度条更新有时候会抛出异常
                	   listener.onUploadSize(length);
                   }catch(Exception e){
                	   System.out.println("更新进度条出错");
                   }
                   System.out.println("写入文件length="+length);
                   Properties properties = new Properties();  
                   properties.put("length", String.valueOf(length));  
                   FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
                   //实时记录文件的最后保存位置  
                   properties.store(logFile, null);  
                   logFile.close();  
                   if(length==fileOutStream.length()){ ///传输文件完成
                	   System.out.println("传输文件完成,删除数据库id信息");
                       delete(id);
                       
                       System.out.println("文件传输完成，删除记录断点的log文件");
                       File deletelogfile=new File(file.getParentFile(), file.getName()+".log");
                       deletelogfile.delete();
                   }
               }
               listener.onUploadSize(length);//////////////////////////////////////////////////////////
               //如果长传长度等于实际长度则表示长传成功  
               if(length==fileOutStream.length()){  
            	   System.out.println("传输文件完成,删除数据库id信息");
                   delete(id);  
               }
               System.out.println("关闭文件流");
               fileOutStream.close(); //关闭文件流                   
               file = null;
           }  
       }  
       catch (Exception e)   
       {  
    	   System.out.println("socket流出现问题");
           e.printStackTrace();  
       }  
       finally{  
           try  
           {  
               if(socket!=null && !socket.isClosed()) socket.close();
               System.out.println("传输完成socket以关闭");
           }   
           catch (IOException e)  
           {  
               e.printStackTrace();  
           }  
       }  
   }  
} 

/*****************test****************************/
private com.example.bigfiletp.UploadThread.UploadProgressListener listener;//接口；
/*UploadProgressListener接口*/
public interface UploadProgressListener{
	void onUploadSize(int size);
}
/*传递接口的setListener函数*/
public void setListener(com.example.bigfiletp.UploadThread.UploadProgressListener uploadProgressListener){
	this.listener=uploadProgressListener;
}
/*****************test****************************/



}

