package bluetoothtp;

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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bluetoothtp.UploadThread.UploadProgressListener;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract.Data;
import android.widget.Toast;

public class FileServer {
public int FileLeng=0;/////////���ظ�����������ļ�����
private ExecutorService executorService;
private BluetoothDevice device;
private boolean quit=false;
BluetoothSocket socket = null;
private SQLServe sqlserve;//���ݿ���

public FileServer(Context context,BluetoothDevice device){//���캯��,context����activity��Ϊ����activity�������ݿ�
	executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*50);  //��ʼ���̳߳� 1
	this.device=device;
	sqlserve = new SQLServe(context,"Servedatabasename","Servetablename"); 
}

public int getFileLeng(){//��head�л��Ҫ�����ļ��ĳ��ȣ��������ÿͻ��˶Ի������󳤶ȣ�
	 return this.FileLeng;
}

public void quit(){//�˳�
	this.quit=true;
	try{
		if(socket!=null){
		socket.close();
		}
	}catch(IOException e){
		e.printStackTrace();
	}
}

public void start(Context ctx) throws IOException {//�������� 
		try{	
			/*******����socket����**********/
			socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//UUID�����Զ��壿
			socket.connect();//����BluetoothServersocket
			/*******����socket����*********/
			
			
			System.out.println("���ӳɹ�");
			System.out.println("���ӳɹ�");
			System.out.println("���ӳɹ�");
			executorService.execute(new SocketTask(socket));//ʹ���̳߳��������߳�     2
		}catch(Exception e){
			System.out.println("�����쳣1");
			e.printStackTrace();
		}
}
/****���ϵ��߳��ڲ�û�õ�****/




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
   //private Socket socket = null;  
	BluetoothSocket socket=null;
   public SocketTask(BluetoothSocket socket)   
   {  
       this.socket = socket;  
   }  
   @Override  
   public void run()   
   {  
       try   
       {  
           System.out.println("��ʼ����Э��ͷ");  
          
           
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
               System.out.println("�Ƿ�����ϴ���¼��"+log);/////////////////////////////////////////////////////////////////////////////////////////
               //����ϴ����ļ��������ϴ���¼,Ϊ�ļ���Ӹ��ټ�¼  
               if(log==null)  
               {  
            	   System.out.println("�ļ���һ���ϴ�");
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
                   System.out.println("�ļ����ǵ�һ���ϴ�");  
                   //���ϴ���¼�еõ��ļ���·��  
                   file = new File(log);  ///***********************************���ݿ⴫��·����ȫ·��
                   if(file.exists())  
                   {  
                       File logFile = new File(file.getParentFile(), file.getName()+".log");  
                       System.out.println("�ô��ļ�����log�ļ���"+logFile.getName());//////////////////////////////////////////////////
                       if(logFile.exists())  
                       {  
                           Properties properties = new Properties();  
                           properties.load(new FileInputStream(logFile));  
                           //��ȡ�ϵ�λ��  
                           position = Integer.valueOf(properties.getProperty("length"));  
                           System.out.println("����log�ļ��еĶϵ�=="+position);///////////////////////////////////////////////////////
                       }  
                   }  
               }  
               //***************************�����Ƕ�Э��ͷ�Ĵ���������ʽ��������***************************************  
               
               
               /***************test************************/
               //inStream.close();
               PrintWriter out = null;
               try{
               out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
               String response = "sourceid="+ id+ ";position="+ position;
               out.println(response);
               //out.close(); //                ��û�б��ر�
               //br.close();  //
               System.out.println("����response��������");
               }catch(Exception e){
            	   System.out.println("���ڱ�����������");
               }
               /***************test************************/
               
               
               RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");  
               //�����ļ�����  
               if(position==0) fileOutStream.setLength(Integer.valueOf(filelength));
               //�ƶ��ļ�ָ����λ�ÿ�ʼд������  
               fileOutStream.seek(position);
               byte[] buffer = new byte[1024*100];  
               int len = -1;  
               int length = position;  
               //���������ж�ȡ����д�뵽�ļ��У������Ѿ�������ļ�����д�������ļ���ʵʱ��¼�ļ�����󱣴�λ��  
               int  i=0;
               while( (len=inStream.read(buffer)) != -1)  
               {  
            	   System.out.println("len="+len);
            	   fileOutStream.write(buffer, 0, len);
            	   length+=len;
            	   ///////
                   try{//������������ʱ����׳��쳣
                	   listener.onUploadSize(length);
                   }catch(Exception e){
                	   System.out.println("���½���������");
                   }
                   ///////
            	   System.out.println("length="+length);
            	   Properties properties = new Properties();  
                   properties.put("length", String.valueOf(length));  
                   FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
                   //ʵʱ��¼�ļ�����󱣴�λ��  
                   properties.store(logFile, null);  
                   logFile.close();  
                   if(length==fileOutStream.length()){             //�ļ������� ��
                	   System.out.println("�����ļ����,ɾ�����ݿ�id��Ϣ");
                       delete(id);  
                       
                       System.out.println("�ļ�������ɣ�ɾ����¼�ϵ��log�ļ�");
                       File deletelogfile=new File(file.getParentFile(), file.getName()+".log");
                       deletelogfile.delete();
                   }
               } 
               //����������ȵ���ʵ�ʳ������ʾ�����ɹ�  
               if(length==fileOutStream.length()){  //û�е��õ�
            	   System.out.println("�����ļ����,ɾ�����ݿ�id��Ϣ");
                   delete(id);  
               }
               System.out.println("�ر��ļ���");
               fileOutStream.close();//�ر��ļ���; 
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
               if(socket!=null ) socket.close();
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

