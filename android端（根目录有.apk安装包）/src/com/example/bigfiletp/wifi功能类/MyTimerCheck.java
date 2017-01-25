/*
 *@author Dawin,2015-3-5
 *
 *
 *
 */
package com.example.bigfiletp.wifi功能类;
/*在进行热点连接时用到的检查类，用于检查连接是否超时；
 * 一个abstract(抽象类)，该类必需被继承使用；
 * 不可生成对象；
 * abstract可以将子类的共性最大程度的抽取出来，实现程序的简洁性；
 * */
public abstract class MyTimerCheck
{
	private int mCount = 0;
	private int mTimeOutCount = 1;
	private int mSleepTime = 1000; // 1s
	private boolean mExitFlag = false;
	private Thread mThread = null;

	/**
	 * Do not process UI work in this.
	 */
	public abstract void doTimerCheckWork();

	public abstract void doTimeOutWork();

	public MyTimerCheck() {
		mThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				while (!mExitFlag)
					{
						mCount++;
						if (mCount < mTimeOutCount)
							{
								doTimerCheckWork();
								try
									{
										mThread.sleep(mSleepTime);
									} catch (InterruptedException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
										exit();
									}
							} else
							{
								doTimeOutWork();
							}
					}
			}
		});
	}

	/**
	 * start
	 * 
	 * @param times
	 *            How many times will check?
	 * @param sleepTime
	 *            ms, Every check sleep time.
	 */
	public void start(int timeOutCount, int sleepTime)
	{
		mTimeOutCount = timeOutCount;
		mSleepTime = sleepTime;

		mThread.start();
	}

	public void exit()
	{
		mExitFlag = true;
	}

}