package event_org;
import java.lang.Thread;
import java.time.ZonedDateTime;
class Sync implements Runnable {
	Thread t1;
	
	Sync()
	{
		t1=new Thread(this);
	}
	public void run()
	{
		while(!t1.isInterrupted())
		{
			
//			if(FileIO.UpdateReceivedInvitations() && FileIO.UpdateUpcomingUserEvents() && FileIO.UpdateResponseOfInvitations()
//				&& FileIO.UpdateReceivedAndSentFriendRequests()) UI_utility.PrintError("\nError occured while syncing data\n");
			if(!FileIO.UpdateReceivedInvitations()) UI_utility.PrintError("\nError occured while syncing Received Invitation\n");
			if(!FileIO.UpdateUpcomingUserEvents()) UI_utility.PrintError("\nError occured while syncing Upcomming events\n");
			if(!FileIO.UpdateResponseOfInvitations()) UI_utility.PrintError("\nError occured while syncing Respose of Invitation\n");
			if(!FileIO.UpdateReceivedAndSentFriendRequests()) UI_utility.PrintError("\nError occured while syncing frind requests\n");
			UI.CurrentSession.LastSessionTime=ZonedDateTime.now();
			//UI_utility.PrintSuccess("Sync till "+UI.CurrentSession.LastSessionTime.toString());
			try {
				Thread.sleep(30000);
			}
			catch(java.lang.InterruptedException e)
			{
				break;
			}
			
		}
	}

}
