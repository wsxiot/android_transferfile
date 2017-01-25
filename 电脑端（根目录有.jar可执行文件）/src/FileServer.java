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
     private ExecutorService executorService;//线程池  
     private boolean quit = false;//退出  
     private int port;//监听端口 
     private ServerSocket server=null;
     private Map<Long, FileLogInfo> datas = new HashMap<Long, FileLogInfo>();//存放断点数据,以后改为数据库存放  
     public FileServer(final int port)  
     {  
    	 this.port=port;
         //创建线程池，池中具有(cpu个数*50)条线程  
         executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);  
     }  
       
    /** 
      * 退出 
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
      * 启动服务 
      * @throws Exception 
      */  
     public void start() throws Exception{
    	 //while(true){
    		 System.out.println("开始监听链接");
    		 server = new ServerSocket(port);//实现端口监听
    		 Socket socket=server.accept();
    		 executorService.execute(new SocketTask(socket));//为支持多用户并发访问，采取线程池管理每一个用户的连接请求
    		 System.out.println("客户端以连上服务器");
    		 System.out.println("客户端以连上服务器");
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
                //得到客户端发来的第一行协议数据：Content-Length=143253434;filename=xxx.3gp;sourceid=  
                //如果用户初次上传文件，sourceid的值为空。  
                
                
                /******接收headtest***********/
                String head=null;
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


                
                
                System.out.println("FileServer head:"+head);  
                if(head!=null)  
                {  
                    //下面从协议数据中提取各项参数值  
                	System.out.println("head不为空"+head);
                    String[] items = head.split(";");  
                    String filelength = items[0].substring(items[0].indexOf("=")+1);  
                    String filename = items[1].substring(items[1].indexOf("=")+1);  
                    String sourceid = items[2].substring(items[2].indexOf("=")+1);        
                    //生成资源id，如果需要唯一性，可以采用UUID  
                    long id = System.currentTimeMillis();  
                    FileLogInfo log = null;  
                    if(sourceid!=null && !"".equals(sourceid))  
                    {  
                        id = Long.valueOf(sourceid);  
                        //查找上传的文件是否存在上传记录  
                        log = find(id);  
                    }  
                    File file = null;  
                    int position = 0;  
                    //如果上传的文件不存在上传记录,为文件添加跟踪记录  
                    if(log==null)  
                    {  
                        //设置存放的位置与当前应用的位置有关  
                        File dir = new File("c:/temp/");  
                        if(!dir.exists()) dir.mkdirs();  
                        file = new File(dir, filename);  
                        //如果上传的文件发生重名，然后进行改名  
                        if(file.exists())  
                        {  
                            filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                            file = new File(dir, filename);  
                        }  
                        save(id, file);  
                    }  
                    // 如果上传的文件存在上传记录,读取上次的断点位置  
                    else  
                    {  
                        System.out.println("FileServer have exits log not null");  
                        //从上传记录中得到文件的路径  
                        file = new File(log.getPath());  
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
                    
                

                    
                    
                    
                    
                    /***************test************************/
                    PrintWriter out = null;
                    try{
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    String response = "sourceid="+ id+ ";position="+ position;
                    out.println(response);
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
                    	
                        fileOutStream.write(buffer, 0, len);  
                        length += len;  
                        System.out.println("正在接收文件"+length);
                        Properties properties = new Properties();  
                        properties.put("length", String.valueOf(length));  
                        FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
                        //实时记录文件的最后保存位置  
                        properties.store(logFile, null);  
                        logFile.close();  
                    }  
                    //如果长传长度等于实际长度则表示长传成功  
                    if(length==fileOutStream.length()){  
                        delete(id);  
                        
                        System.out.println("文件传输完成，删除记录断点的log文件");
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
                    System.out.println("传输完成socket以关闭");
                }   
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
        }  
     }  
       
     /**  
      * 查找在记录中是否有sourceid的文件  
      * @param sourceid  
      * @return  
      */  
     public FileLogInfo find(Long sourceid)  
     {  
         return datas.get(sourceid);  
     }  
       
     /** 
      * 保存上传记录，日后可以改成通过数据库存放 
      * @param id 
      * @param saveFile 
      */  
     public void save(Long id, File saveFile)  
     {  
         System.out.println("save logfile "+id);  
         datas.put(id, new FileLogInfo(id, saveFile.getAbsolutePath()));  
     }  
       
     /** 
      * 当文件上传完毕，删除记录 
      * @param sourceid 
      */  
     public void delete(long sourceid)  
     {  
         System.out.println("delete logfile "+sourceid);  
         if(datas.containsKey(sourceid)) datas.remove(sourceid);  
     }  
       
}
