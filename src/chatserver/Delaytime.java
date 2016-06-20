package chatserver;

/**
 * Class used by server to control the delay time used to block users
 * 
 * @author Yan NA
 *
 */

public class Delaytime implements Runnable
{
	Connection kickeduser;
	Room theroom;
	Long thetime=(long) 0;
	public Delaytime(Connection user,Room room,Long sleeptime)
	{
		kickeduser=user;
		theroom=room;
		thetime=sleeptime;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Thread.currentThread();
			Thread.sleep(thetime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		theroom.removeblockid(kickeduser);	
		
	}
	
}
