package event_org;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

class FileIO {
	private static final String USER_FILE = "root/Users.txt";
	private static final String INVITATION_FILE = "root/Invitaton.txt";
	private static final String FRIEND_REQUEST_FILE = "root/FriendRequest.txt";
	private static final String FRIEND_RESPONSE_FILE="root/FriendResponse.txt";
	private static final String RSVP_FILE = "root/RSVP.txt";
	private static final String CREATED_EVENT_FILE="root/CreatedEvents.txt";
	private static final String EDITED_EVENT_FILE="root/EditedEvents.txt";
	private static final String DELETED_EVENT_FILE="root/DeletedEvents.txt";
	static boolean CreateFolder(String path)
	{
		File dir = new File(path);
        if (!dir.exists()) 
        {
            if(!dir.mkdirs())
            {
            	return false;
            }
        }
        return true;
	}
	static boolean CreateFile(String path_filename)
	{
		File file = new File(path_filename);
        if (!file.exists()) {
            try{file.createNewFile();}
            catch(Exception e)
            {
            	e.printStackTrace();
            	return false;
            }
        }
        return true;
	}
	public static boolean deleteFolder(String foldername) 
	{
		File folder=new File(foldername);
		if(!folder.exists()) return true;
		try
		{
			File[] files = folder.listFiles();

			if(files != null) 
			{
				for(File f : files)
				{

					if(f.isDirectory())
					{
						if(!deleteFolder(f.getAbsolutePath()))
		                    return false;
					}// recursive call
					 else 
				   	{
						 if(!f.delete())
			                    return false;
					 }
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	    return folder.delete();
	}
	static boolean InitializeFiles()
	{
		if(CreateFolder("root")&& CreateFile(USER_FILE)&& CreateFile(INVITATION_FILE )&&
				CreateFile(CREATED_EVENT_FILE)&&CreateFile(EDITED_EVENT_FILE)&&
				CreateFile(DELETED_EVENT_FILE)&&CreateFile(RSVP_FILE)&&CreateFile(FRIEND_REQUEST_FILE)&&
				CreateFile(FRIEND_RESPONSE_FILE))
			return true;
		else return false;
	}
	static boolean EnterFriendRequest(FriendRequest Fr)
	{
		try
		{
			BufferedWriter writer= new BufferedWriter(new FileWriter(FileIO.FRIEND_REQUEST_FILE,true));
			writer.write(Fr.getFrom()+","+Fr.getTo()+","+ZonedDateTime.now());
			writer.newLine();
			writer.close();
			writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_sent_request_file,true));
			writer.write(Fr.getTo());
			writer.newLine();
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean RespondToRequest(FriendRequest Fr)
	{
		ArrayList<FriendRequest>Fr_list=new ArrayList<>();
		String from;
		try 
		{
			BufferedWriter writer= new BufferedWriter(new FileWriter(FileIO.FRIEND_RESPONSE_FILE,true));
			writer.write(Fr.getFrom()+","+Fr.getTo()+","+Fr.getResponse().toString()+","+ZonedDateTime.now().toString());
			writer.newLine();
			writer.close();
			
			BufferedReader reader=new BufferedReader(new FileReader(UI.CurrentSession.User_received_request_file));
			from=reader.readLine().trim();
			while(from!=null)
			{
				from=from.trim();
				if(from.isBlank()||from.length()!=10)
				{
					from=reader.readLine();
					continue;
				}
				Fr_list.add(new FriendRequest(from,UI.CurrentSession.CurrentUser.getUsername()));
				from=reader.readLine();
			}
			reader.close();
			writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_received_request_file,false));
			for(int i=0;i<Fr_list.size();i++)
			{
				if(Fr_list.get(i).getFrom().equals(Fr.getFrom())) continue;
				writer.write(Fr_list.get(i).getFrom());
				writer.newLine();
			}
			writer.close();
			return true;			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean EnterNewInvitation(Invitation iv)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(INVITATION_FILE,true));
			writer.write(iv.getID()+","+iv.getFrom().getID()+","+iv.getTo().getID()+","+ZonedDateTime.now().toString());
			writer.newLine();
			writer.close();
			
			String EventID=iv.getEventID();
			writer=new BufferedWriter(new FileWriter(UI.CurrentSession.Event_Folder+"/"+EventID+"/invited.txt",true));
			writer.write(iv.getID()+","+iv.getFrom().getID()+","+iv.getTo().getID());
			writer.newLine();
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean RespondToInvitation(Invitation iv)
	{
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(RSVP_FILE,true)))
		{
			writer.write(iv.getID()+","+iv.getFrom().getID()+","+iv.getTo().getID()+","+iv.getStatus()+","+ZonedDateTime.now().toString());
			writer.newLine();
			BufferedWriter writer_user;
			if(iv.getStatus().equals(RSVP.MAYBE)|| iv.getStatus().equals(RSVP.ACCEPTED))
			{
				writer_user=new BufferedWriter(new FileWriter(UI.CurrentSession.User_upcomingevent_file,true));
				Event ev=FileIO.GetRootEvent(iv.getEventID())[0];
				writer_user.write(ev.getID()+","+ev.getName()+","+ev.getDescription()+","+ev.getLocation()+","+ev.getEventType()+","+ev.getEvent_DateTime());
				writer_user.newLine();
				writer_user.close();
			}
			// yet to complete.
			Invitation Received_iv[]=FileIO.GetReceivedInvitations();
			writer_user=new BufferedWriter(new FileWriter(UI.CurrentSession.User_invitations_file));
			for(int i=0;i<Received_iv.length;i++)
			{
				if(Received_iv[i]==null) continue;
				if(Received_iv[i].getID().equals(iv.getID()))
				{
					continue;
				}
				writer_user.write(Received_iv[i].getID()+","+Received_iv[i].getFrom().getID()+","+Received_iv[i].getTo().getID());
				writer_user.newLine();
			}
			writer_user.close();
			return true;
		}
		catch(IOException e)
		{
			e.getStackTrace();
			return false;
		}
	}
	static boolean EnterNewUser(User us)
	{
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE,true)))
		{
			writer.write(us.getID()+','+us.getUsername()+','+us.getEmailaddress()+','+us.getPassword());
			writer.newLine();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean EnterNewEvent(Event ev)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(CREATED_EVENT_FILE,true));
			writer.write(ev.getID()+","+ev.getName()+","+ev.getDescription()+","+ev.getLocation()+","+ev.getEventType().toString()+","+ev.getEvent_DateTime().toString());
			writer.newLine();
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_events_file,true));
			writer.write(ev.getID()+","+ev.getName()+","+ev.getDescription()+","+ev.getLocation()+","+ev.getEventType().toString()+","+ev.getEvent_DateTime().toString());
			writer.newLine();
			writer.close();
			
			String new_ev_folder=new String(UI.CurrentSession.Event_Folder+"/"+ev.getID());
			CreateFolder(new_ev_folder);
			CreateFile(new_ev_folder+"/invited.txt");
			CreateFile(new_ev_folder+"/RSVP.txt");
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean EditEvent(Event ev)
	{
		Event events_list[];
		if(ev==null) return false;
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(EDITED_EVENT_FILE,true));
			writer.write(ev.getID()+","+ev.getName()+","+ev.getDescription()+","+ev.getLocation()+","+ev.getEventType().toString()+","+ev.getEvent_DateTime().toString());
			writer.newLine();
			writer.close();
			
			events_list=FileIO.GetUserEvent();
			writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_events_file,false));
			for(int i=0;i<events_list.length;i++)
			{
				if(ev.getID().equals(events_list[i].getID()))
				{
					events_list[i]=ev;
				}
				writer.write(events_list[i].getID()+","+events_list[i].getName()+","+events_list[i].getDescription()+","+events_list[i].getLocation()+","+events_list[i].getEventType().toString()+","+events_list[i].getEvent_DateTime().toString());
				writer.newLine();
			}
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean DeleteEvent(Event ev)
	{
		Event events_list[];
		if(ev==null) return false;
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(DELETED_EVENT_FILE,true));
			writer.write(ev.getID()+","+ev.getName()+","+ev.getDescription()+","+ev.getLocation()+","+ev.getEventType().toString()+","+ev.getEvent_DateTime().toString());
			writer.newLine();
			writer.close();
			
			events_list=FileIO.GetUserEvent();
			writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_events_file,false));
			for(int i=0;i<events_list.length;i++)
			{
				if(ev.getID().equals(events_list[i].getID()))
				{
					continue;
				}
				writer.write(events_list[i].getID()+","+events_list[i].getName()+","+events_list[i].getDescription()+","+events_list[i].getLocation()+","+events_list[i].getEventType().toString()+","+events_list[i].getEvent_DateTime().toString());
				writer.newLine();
			}
			writer.close();
			
			if(!FileIO.deleteFolder(UI.CurrentSession.Event_Folder+"/"+ev.getID()))
			{
				UI.PrintError("Fatal error occured while deleting Event Files.");
				return false;
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static String Create_UserID()
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(USER_FILE)))
		{
			String temp=null,last_ID=null;
			try
			{
				temp=reader.readLine();
				while(temp!=null)
				{
					last_ID=temp;
					temp=reader.readLine();
				}
				if(last_ID==null) return "0000000000";
				
				else
				{
					last_ID=last_ID.substring(0, 10);
					int index=last_ID.length()-1;
					char []c=last_ID.toCharArray();;
					while(index>=0)
					{
						
						if(c[index]<'9')
						{
							c[index]++;
							break;
						}
						else if(c[index]=='9')
						{
							c[index]='A';
							break;
						}
						else if (c[index]<'Z')
						{
							c[index]++;
							break;
						}
						else
						{
							c[index]='0';
							index--;
						}
					}
					return new String(c);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	static String Create_EventID()
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(CREATED_EVENT_FILE)))
		{
			String temp=null,last_ID=null;
			try
			{
				temp=reader.readLine();
				while(temp!=null)
				{
					last_ID=temp;
					temp=reader.readLine();
				}
				if(last_ID==null) return "0000000000";
				
				else
				{
					last_ID=last_ID.substring(0, 10);
					int index=last_ID.length()-1;
					char []c=last_ID.toCharArray();;
					while(index>=0)
					{
						
						if(c[index]<'9')
						{
							c[index]++;
							break;
						}
						else if(c[index]=='9')
						{
							c[index]='A';
							break;
						}
						else if (c[index]<'Z')
						{
							c[index]++;
							break;
						}
						else
						{
							c[index]='0';
							index--;
						}
					}
					return new String(c);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	static String Create_InviteID(Event ev)
	{
		String EventID=ev.getID();
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.Event_Folder+"/"+EventID+"/invited.txt")))
		{
			String temp=null,last_ID=null;
			try
			{
				temp=reader.readLine();
				while(temp!=null)
				{
					if(temp.isBlank())
					{
						temp=reader.readLine();
						continue;
					}
					last_ID=temp;
					temp=reader.readLine();
				}
				if(last_ID==null) return EventID+"."+"0000000000";
				
				else
				{
					last_ID=last_ID.substring(0, 21);
					last_ID=last_ID.substring(11, last_ID.length());
					int index=last_ID.length()-1;
					char []c=last_ID.toCharArray();;
					while(index>=0)
					{
						
						if(c[index]<'9')
						{
							c[index]++;
							break;
						}
						else if(c[index]=='9')
						{
							c[index]='A';
							break;
						}
						else if (c[index]<'Z')
						{
							c[index]++;
							break;
						}
						else
						{
							c[index]='0';
							index--;
						}
					}
					return  EventID+"."+ new String(c);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	static Event[] GetUserEvent()
	{
		ArrayList<Event> ev = new ArrayList<>();
		String ID,name,description,location,temp;
		Event_t type;
		ZonedDateTime Event_DateTime;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_events_file));
			temp=reader.readLine();
			int index;
			while(temp!=null)
			{
				
				if (temp.isBlank())
				{
					temp = reader.readLine(); 
					continue;
				}
				ID=temp.substring(0,10);
				temp=temp.substring(11);//remove ID
				
				index=temp.indexOf(',');
				name=temp.substring(0,index);
				temp=temp.substring(index+1);//remove name
				
				index=temp.indexOf(',');
				description=temp.substring(0,index);
				temp=temp.substring(index+1);//remove description
				
				index=temp.indexOf(',');
				location=temp.substring(0,index);
				temp=temp.substring(index+1);//remove location
				
				index=temp.indexOf(',');
				type=Event_t.valueOf(temp.substring(0,index));
				temp=temp.substring(index+1);//remove event type
				
				Event_DateTime=ZonedDateTime.parse(temp);
				
				ev.add(new Event(ID,name,description,location,type,Event_DateTime));
				temp=reader.readLine();
			}
			reader.close();
			return ev.toArray(new Event[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Event[0];
		}
	}
	static Event[] GetUserEvent(String ID)
	{
		Event ev[];
		String name,description,location,temp;
		Event_t type;
		ZonedDateTime Event_DateTime;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_events_file));
	
			int index;
			temp=reader.readLine();
			while(temp!=null)
			{
				
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				if(!ID.equals(temp.substring(0,10)))
				{
					temp=reader.readLine();
					continue;
				}
				temp=temp.substring(11);//remove ID
				
				index=temp.indexOf(',');
				name=temp.substring(0,index);
				temp=temp.substring(index+1);//remove name
				
				index=temp.indexOf(',');
				description=temp.substring(0,index);
				temp=temp.substring(index+1);//remove description
				
				index=temp.indexOf(',');
				location=temp.substring(0,index);
				temp=temp.substring(index+1);//remove location
				
				index=temp.indexOf(',');
				type=Event_t.valueOf(temp.substring(0,index));
				temp=temp.substring(index+1);//remove event type
				
				Event_DateTime=ZonedDateTime.parse(temp);
				
				ev=new Event[1];
				ev[0]=new Event(ID,name,description,location,type,Event_DateTime);
				reader.close();
				return ev;
			}
			reader.close();
			return new Event[0];
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Event[0];
		}
	}
	static Event[] GetRootEvent(String ID)
	{
		Event ev[]=null;
		String name,description,location,temp;
		Event_t type;
		ZonedDateTime Event_DateTime;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(FileIO.DELETED_EVENT_FILE));
	
			int index;
			temp=reader.readLine();
			while(temp!=null)
			{
				
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				if(!ID.equals(temp.substring(0,10)))
				{
					temp=reader.readLine();
					continue;
				}
				reader.close();
				return new Event[0];
			}
			reader.close();
			
			reader = new BufferedReader(new FileReader(FileIO.EDITED_EVENT_FILE));
			temp=reader.readLine();
			while(temp!=null)
			{
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				if(!ID.equals(temp.substring(0,10)))
				{
					temp=reader.readLine();
					continue;
				}
				temp=temp.substring(11);//remove ID
				
				index=temp.indexOf(',');
				name=temp.substring(0,index);
				temp=temp.substring(index+1);//remove name
				
				index=temp.indexOf(',');
				description=temp.substring(0,index);
				temp=temp.substring(index+1);//remove description
				
				index=temp.indexOf(',');
				location=temp.substring(0,index);
				temp=temp.substring(index+1);//remove location
				
				index=temp.indexOf(',');
				type=Event_t.valueOf(temp.substring(0,index));
				temp=temp.substring(index+1);//remove event type
				
				Event_DateTime=ZonedDateTime.parse(temp);
				
				ev=new Event[1];
				ev[0]=new Event(ID,name,description,location,type,Event_DateTime);
			}
			reader.close();
			if(ev!=null)
			{
				return ev;
			}
			
			reader = new BufferedReader(new FileReader(FileIO.CREATED_EVENT_FILE));
			temp=reader.readLine();
			while(temp!=null)
			{
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				if(!ID.equals(temp.substring(0,10)))
				{
					temp=reader.readLine();
					continue;
				}
				temp=temp.substring(11);//remove ID
				
				index=temp.indexOf(',');
				name=temp.substring(0,index);
				temp=temp.substring(index+1);//remove name
				
				index=temp.indexOf(',');
				description=temp.substring(0,index);
				temp=temp.substring(index+1);//remove description
				
				index=temp.indexOf(',');
				location=temp.substring(0,index);
				temp=temp.substring(index+1);//remove location
				
				index=temp.indexOf(',');
				type=Event_t.valueOf(temp.substring(0,index));
				temp=temp.substring(index+1);//remove event type
				
				
				Event_DateTime=ZonedDateTime.parse(temp);
				ev=new Event[1];
				ev[0]=new Event(ID,name,description,location,type,Event_DateTime);
				reader.close();
				return ev;
			}
			reader.close();
			return new Event[0];
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Event[0];
		}
	}
	static Invitation[] GetReceivedInvitations()
	{
		ArrayList<Invitation> iv = new ArrayList<>();
		String temp,ID;
		User From,To;
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_invitations_file)))
		{
			int index;
			temp=reader.readLine();
			while(temp!=null)
			{
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(',');
				if(index==-1)
				{
					temp = reader.readLine();
				    continue;
				}
				ID=temp.substring(0,index);//remove 10+1+10
				temp=temp.substring(index+1);//remove ID
				
				index=temp.indexOf(',');
				From=SearchUserID(temp.substring(0,index));
				temp=temp.substring(index+1);
				
				To=SearchUserID(temp);
				
				iv.add(new Invitation(ID,From,To));
				temp=reader.readLine();
			}
			reader.close();
			return iv.toArray(new Invitation[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Invitation[0];
		}
	}
	static Event[] GetUpcomingUserEvents()
	{
		ArrayList<Event> ev = new ArrayList<>();
		String ID,name,description,location,temp;
		Event_t type;
		ZonedDateTime Event_DateTime;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_upcomingevent_file));
			temp=reader.readLine();
			int index;
			while(temp!=null)
			{
				
				if (temp.isBlank())
				{
					temp = reader.readLine(); 
					continue;
				}
				ID=temp.substring(0,10);
				temp=temp.substring(11);//remove ID
				
				index=temp.indexOf(',');
				name=temp.substring(0,index);
				temp=temp.substring(index+1);//remove name
				
				index=temp.indexOf(',');
				description=temp.substring(0,index);
				temp=temp.substring(index+1);//remove description
				
				index=temp.indexOf(',');
				location=temp.substring(0,index);
				temp=temp.substring(index+1);//remove location
				
				index=temp.indexOf(',');
				type=Event_t.valueOf(temp.substring(0,index));
				temp=temp.substring(index+1);//remove event type
				
				Event_DateTime=ZonedDateTime.parse(temp);
				
				ev.add(new Event(ID,name,description,location,type,Event_DateTime));
				temp=reader.readLine();
			}
			reader.close();
			return ev.toArray(new Event[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new Event[0];
		}
		
	}
	static Invitation[] GetUserEventResponse(Event ev)
	{
		
		HashMap<String,Invitation> map = new HashMap<>();
		String temp,FullID;
		User From,To;
		RSVP response;
		try(BufferedReader invi_reader=new BufferedReader(new FileReader(UI.CurrentSession.Event_Folder+"/"+ev.getID()+"/invited.txt")))
		{
			temp=invi_reader.readLine();
			
			int index;
			while(temp!=null)
			{
				if(temp.isBlank())
				{
					temp=invi_reader.readLine();
					continue;
				}
				index=temp.indexOf(',');
				if(temp.isBlank())
				{
					temp=invi_reader.readLine();
					continue;
				}
				FullID=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(',');
				From=SearchUserID(temp.substring(0,index));
				temp=temp.substring(index+1);
				
				To=SearchUserID(temp);
				
				map.put(FullID, new Invitation(FullID,From,To));
				temp=invi_reader.readLine();
			}
			try(BufferedReader RSVP_reader=new BufferedReader(new FileReader(UI.CurrentSession.Event_Folder + "/" + ev.getID() + "/RSVP.txt")))
			{
				temp=RSVP_reader.readLine();
				while(temp!=null)
				{
					if(temp.isBlank())
					{
						temp=RSVP_reader.readLine();
						continue;
					}
					
					index=temp.indexOf(',');
					if(index==-1)
					{
						temp=RSVP_reader.readLine();
						continue;
					}
					FullID=temp.substring(0,index);
					temp=temp.substring(index+1);
					
					index=temp.indexOf(',');
					//From=SearchUserID(temp.substring(0,index));
					temp=temp.substring(index+1);
					
					index=temp.indexOf(',');
					//To=SearchUserID(temp.substring(0,index));
					temp=temp.substring(index+1);
					
					response=RSVP.valueOf(temp);
					
					Invitation existing = map.get(FullID);
					if (existing != null) 
					{
					    existing.setStatus(response);
					}
				}
			}
			return map.values().toArray(new Invitation[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			UI.PrintError("File Error occured while reading files");
			return new Invitation[0];
		}
	}
	static FriendRequest[] GetFriendRequests()
	{
		ArrayList<FriendRequest> fr = new ArrayList<>();
		String temp;
		String From;
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_received_request_file)))
		{
			temp=reader.readLine();
			while(temp!=null)
			{
				temp=temp.trim();
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				From=temp;
				
				fr.add(new FriendRequest(From,UI.CurrentSession.CurrentUser.getID()));
				temp=reader.readLine();
			}
			return fr.toArray(new FriendRequest[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new FriendRequest[0];
		}
	}
	static FriendRequest[] GetSentRequests()
	{
		ArrayList<FriendRequest> fr = new ArrayList<>();
		String temp;
		String To;
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_sent_request_file)))
		{
			temp=reader.readLine();
			while(temp!=null)
			{
				temp=temp.trim();
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				To=temp;
				
				fr.add(new FriendRequest(UI.CurrentSession.CurrentUser.getID(),To));
				temp=reader.readLine();
			}
			return fr.toArray(new FriendRequest[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return new FriendRequest[0];
		}
	}
	static User SearchUserID(String ID)
	{
		String emailaddress,name,password;
		try(BufferedReader reader = new BufferedReader(new FileReader("root/Users.txt")))
		{
			String test;
			int index;
			test=reader.readLine();
			while(test!=null)
			{
				index=10;
				if(test.substring(0,index).equals(ID))
				{
					test=test.substring(11);//ID removed
					index=test.indexOf(',');
					
					name=test.substring(0, index);
					test=test.substring(index+1);//Name removed
					
					index=test.indexOf(',');
					emailaddress=test.substring(0,index);
					
					password=test.substring(index+1);//Email Address removed we only have password now
					return new User(ID,name,emailaddress,password);
				}
				test=reader.readLine();
			}
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			UI.PrintError("File System Error occured");
			return null;
		}
		
	}
	
	static User SearchUsername(String username)
	{
		String emailaddress,ID,password;
		try(BufferedReader reader = new BufferedReader(new FileReader("root/Users.txt")))
		{
			String test;
			int index;
			test=reader.readLine();
			while(test!=null)
			{
				ID=test.substring(0,10);
				test=test.substring(11);//ID removed
				index=test.indexOf(',');
				if(test.substring(0,index).equals(username))
				{
					test=test.substring(index+1); //Username removed
					index=test.indexOf(',');
					
					emailaddress=test.substring(0,index);
					password=test.substring(index+1);//Email Address removed we only have password now
					return new User(ID,username,emailaddress,password);
				}
				test=reader.readLine();
			}
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			UI.PrintError("File System Error occured");
			return null;
		}
	}
	static User SearchUserforAuthentication(String username,String password)
	{
		User us=FileIO.SearchUsername(username);
		if(us==null)
			return null;
		if(us.getPassword().equals(password)) return us;
		else return null;
	}
	static boolean SetUserInfo()
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_Info_file)))
		{
			UI.CurrentSession.UserInfo=reader.readLine();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			UI.CurrentSession.UserInfo=null;
			return false;
		}
	}
	static boolean SetUserFriends()
	{
		String ID,name,temp;
		//int index=10;//till ID
		
		try(BufferedReader reader = new BufferedReader(new FileReader(UI.CurrentSession.User_friendlist_file)))
		{
			temp=reader.readLine();
			UI.CurrentSession.CurrentUser.clearFriendList();
			while(temp!=null)
			{
				if (temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				
				String[] parts = temp.split(",", 2);
				if(parts.length < 2) 
				{
					temp=reader.readLine();
					continue;
				}
				
				ID = parts[0];
				name = parts[1];
				User new_Profile=SearchUserID(ID);
				if(!name.equals(new_Profile.getUsername()))
				{
					UI.CurrentSession.Notifications.add(new Notification("Friend with ID: "+ID+" Changed their Username form "+name+" to "+new_Profile.getUsername()+"."
				,false,ZonedDateTime.now()));
					name=new_Profile.getUsername();
				}
				UI.CurrentSession.CurrentUser.addFriend(new User(ID,name));
				temp=reader.readLine();
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean SetUserNotifications()
	{
		String temp;
		int index;
		String message;
		boolean seen;
		ZonedDateTime dt;
		try(BufferedReader reader=new BufferedReader(new FileReader(UI.CurrentSession.User_notification_file)))
		{
			temp=reader.readLine();
			while(temp!=null)
			{
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(',');
				if(index==-1)
				{
					temp=reader.readLine();
					continue;
				}
				message=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(',');
				if(temp.substring(0,index).equals("true"))
				{
					seen=true;
				}
				else
				{
					seen=false;
				}
				temp=temp.substring(index+1);
				
				dt=ZonedDateTime.parse(temp);
				UI.CurrentSession.Notifications.add(new Notification(message,seen,dt));
				temp=reader.readLine();
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean SaveUserInfo()
	{
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_Info_file)))
		{
			writer.write(UI.CurrentSession.StartTime.toString());
			writer.newLine();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		//yet to do
	}
	static boolean SaveUserFriends()
	{
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_friendlist_file,false)))
		{
			for(User us:UI.CurrentSession.CurrentUser.getFriend())
			{
				if(us==null)continue;
				writer.write(us.getID()+"," +us.getUsername());
				writer.newLine();
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean SaveUserNotification()
	{
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(UI.CurrentSession.User_notification_file,false)))
		{
			for(int i=0;i<UI.CurrentSession.Notifications.size();i++)
			{
				if(UI.CurrentSession.Notifications.get(i)==null) continue;
				writer.write(UI.CurrentSession.Notifications.get(i).GetNotification()+","+UI.CurrentSession.Notifications.get(i).getView_Status()+","+UI.CurrentSession.Notifications.get(i).getGenerationTime().toString());    
				writer.newLine();
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean ChangeUserProfile()
	{
		ArrayList<User> Users =new ArrayList<>();
		try(BufferedReader reader=new BufferedReader(new FileReader(FileIO.USER_FILE)))
		{
			String temp,ID,username,password,emailaddress;
			int index;
			temp=reader.readLine();
			while(temp!=null)
			{
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(",");
				if(index==-1) break;
				
				ID=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(",");
				username=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(",");
				emailaddress=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				password=temp;
				if(ID.equals(UI.CurrentSession.CurrentUser.getID()))
				{
					username=UI.CurrentSession.CurrentUser.getUsername();
					password=UI.CurrentSession.CurrentUser.getPassword();
					emailaddress=UI.CurrentSession.CurrentUser.getEmailaddress();
				}
				Users.add(new User(ID,username,emailaddress,password));
				temp=reader.readLine();
			}
			reader.close();
			try(BufferedWriter writer =new BufferedWriter(new FileWriter(FileIO.USER_FILE,false)))
			{
				for(int i=0;i<Users.size();i++)
				{
					writer.write(Users.get(i).getID()+","+Users.get(i).getUsername()+","+Users.get(i).getEmailaddress()+","+Users.get(i).getPassword());
					writer.newLine();
				}
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean UpdateReceivedInvitations()
	{
		Invitation invite_list[];
		String Event_ID;
		Event Ev[];
		String temp,FullInvite_ID,From,To;
		try
		{
			invite_list=FileIO.GetReceivedInvitations();
			BufferedWriter writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_invitations_file,false));
			
			for(int i=0;i<invite_list.length;i++)
			{
				if(invite_list[i]==null) continue;
				Event_ID=invite_list[i].getEventID();
				Ev=FileIO.GetRootEvent(Event_ID);
				if(Ev.length==0 || Ev[0].getEvent_DateTime().isBefore(ZonedDateTime.now()))//Check if Event is deleted/edited or is finished
				{
					continue;
				}
				writer.write(invite_list[i].getID()+","+invite_list[i].getFrom().getID()+","+invite_list[i].getTo().getID());
				writer.newLine();
			}
			try(BufferedReader reader=new BufferedReader(new FileReader(FileIO.INVITATION_FILE)))
			{
				int index;
				temp=reader.readLine();
				ZonedDateTime sent_time;
				while(temp!=null)
				{
					if(temp.isBlank()||temp.isEmpty())
					{
						temp=reader.readLine();
						continue;
					}
					index=temp.indexOf(',');
					if(index==-1)break;
					FullInvite_ID=temp.substring(0,index);
					temp=temp.substring(index+1);
					
					index=temp.indexOf(',');
					From=temp.substring(0,index);
					temp=temp.substring(index+1);
					
					index=temp.indexOf(',');
					To=temp.substring(0,index);
					temp=temp.substring(index+1);
					
					sent_time=ZonedDateTime.parse(temp);
					
					Event_ID=FullInvite_ID.substring(0,10);
					Ev=FileIO.GetRootEvent(Event_ID);
					if(To.equals(UI.CurrentSession.CurrentUser.getID()) &&sent_time.isAfter(UI.CurrentSession.LastSessionTime)
							&& Ev.length!=0 && Ev[0].getEvent_DateTime().isAfter(ZonedDateTime.now()))//check if id matches and the invitation is latest and the event is still there
					{
						UI.CurrentSession.Notifications.add(new Invite_Notification(new Invitation(FullInvite_ID,From,To),sent_time));
						writer.write(FullInvite_ID+","+From+","+To);
						writer.newLine();
					}
					temp=reader.readLine();
				}
			}
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean UpdateUpcomingUserEvents()
	{
		Event Upcomingev[]=FileIO.GetUpcomingUserEvents();
		Event rootev[];
		try
		{
			
			BufferedWriter writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_upcomingevent_file,false));
			for(int i=0;i<Upcomingev.length;i++)
			{
				rootev=FileIO.GetRootEvent(Upcomingev[i].getID());
				if(rootev.length==0)
				{
					UI.CurrentSession.Notifications.add(new EventCancel_Notification(Upcomingev[i]));//added notification
					continue;//see if event is cancelled
				}
				if(!Upcomingev[i].equals(rootev[0]))//see for edit in events
				{
					UI.CurrentSession.Notifications.add(new EventChange_Notification(Upcomingev[i],rootev[0]));//added notification
					Upcomingev[i]=rootev[0];
				}
				if(Upcomingev[i].getEvent_DateTime().isBefore(ZonedDateTime.now()))//See if event is done
				{
					continue;
				}
				writer.write(Upcomingev[i].getID()+","+Upcomingev[i].getName()+","+Upcomingev[i].getDescription()+","+Upcomingev[i].getLocation()+","+Upcomingev[i].getEventType()+","+Upcomingev[i].getEvent_DateTime());
			}
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean UpdateResponseOfInvitations()
	{
		String temp,Event_ID,Full_ID;
		int index;
		String From_ID,To_ID,Status;
		Event User_events[]=GetUserEvent();
		ZonedDateTime ResponseTime;
		try(BufferedReader reader=new BufferedReader(new FileReader(FileIO.RSVP_FILE)))
		{
			temp=reader.readLine();
			while(temp!=null)
			{
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(',');
				Full_ID=temp.substring(0, index);
				Event_ID=Full_ID.substring(0,10);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(',');
				From_ID=temp.substring(0, index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(',');
				To_ID=temp.substring(0, index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(',');
				Status=temp.substring(0, index);
				temp=temp.substring(index+1);
				
				ResponseTime=ZonedDateTime.parse(temp);
				for(Event e:User_events)
				{
					if(e.getID().equals(Event_ID))
					{	
						if(ResponseTime.isAfter(UI.CurrentSession.LastSessionTime)&&
								ResponseTime.isBefore(e.getEvent_DateTime()))
						{
							try (BufferedWriter writer = new BufferedWriter(new FileWriter(UI.CurrentSession.Event_Folder + "/" + Event_ID + "/RSVP.txt", true)))
							{
								//yet to create notification
								UI.CurrentSession.Notifications.add(new Response_Notification(new Invitation(Full_ID,From_ID,To_ID,RSVP.valueOf(Status)),ResponseTime));
								writer.write(Full_ID+","+From_ID+","+To_ID+","+Status);
								writer.newLine();
								break;
							}
						}
					}
				}
				temp=reader.readLine();
			}
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	static boolean UpdateReceivedAndSentFriendRequests()
	{
		String temp;
		String From;
		String To;
		ZonedDateTime sent_time;
		int index;
		RequestResponse response;
		try
		{
			BufferedReader reader=new BufferedReader(new FileReader(FileIO.FRIEND_REQUEST_FILE));
			BufferedWriter writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_received_request_file,true));
			temp=reader.readLine();
			while(temp!=null)
			{
				temp=temp.trim();
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(",");
				if(index==-1)
				{
					temp=reader.readLine();
					continue;
				}
				From=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				index=temp.indexOf(",");
				To=temp.substring(0,index);
				temp=temp.substring(index+1);
				
				sent_time=ZonedDateTime.parse(temp);
				
				if(To.equals(UI.CurrentSession.CurrentUser.getID())&&sent_time.isAfter(UI.CurrentSession.LastSessionTime))
				{
					writer.write(From);
					writer.newLine();
				}
				temp=reader.readLine();
			}
			writer.close();
			reader.close();
			ArrayList<FriendRequest> fr_list=new ArrayList<>();
			reader=new BufferedReader(new FileReader(UI.CurrentSession.User_sent_request_file));
			//Backing up Sent_request file
			temp=reader.readLine();
			while(temp!=null)
			{
				temp=temp.trim();
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				To=temp;
				fr_list.add(new FriendRequest(UI.CurrentSession.CurrentUser.getID(),To));
				temp=reader.readLine();
			}
			reader.close();
			//Checking Response file
			reader=new BufferedReader(new FileReader(FileIO.FRIEND_RESPONSE_FILE));
			temp=reader.readLine();
			
			while(temp!=null)
			{
				temp=temp.trim();
				if(temp.isBlank())
				{
					temp=reader.readLine();
					continue;
				}
				index=temp.indexOf(",");
				if(index==-1)
				{
					temp=reader.readLine();
					continue;
				}
				From=temp.substring(0,index);
				temp=temp.substring(index+1);
					
				index=temp.indexOf(",");
				To=temp.substring(0,index);
				temp=temp.substring(index+1);
					
				index=temp.indexOf(",");
				try
				{
					response=RequestResponse.valueOf(temp.substring(0,index));
				}
				catch (IllegalArgumentException e) 
				{
					e.printStackTrace();
					temp = reader.readLine();
					continue;
				}
				temp=temp.substring(index+1);
				
				sent_time=ZonedDateTime.parse(temp);
					
				if(From.equals(UI.CurrentSession.CurrentUser.getID())&&sent_time.isAfter(UI.CurrentSession.LastSessionTime))
				{
					if(!response.equals(RequestResponse.DELETED))
					{
						fr_list.remove(new FriendRequest(From,To));
						if(response.equals(RequestResponse.ACCEPTED))
						{
							if(UI.CurrentSession.CurrentUser.getFriendByID(To).length==0)
								UI.CurrentSession.CurrentUser.addFriend(FileIO.SearchUserID(To));
						}
					}
					else
					{
						UI.CurrentSession.CurrentUser.removeFriend(FileIO.SearchUserID(To));
					}
				}
				temp=reader.readLine();
			}
			reader.close();
			writer=new BufferedWriter(new FileWriter(UI.CurrentSession.User_sent_request_file,false));
			for(int i=0;i<fr_list.size();i++)
			{
				if(fr_list.get(i)==null)continue;
				writer.write(fr_list.get(i).getTo());
				writer.newLine();
			}
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	//This function sends a response for deleting the current user as friend to the friend. It does not take any request
	static boolean SendRemoveFriendResponse(User remove_friend)
	{
		if(remove_friend==null)return false;
		try(BufferedWriter writer=new BufferedWriter(new FileWriter(FileIO.FRIEND_RESPONSE_FILE,true)))
		{
			writer.write(remove_friend.getID()+","+UI.CurrentSession.CurrentUser.getID()+","+RequestResponse.DELETED.toString()+","+ZonedDateTime.now().toString());
			writer.newLine();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
