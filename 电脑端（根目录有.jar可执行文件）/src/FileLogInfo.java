import java.io.File;


public class FileLogInfo {
	public Long id;
	public String path;
	public FileLogInfo(Long id,String path){
		this.id=id;
		this.path=path;
	}
	public String getPath(){
		return this.path;
	}
	
}
