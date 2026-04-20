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
/**
 * Provides all file-system I/O operations for the event organiser application.
 * <p>
 * This class is never instantiated; every method is static. It is responsible for:
 * <ul>
 *   <li>Creating and deleting files and directories.</li>
 *   <li>Persisting and reading users, events, invitations, friend requests, RSVP
 *       responses, and notifications.</li>
 *   <li>Generating unique IDs for users, events, and invitations.</li>
 *   <li>Synchronising session-scoped state (friends, notifications, upcoming events,
 *       etc.) with the flat-file database at login and logout.</li>
 * </ul>
 * All data is stored as comma-separated text files under a {@code root/} directory
 * and per-user folders named {@code user_<ID>/}.
 * </p>
 */
class FileIO {
	/** Path to the global file that stores all registered user records. */
	private static final String USER_FILE = "root/Users.txt";
 
	/** Path to the global file that records every invitation ever sent. */
	private static final String INVITATION_FILE = "root/Invitaton.txt";
 
	/** Path to the global file that records all outgoing friend requests. */
	private static final String FRIEND_REQUEST_FILE = "root/FriendRequest.txt";
 
	/** Path to the global file that records all friend-request responses (accept/reject/delete). */
	private static final String FRIEND_RESPONSE_FILE="root/FriendResponse.txt";
 
	/** Path to the global file that records all RSVP responses to invitations. */
	private static final String RSVP_FILE = "root/RSVP.txt";
 
	/** Path to the global file that records every event that has ever been created. */
	private static final String CREATED_EVENT_FILE="root/CreatedEvents.txt";
 
	/** Path to the global file that records the latest version of every edited event. */
	private static final String EDITED_EVENT_FILE="root/EditedEvents.txt";
 
