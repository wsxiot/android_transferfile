class MyThread extends Thread{
	private FileServer fileserver;
	public MyThread(FileServer fileserver){
		this.fileserver=fileserver;
	}
	public void run(){
		try {
			System.out.println("mythread �߳̿�ʼ");
			fileserver.start();
			System.out.println("���ӳɹ�");
		} catch (Exception e) {				// TODO Auto-generated catch block
			System.out.println("�����쳣3");
			e.printStackTrace();
		}
		}
	}