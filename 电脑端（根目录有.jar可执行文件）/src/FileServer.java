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


public class FileServer {  
     private ExecutorService executorService;//�̳߳�  
     private boolean quit = false;//�˳�  
     private int port;//�����˿� 
     private ServerSocket server=null;
     private Map<Long, FileLogInfo> datas = new HashMap<Long, FileLogInfo>();//��Ŷϵ�����,�Ժ��Ϊ���ݿ���  
     public FileServer(final int port)  
     {  
    	 this.port=port;
         //�����̳߳أ����о���(cpu����*50)���߳�  
         executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);  
     }  
       
    /** 
      * �˳� 
      */  
     public void quit()  
     {  
        this.quit = true;  
        try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }  
       
     /** 
      * �������� 
      * @throws Exception 
      */  
     public void start() throws Exception{
    	 //while(true){
    		 System.out.println("��ʼ��������");
    		 server = new ServerSocket(port);//ʵ�ֶ˿ڼ���
    		 Socket socket=server.accept();
    		 executorService.execute(new SocketTask(socket));//Ϊ֧�ֶ��û��������ʣ���ȡ�̳߳ع���ÿһ���û�����������
    		 System.out.println("�ͻ��������Ϸ�����");
    		 System.out.println("�ͻ��������Ϸ�����");
    	 //}
     }  
       
     private final class SocketTask implements Runnable  
     {  
        private Socket socket = null;  
        public SocketTask(Socket socket)   
        {  
            this.socket = socket;  
        }  
        public void run()   
        {  
            try   
            {  
                System.out.println("FileServer accepted connection "+ socket.getInetAddress()+ ":"+ socket.getPort());  
                //�õ��ͻ��˷����ĵ�һ��Э�����ݣ�Content-Length=143253434;filename=xxx.3gp;sourceid=  
                //����û������ϴ��ļ���sourceid��ֵΪ�ա�  
                
                
                /******����headtest***********/
                String head=null;
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


                
                
                System.out.println("FileServer head:"+head);  
                if(head!=null)  
                {  
                    //�����Э����������ȡ�������ֵ  
                	System.out.println("head��Ϊ��"+head);
                    String[] items = head.split(";");  
                    String filelength = items[0].substring(items[0].indexOf("=")+1);  
                    String filename = items[1].substring(items[1].indexOf("=")+1);  
                    String sourceid = items[2].substring(items[2].indexOf("=")+1);        
                    //������Դid�������ҪΨһ�ԣ����Բ���UUID  
                    long id = System.currentTimeMillis();  
                    FileLogInfo log = null;  
                    if(sourceid!=null && !"".equals(sourceid))  
                    {  
                        id = Long.valueOf(sourceid);  
                        //�����ϴ����ļ��Ƿ�����ϴ���¼  
                        log = find(id);  
                    }  
                    File file = null;  
                    int position = 0;  
                    //����ϴ����ļ��������ϴ���¼,Ϊ�ļ���Ӹ��ټ�¼  
                    if(log==null)  
                    {  
                        //���ô�ŵ�λ���뵱ǰӦ�õ�λ���й�  
                        File dir = new File("c:/temp/");  
                        if(!dir.exists()) dir.mkdirs();  
                        file = new File(dir, filename);  
                        //����ϴ����ļ�����������Ȼ����и���  
                        if(file.exists())  
                        {  
                            filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                            file = new File(dir, filename);  
                        }  
                        save(id, file);  
                    }  
                    // ����ϴ����ļ������ϴ���¼,��ȡ�ϴεĶϵ�λ��  
                    else  
                    {  
                        System.out.println("FileServer have exits log not null");  
                        //���ϴ���¼�еõ��ļ���·��  
                        file = new File(log.getPath());  
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
                    
                

                    
                    
                    
                    
                    /***************test************************/
                    PrintWriter out = null;
                    try{
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    String response = "sourceid="+ id+ ";position="+ position;
                    out.println(response);
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
                    	
                        fileOutStream.write(buffer, 0, len);  
                        length += len;  
                        System.out.println("���ڽ����ļ�"+length);
                        Properties properties = new Properties();  
                        properties.put("length", String.valueOf(length));  
                        FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
                        //ʵʱ��¼�ļ�����󱣴�λ��  
                        properties.store(logFile, null);  
                        logFile.close();  
                    }  
                    //����������ȵ���ʵ�ʳ������ʾ�����ɹ�  
                    if(length==fileOutStream.length()){  
                        delete(id);  
                        
                        System.out.println("�ļ�������ɣ�ɾ����¼�ϵ��log�ļ�");
                        File deletelogfile=new File(file.getParentFile(), file.getName()+".log");
                        deletelogfile.delete();
                    }  
                    fileOutStream.close();                    
                    file = null;  
                    server.close();
                }  
            }  
            catch (Exception e)   
            {  
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
       
     /**  
      * �����ڼ�¼���Ƿ���sourceid���ļ�  
      * @param sourceid  
      * @return  
      */  
     public FileLogInfo find(Long sourceid)  
     {  
         return datas.get(sourceid);  
     }  
       
     /** 
      * �����ϴ���¼���պ���Ըĳ�ͨ�����ݿ��� 
      * @param id 
      * @param saveFile 
      */  
     public void save(Long id, File saveFile)  
     {  
         System.out.println("save logfile "+id);  
         datas.put(id, new FileLogInfo(id, saveFile.getAbsolutePath()));  
     }  
       
     /** 
      * ���ļ��ϴ���ϣ�ɾ����¼ 
      * @param sourceid 
      */  
     public void delete(long sourceid)  
     {  
         System.out.println("delete logfile "+sourceid);  
         if(datas.containsKey(sourceid)) datas.remove(sourceid);  
     }  
       
}