	/** Path to the global file that records the IDs of all deleted events. */
	private static final String DELETED_EVENT_FILE="root/DeletedEvents.txt";
	/**
	 * Creates a directory (and any necessary parent directories) at the given path.
	 * <p>
	 * If the directory already exists the method returns {@code true} immediately
	 * without performing any filesystem operation.
	 * </p>
	 *
	 * @param path the relative or absolute path of the directory to create
	 * @return {@code true} if the directory exists or was created successfully;
	 *         {@code false} if {@link File#mkdirs()} failed
	 */
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
	/**
	 * Creates an empty file at the given path.
	 * <p>
	 * If the file already exists the method returns {@code true} immediately without
	 * modifying the file.
	 * </p>
	 *
	 * @param path_filename the relative or absolute path of the file to create
	 * @return {@code true} if the file exists or was created successfully;
	 *         {@code false} if an exception was thrown during creation
	 */
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
	/**
	 * Recursively deletes a folder and all of its contents.
	 * <p>
	 * If the folder does not exist the method returns {@code true} immediately.
	 * Deletion stops and returns {@code false} as soon as any individual file or
	 * sub-directory cannot be removed.
	 * </p>
	 *
	 * @param foldername the path to the folder to delete
	 * @return {@code true} if the folder and all its contents were deleted (or the
	 *         folder did not exist); {@code false} if any deletion failed
	 */
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
	/**
	 * Ensures that all root-level application files and directories exist, creating
	 * them if necessary.
	 * <p>
	 * This should be called once at application startup before any other
	 * {@link FileIO} method is used.
	 * </p>
	 *
	 * @return {@code true} if all required files and the {@code root/} directory exist
	 *         or were created successfully; {@code false} if any creation failed
	 */
	static boolean InitializeFiles()
	{
		if(CreateFolder("root")&& CreateFile(USER_FILE)&& CreateFile(INVITATION_FILE )&&
				CreateFile(CREATED_EVENT_FILE)&&CreateFile(EDITED_EVENT_FILE)&&
				CreateFile(DELETED_EVENT_FILE)&&CreateFile(RSVP_FILE)&&CreateFile(FRIEND_REQUEST_FILE)&&
				CreateFile(FRIEND_RESPONSE_FILE))
			return true;
		else return false;
	}
	/**
	 * Appends a new friend request to the global friend-request file and records the
	 * recipient's ID in the current user's sent-requests file.
	 *
	 * @param Fr the {@link FriendRequest} to persist; must have valid {@code from} and
	 *           {@code to} IDs
	 * @return {@code true} if both writes succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Records the current user's response (accept or reject) to a friend request and
	 * removes the corresponding entry from the current user's received-requests file.
	 *
	 * @param Fr the {@link FriendRequest} carrying the sender's ID, the current user's
	 *           ID, and the chosen {@link RequestResponse}
	 * @return {@code true} if the response was written and the received-requests file
	 *         was updated; {@code false} on any {@link IOException}
	 */
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
			from=reader.readLine();
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
	/**
	 * Persists a new invitation to the global invitation file and to the event's local
	 * {@code invited.txt} file inside the current session's event folder.
	 *
	 * @param iv the {@link Invitation} to store; must have a valid ID, sender, and
	 *           recipient
	 * @return {@code true} if both writes succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Records the current user's RSVP response to an invitation in the global RSVP
	 * file, optionally adds the event to the user's upcoming-events file (for ACCEPTED
	 * or MAYBE responses), and removes the invitation from the user's pending
	 * invitations file.
	 *
	 * @param iv the {@link Invitation} carrying the chosen {@link RSVP} status
	 * @return {@code true} if all file operations succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Appends a new user record to the global users file.
	 *
	 * @param us the {@link User} to persist; the record is written as
	 *           {@code ID,username,email,password}
	 * @return {@code true} if the write succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Persists a newly created event to the global created-events file, the current
	 * user's events file, and creates a dedicated event sub-folder containing empty
	 * {@code invited.txt} and {@code RSVP.txt} files.
	 *
	 * @param ev the {@link Event} to persist
	 * @return {@code true} if all file operations succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Persists an edited event to the global edited-events file and rewrites the
	 * current user's events file so that the matching record reflects the new event
	 * data.
	 *
	 * @param ev the updated {@link Event}; must not be {@code null}
	 * @return {@code true} if all file operations succeeded; {@code false} if
	 *         {@code ev} is {@code null} or any {@link IOException} occurs
	 */
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
	/**
	 * Records an event deletion in the global deleted-events file, removes the event
	 * from the current user's events file, and deletes the event's local folder and
	 * all its contents.
	 *
	 * @param ev the {@link Event} to delete; must not be {@code null}
	 * @return {@code true} if all file operations succeeded; {@code false} if
	 *         {@code ev} is {@code null} or any {@link IOException} occurs
	 */
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
				UI_utility.PrintError("Fatal error occured while deleting Event Files.");
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
	/**
	 * Generates a unique 10-character alphanumeric user ID by reading the last record
	 * in the global users file and incrementing it.
	 * <p>
	 * The ID space uses digits {@code '0'–'9'} followed by uppercase letters
	 * {@code 'A'–'Z'}, incrementing from the rightmost character. If the file is
	 * empty, {@code "0000000000"} is returned as the first ID.
	 * </p>
	 *
	 * @return a new unique 10-character ID string, or {@code null} if an
	 *         {@link IOException} occurs
	 */
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
	/**
	 * Generates a unique 10-character alphanumeric event ID by reading the last record
	 * in the global created-events file and incrementing it.
	 * <p>
	 * Uses the same base-36 increment algorithm as {@link #Create_UserID()}.  If the
	 * file is empty, {@code "0000000000"} is returned as the first ID.
	 * </p>
	 *
	 * @return a new unique 10-character ID string, or {@code null} if an
	 *         {@link IOException} occurs
	 */
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
	/**
	 * Generates a unique invitation ID for the given event by reading the last entry
	 * in the event's {@code invited.txt} file and incrementing the sequence portion.
	 * <p>
	 * The returned ID has the format {@code <eventID>.<sequence>} where
	 * {@code <sequence>} is a 10-character alphanumeric value using the same
	 * base-36 increment algorithm as {@link #Create_UserID()}. If no invitations
	 * exist yet, the sequence starts at {@code "0000000000"}.
	 * </p>
	 *
	 * @param ev the {@link Event} for which an invitation ID is needed
	 * @return a new unique invitation ID string, or {@code null} if an
	 *         {@link IOException} occurs
	 */
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
	/**
	 * Reads and returns all events owned by the current user from their events file.
	 *
	 * @return an array of {@link Event} objects; an empty array if no events exist or
	 *         an {@link IOException} occurs
	 */
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
	/**
	 * Looks up a single event owned by the current user by its ID.
	 *
	 * @param ID the 10-character event ID to search for
	 * @return a one-element {@link Event} array if found; an empty array if the event
	 *         does not exist in the user's events file or an {@link IOException} occurs
	 */
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
	/**
	 * Retrieves the authoritative (root) version of an event from the global event
	 * files using the following priority:
	 * <ol>
	 *   <li>If the ID appears in the deleted-events file, returns an empty array
	 *       (event has been removed).</li>
	 *   <li>If the ID appears in the edited-events file, returns the most recently
	 *       edited version.</li>
	 *   <li>Otherwise, returns the original record from the created-events file.</li>
	 * </ol>
	 *
	 * @param ID the 10-character event ID to look up
	 * @return a one-element {@link Event} array with the current event data, or an
	 *         empty array if the event has been deleted, does not exist, or an
	 *         {@link IOException} occurs
	 */
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
	/**
	 * Reads and returns all pending invitations received by the current user from
	 * their invitations file.
	 *
	 * @return an array of {@link Invitation} objects; an empty array if there are no
	 *         pending invitations or an {@link IOException} occurs
	 */
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
	/**
	 * Reads and returns all upcoming events that the current user has accepted or
	 * indicated interest in attending from their upcoming-events file.
	 *
	 * @return an array of {@link Event} objects representing upcoming events; an empty
	 *         array if there are none or an {@link IOException} occurs
	 */
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
	/**
	 * Retrieves all invitations for a given event along with their current RSVP
	 * statuses.
	 * <p>
	 * The method reads the event's {@code invited.txt} file to build a map of
	 * invitations keyed by their full ID, then reads the event's {@code RSVP.txt}
	 * file to overlay any recorded responses onto the corresponding invitation objects.
	 * </p>
	 *
	 * @param ev the {@link Event} whose invitation responses are to be retrieved
	 * @return an array of {@link Invitation} objects (with {@link RSVP} status set);
	 *         an empty array if an {@link IOException} occurs
	 */
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
					temp=RSVP_reader.readLine();
				}
			}
			return map.values().toArray(new Invitation[0]);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			UI_utility.PrintError("File Error occured while reading files");
			return new Invitation[0];
		}
	}
	/**
	 * Reads and returns all pending friend requests received by the current user from
	 * their received-requests file.
	 *
	 * @return an array of {@link FriendRequest} objects; an empty array if there are
	 *         none or an {@link IOException} occurs
	 */
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
	/**
	 * Reads and returns all friend requests that the current user has sent and that
	 * have not yet received a response, from their sent-requests file.
	 *
	 * @return an array of {@link FriendRequest} objects; an empty array if there are
	 *         none or an {@link IOException} occurs
	 */
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
	/**
	 * Searches the global users file for a user with the given ID and returns a
	 * {@link User} object populated with their stored data.
	 *
	 * @param ID the 10-character user ID to search for
	 * @return the matching {@link User}, or {@code null} if no user with that ID
	 *         exists or an {@link IOException} occurs
	 */
	static User SearchUserID(String ID)
	{
		if(UI.CurrentSession.UserCache.containsKey(ID)) return UI.CurrentSession.UserCache.get(ID);
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
					User found=new User(ID,name,emailaddress,password);
					UI.CurrentSession.UserCache.put(ID, found);
					return found;
				}
				test=reader.readLine();
			}
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			UI_utility.PrintError("File System Error occured");
			return null;
		}
		
	}
	/**
	 * Searches the global users file for a user with the given username and returns a
	 * {@link User} object populated with their stored data.
	 *
	 * @param username the exact username to search for (case-sensitive)
	 * @return the matching {@link User}, or {@code null} if no user with that username
	 *         exists or an {@link IOException} occurs
	 */
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
			UI_utility.PrintError("File System Error occured");
			return null;
		}
	}
	/**
	 * Authenticates a user by verifying that the supplied username exists and that its
	 * stored password matches the provided password.
	 *
	 * @param username the username to authenticate
	 * @param password the plaintext password to verify
	 * @return the authenticated {@link User} if credentials are valid; {@code null} if
	 *         the username does not exist or the password does not match
	 */
	static User SearchUserforAuthentication(String username,String password)
	{
		User us=FileIO.SearchUsername(username);
		if(us==null)
			return null;
		if(us.getPassword().equals(password)) return us;
		else return null;
	}
	/**
	 * Reads the first line of the current user's info file and stores it in
	 * {@link UI.CurrentSession#UserInfo}, which holds the timestamp of the last
	 * session start.
	 *
	 * @return {@code true} if the file was read successfully; {@code false} on any
	 *         {@link IOException} (in which case {@code UserInfo} is set to
	 *         {@code null})
	 */
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
	/**
	 * Loads the current user's friend list from their friend-list file into the
	 * in-memory {@link User} object.
	 * <p>
	 * If a friend's username in the file differs from their current username in the
	 * global users file, a {@link Notification} is generated to inform the user of the
	 * change, and the in-memory username is updated.
	 * </p>
	 *
	 * @return {@code true} if the friend list was loaded successfully; {@code false}
	 *         on any {@link IOException}
	 */
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
	/**
	 * Loads the current user's saved notifications from their notifications file into
	 * {@link UI.CurrentSession#Notifications}.
	 * <p>
	 * Each line is parsed as {@code message,seen,timestamp} and converted to a
	 * {@link Notification} object. Blank or malformed lines are silently skipped.
	 * </p>
	 *
	 * @return {@code true} if the notifications were loaded successfully; {@code false}
	 *         on any {@link IOException}
	 */
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
	/**
	 * Writes the current session's start time to the user's info file, overwriting any
	 * previous value. This timestamp becomes the "last session time" on the next login.
	 *
	 * @return {@code true} if the write succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Overwrites the current user's friend-list file with the in-memory friend list,
	 * writing each friend as {@code ID,username}.
	 *
	 * @return {@code true} if the write succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Overwrites the current user's notifications file with the in-memory list of
	 * {@link Notification} objects, writing each as {@code message,seen,timestamp}.
	 *
	 * @return {@code true} if the write succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Updates the global users file so that the current user's record reflects any
	 * in-session profile changes (username, password, or email address).
	 * <p>
	 * All other user records are preserved unchanged. The file is rewritten in full.
	 * </p>
	 *
	 * @return {@code true} if the file was rewritten successfully; {@code false} on
	 *         any {@link IOException}
	 */
	static boolean ChangeUserProfile()
	{
		
		ArrayList<User> Users =new ArrayList<>();
		try(BufferedReader reader=new BufferedReader(new FileReader(FileIO.USER_FILE)))
		{
			UI.CurrentSession.UserCache.remove(UI.CurrentSession.CurrentUser.getID());
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
	/**
	 * Synchronises the current user's pending invitations file at login.
	 * <p>
	 * First, any previously stored invitations that refer to deleted, past, or
	 * otherwise invalid events are pruned. Then, new invitations sent to the current
	 * user since their last session are appended, and a corresponding
	 * {@link Invite_Notification} is generated for each one.
	 * </p>
	 *
	 * @return {@code true} if the file was updated successfully; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Synchronises the current user's upcoming-events file at login.
	 * <p>
	 * For each stored upcoming event:
	 * <ul>
	 *   <li>If the event has been deleted, an {@link EventCancel_Notification} is
	 *       generated and the event is dropped.</li>
	 *   <li>If the event has been edited, an {@link EventChange_Notification} is
	 *       generated and the local record is updated.</li>
	 *   <li>If the event's date/time has already passed, it is silently dropped.</li>
	 * </ul>
	 * The file is rewritten with only the valid, still-upcoming events.
	 * </p>
	 *
	 * @return {@code true} if the file was updated successfully; {@code false} on any
	 *         {@link IOException}
	 */
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
	/**
	 * Scans the global RSVP file at login for new responses to invitations for events
	 * owned by the current user.
	 * <p>
	 * For each RSVP entry that was recorded after the last session and belongs to one
	 * of the current user's events, the response is written to the event's local
	 * {@code RSVP.txt} file and a {@link Response_Notification} is generated.
	 * </p>
	 *
	 * @return {@code true} if the file was processed successfully; {@code false} on
	 *         any {@link IOException}
	 */
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
	/**
	 * Synchronises received and sent friend-request state at login.
	 * <p>
	 * <b>Received requests:</b> Any friend request sent to the current user since
	 * their last session is appended to their received-requests file.
	 * <br>
	 * <b>Sent requests:</b> The global friend-response file is scanned for responses
	 * to requests previously sent by the current user. Responded requests are removed
	 * from the sent-requests file; if a request was accepted the responder is added as
	 * a friend, and if the response is {@link RequestResponse#DELETED} the former
	 * friend is removed.
	 * </p>
	 *
	 * @return {@code true} if all file operations succeeded; {@code false} on any
	 *         {@link IOException}
	 */
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
						UI.CurrentSession.CurrentUser.removeFriend(To);
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
	/**
	 * Writes a {@link RequestResponse#DELETED} entry to the global friend-response
	 * file on behalf of the current user, signalling to the other party that they have
	 * been removed as a friend.
	 * <p>
	 * Note: this method does not require a prior friend request — it is called
	 * unilaterally when the current user removes a friend.
	 * </p>
	 *
	 * @param remove_friend the {@link User} who is being removed as a friend; must not
	 *                      be {@code null}
	 * @return {@code true} if the response was written successfully; {@code false} if
	 *         {@code remove_friend} is {@code null} or an {@link IOException} occurs
	 */
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
