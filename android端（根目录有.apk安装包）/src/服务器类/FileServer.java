package ��������;

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
import com.example.bigfiletp.wifi������.Constant;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.ContactsContract.Data;
import android.widget.Toast;

public class FileServer {
public int FileLeng=0;/////////���ظ�����������ļ�����
private ExecutorService executorService;
private boolean quit=false;
private String dstName;
private int dstPort;
private Socket socket=null;
private SQLServe sqlserve;//���ݿ���
private Context context;
//private Map<Long,FlieLogInfo> datas=new HashMap<Long,FileLogInfo>();


public FileServer(Context context,final String dstName,final int dstPort){//���캯��,context����activity��Ϊ����activity�������ݿ�
	this.context=context;
	this.dstName=dstName;
	this.dstPort=dstPort;
	executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50);
	sqlserve = new SQLServe(context,"Servedatabasename","Servetablename"); 
}

public void quit(){//�˳�
	this.quit=true;
	try{
		if(socket!=null){
		socket.close();
		System.out.println("�ж�socket����");
		}
	}catch(IOException e){
		e.printStackTrace();
	}
}



public void start(Context ctx) throws IOException {
	socket=new Socket(dstName,dstPort);
	executorService.execute(new SocketTask(socket));//Ϊ֧�ֶ��û��������ʣ���ȡ�̳߳ع���ÿһ���û�����������
	System.out.println("�ͻ��������Ϸ�����");
	System.out.println("�ͻ��������Ϸ�����");
}
/****���ϵ��߳��ڲ�û�õ�****/
/*
public FileLogInfo find(Long sourceid){//���Ҽ�¼���Ƿ����sourceid���ļ�
	return datas.get(sourceid);
}
public void save(Long id,File saveFile){//�����ϴ���¼:Ϊ���Ƿ���Ҫȥ���ļ��ϵ�����position
	System.out.println("save logfile"+id);
	datas.put(id,new FileLongInfo(id,saveFile.getAbsoluteFile()));
}
public void delete(long sourceid){//ɾ��ָ����¼
	System.out.println("delete logfile"+sourceid);
	if(datas.containsKey(soruceid)){
		datas.remove(sourceid);
	}
}*/
public int getFileLeng(){//��head�л��Ҫ�����ļ��ĳ��ȣ��������ÿͻ��˶Ի������󳤶ȣ�
	 return this.FileLeng;
}

public String find(Long sourceid){//���Ҽ�¼���Ƿ����sourceid��Ӧ���ļ�·��
	String result=null;
	result=sqlserve.GetPath(sourceid);
	return result;
}

public void save(long id,File saveFile){//�����ϴ���¼:Ϊ���Ƿ���Ҫȥ���ļ��ϵ�����position
	sqlserve.saveserve(id, saveFile);
}

public void delete(long sourceid){//ɾ��ָ����¼
	sqlserve.delete(sourceid);
	}
