class MyThread extends Thread{
	private FileServer fileserver;
	public MyThread(FileServer fileserver){
		this.fileserver=fileserver;
	}
	public void run(){
		try {
			System.out.println("mythread 线程开始");
			fileserver.start();
			System.out.println("连接成功");
		} catch (Exception e) {				// TODO Auto-generated catch block
			System.out.println("连接异常3");
			e.printStackTrace();
		}
		}
	}