/********�������߳�************/
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
           //�õ��ͻ��˷����ĵ�һ��Э�����ݣ�Content-Length=143253434;filename=xxx.3gp;sourceid=  
           //����û������ϴ��ļ���sourceid��ֵΪ�ա�  
           
          
           /******����headtest***********/
           String head="";
           InputStream inStream=socket.getInputStream();
           BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
           try{
			head = br.readLine();
			//br.close();
			}catch(Exception e){
				System.out.println("�������ڽ���response�ڱ�����������");
			}
           System.out.println("���յ�Э��ͷ��"+head);
           /******����headtest**********/
           
			
           if(head!=null)  
           {  
               //�����Э����������ȡ�������ֵ  
               String[] items = head.split(";");  
               String filelength = items[0].substring(items[0].indexOf("=")+1);
               String filename = items[1].substring(items[1].indexOf("=")+1);  
               String sourceid = items[2].substring(items[2].indexOf("=")+1);
               System.out.println("Э��ͷ��Ϊ��"+"�ļ�����:"+filelength+"�ļ���:"+filename+"�ļ�id:"+sourceid);///////////////////////////////////
               
               FileLeng=Integer.parseInt(filelength);/////////////////////////////////����Ҫ���ݵĽ����ļ�����
               
               //������Դid�������ҪΨһ�ԣ����Բ���UUID  
               long id = System.currentTimeMillis();  
               //FileLogInfo log = null;//��ΪString��  
               String log = null;//��ΪString��
               if(sourceid!=null && !"".equals(sourceid))  
               {  
                   id = Long.valueOf(sourceid);  /////////����Ҫת������
                   //�����ϴ����ļ��Ƿ�����ϴ���¼  
                   log = find(id);  ////////////////////////////Ӧ����find(sourceid);
                   System.out.println("�ϴ���¼:"+log);
               }  
               File file = null;  
               int position = 0;  
               System.out.println("�Ƿ�����ϴ���¼��"+log+" null�ǵ�һ���ϴ�");/////////////////////////////////////////////////////////////////////////////////////////
               //����ϴ����ļ��������ϴ���¼,Ϊ�ļ���Ӹ��ټ�¼  
               if(log==null)  
               {  
                   //���ô�ŵ�λ���뵱ǰӦ�õ�λ���й�  
                   File dir = new File(Environment.getExternalStorageDirectory().getPath());  
                   if(!dir.exists()) dir.mkdirs();  
                   file = new File(dir, filename);  
                   //����ϴ����ļ�����������Ȼ����и���  
                   if(file.exists())  
                   {  
                       filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                       file = new File(dir, filename);  
                   }  
                   save(id, file);  
                   System.out.println("��һ�ϴ����ļ��������ϴ���¼�����ݿ�");
               }  
               // ����ϴ����ļ������ϴ���¼,��ȡ�ϴεĶϵ�λ��  
               else  
               {  
                   System.out.println("FileServer have exits log not null");  
                   //���ϴ���¼�еõ��ļ���·��  
                   file = new File(log);  ///***********************************���ݿ⴫��·����ȫ·��
                   if(file.exists())  
                   {  
                       File logFile = new File(file.getParentFile(), file.getName()+".log");  
                       if(logFile.exists())  
                       {  
                           Properties properties = new Properties();  
                           properties.load(new FileInputStream(logFile));  
                           //��ȡ�ϵ�λ��  
                           position = Integer.valueOf(properties.getProperty("length"));  
                       }  
                   }  
               }  
               //***************************�����Ƕ�Э��ͷ�Ĵ���������ʽ��������***************************************  


               
               
               
               /***************test************************/
               PrintWriter out = null;
               try{
               out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
               String response = "sourceid="+ id+ ";position="+ position;
               out.println(response);
               //out.close(); //
               //br.close();  //
               System.out.println("����response���ͻ���");
               }catch(Exception e){
            	   System.out.println("���ڱ�����������");
               }
               /***************test***********************/
               
               
               RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");  
               //�����ļ�����  
               if(position==0) fileOutStream.setLength(Integer.valueOf(filelength));
               //�ƶ��ļ�ָ����λ�ÿ�ʼд������  
               fileOutStream.seek(position);
               byte[] buffer = new byte[1024*100];  
               int len = -1;  
               int length = position;  
               //���������ж�ȡ����д�뵽�ļ��У������Ѿ�������ļ�����д�������ļ���ʵʱ��¼�ļ�����󱣴�λ��  
               while( (len=inStream.read(buffer)) != -1)  
               {  
            	   
            	   /*******************�л��������书��**********************************/
            	   WifiManager wifiManage = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            	   WifiInfo wifiinfo = wifiManage.getConnectionInfo();
            	   String WifiHotName=wifiinfo.getSSID();
            	   System.out.println("�ȵ���:"+WifiHotName);
            	   String a= new String("\"HotSpotRobin\"");
            	   if(!WifiHotName.equals(a)){
            		   	///�����л��������ܷ��Ļ�����<���ͷ��Ļ�����
            		   System.out.println("��Ŀ���ȵ�"); 
            		   BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
            		   _bluetooth.enable();//��������,�Ǻ�ʱ��������ʵ���ڷ����߳�����ɵ�
            		   try {
            			   TimeUnit.SECONDS.sleep(1);      //Ϊ��������������,�����߳��ӳ�1����ִ��,��Ȼ�Ļ��������ִ����������û����
            		   } catch (InterruptedException e) {
            			   e.printStackTrace();
            		   }
            		   Intent enabler = new Intent(context, ClientSocketActivity.class);
            		   context.startActivity(enabler);
            		   quit();
            		   break;
            	   }
            	   else{
            		   System.out.println("��Ŀ���ȵ�");
            	   }
            	   /********************�л��������书��********************************/
            	   
            	   
            	   System.out.println("д���ļ�len="+len);
                   fileOutStream.write(buffer, 0, len);  
                   length+=len;
                   try{//������������ʱ����׳��쳣
                	   listener.onUploadSize(length);
                   }catch(Exception e){
                	   System.out.println("���½���������");
                   }
                   System.out.println("д���ļ�length="+length);
                   Properties properties = new Properties();  
                   properties.put("length", String.valueOf(length));  
                   FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
                   //ʵʱ��¼�ļ�����󱣴�λ��  
                   properties.store(logFile, null);  
                   logFile.close();  
                   if(length==fileOutStream.length()){ ///�����ļ����
                	   System.out.println("�����ļ����,ɾ�����ݿ�id��Ϣ");
                       delete(id);
                       
                       System.out.println("�ļ�������ɣ�ɾ����¼�ϵ��log�ļ�");
                       File deletelogfile=new File(file.getParentFile(), file.getName()+".log");
                       deletelogfile.delete();
                   }
               }
               listener.onUploadSize(length);//////////////////////////////////////////////////////////
               //����������ȵ���ʵ�ʳ������ʾ�����ɹ�  
               if(length==fileOutStream.length()){  
            	   System.out.println("�����ļ����,ɾ�����ݿ�id��Ϣ");
                   delete(id);  
               }
               System.out.println("�ر��ļ���");
               fileOutStream.close(); //�ر��ļ���                   
               file = null;
           }  
       }  
       catch (Exception e)   
       {  
    	   System.out.println("socket����������");
           e.printStackTrace();  
       }  
       finally{  
           try  
           {  
               if(socket!=null && !socket.isClosed()) socket.close();
               System.out.println("�������socket�Թر�");
           }   
           catch (IOException e)  
           {  
               e.printStackTrace();  
           }  
       }  
   }  
} 

/*****************test****************************/
private com.example.bigfiletp.UploadThread.UploadProgressListener listener;//�ӿڣ�
/*UploadProgressListener�ӿ�*/
public interface UploadProgressListener{
	void onUploadSize(int size);
}
/*���ݽӿڵ�setListener����*/
public void setListener(com.example.bigfiletp.UploadThread.UploadProgressListener uploadProgressListener){
	this.listener=uploadProgressListener;
}
/*****************test****************************/



}

