package event_org;

import java.util.Scanner;
import java.io.File;
import java.lang.NumberFormatException;
import java.io.Console;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Utility interface providing ANSI color constants and colored console output helpers.
 * <p>
 * All methods print the given message wrapped in the appropriate ANSI escape code so
 * that the terminal renders it in the corresponding color, then resets back to the
 * default color.
 * </p>
 */
interface UI_utility
{
	/** ANSI escape code that resets the terminal color to its default. */
	public static final String ANSI_RESET = "\u001B[0m";
 
	/** ANSI escape code for red terminal text (used for errors). */
	public static final String ANSI_RED = "\u001B[31m";
 
	/** ANSI escape code for green terminal text (used for success messages). */
	public static final String ANSI_GREEN = "\u001B[32m";
 
	/** ANSI escape code for yellow terminal text (used for warnings). */
	public static final String ANSI_YELLOW="\u001B[33m";
 
	/** ANSI escape code for orange terminal text (used for access-denied messages). */
	public static final String ANSI_ORANGE="\033[38;5;208m";
 
	/**
	 * Prints the given message to standard output in red, indicating an error.
	 *
	 * @param s the error message to display
	 */
	static void PrintError(String s)
	{
		System.out.println(ANSI_RED + s + ANSI_RESET);
	}
 
	/**
	 * Prints the given message to standard output in green, indicating success.
	 *
	 * @param s the success message to display
	 */
	static void PrintSuccess(String s)
	{
		System.out.println(ANSI_GREEN + s + ANSI_RESET);
	}
 
	/**
	 * Prints the given message to standard output in yellow, indicating a warning.
	 *
	 * @param s the warning message to display
	 */
	static void PrintWarning(String s)
	{
		System.out.println(ANSI_YELLOW + s + ANSI_RESET);
	}
 
	/**
	 * Prints the given message to standard output in orange, typically used to indicate
	 * that the current user does not have access to perform the requested action.
	 *
	 * @param s the access-denied message to display
	 */
	static void PrintNoAccess(String s)
	{
		System.out.println(ANSI_ORANGE + s + ANSI_RESET);
	}
}

/**
 * Abstract base class that provides all console-based UI operations for the event
 * organiser application.
 * <p>
 * This class is never instantiated directly; it serves as a namespace for static
 * helper methods (input reading, prompting, table rendering) and for the top-level
 * menu flows (login, signup, main menu, event/friend/invitation management).  It also
 * houses the inner {@link CurrentSession} class which stores per-login state.
 * </p>
 */
abstract class UI {
	/** Shared {@link Scanner} used for all console input throughout the application. */
	private static Scanner SC_console=new Scanner(System.in);
	private static Console PW_console = System.console();
	
	/**
	 * Displays a prompt and reads a non-empty trimmed string from the console.
	 * <p>
	 * The method loops indefinitely until the user supplies at least one non-whitespace
	 * character.
	 * </p>
	 *
	 * @param s the prompt label printed before the colon ({@code ": "})
	 * @return the trimmed, non-empty string entered by the user
	 */
	static String input(String s)
	{
		while(true) 
		{
			System.out.print(s+": ");
		
        	String inputs = SC_console.nextLine().trim();
        	//Checking if string is empty
        	if(inputs.isEmpty())
        	{
        		System.out.println("Enter someting");
        		continue;
        	}
        	return inputs;
		}
	}
	/**
	 * Presents a yes/no confirmation prompt and returns the user's answer as a boolean.
	 * <p>
	 * The prompt suffix {@code " (N/Y): "} is appended automatically.  The method
	 * accepts {@code 'y'} or {@code 'n'} (case-insensitive) as the first character of
	 * the input and loops on any other input.
	 * </p>
	 *
	 * @param s the question text shown before the {@code "(N/Y)"} suffix
	 * @return {@code true} if the user entered {@code 'y'}; {@code false} for {@code 'n'}
	 */
	static boolean prompt(String s)
	{
		while(true) 
		{
			System.out.print(s+" (N/Y): ");
		
        	String inputs = SC_console.nextLine().trim().toLowerCase();
        	//Checking if string is empty
        	if(inputs.isEmpty())
        	{
        		System.out.println("Enter someting");
        		continue;
        	}
        	char inputc = inputs.charAt(0);
        	//Checking input
        	if(inputc=='n') return false;
        	else if (inputc=='y') return true;
        	else
        	{
        		System.out.println("INVALID INPUT");
        	}
		}
		
	}
	/**
	 * Displays a numbered menu built from the supplied option labels and returns the
	 * index chosen by the user.
	 * <p>
	 * Each option is printed on its own line in the format {@code "<label> (<index>)"}.
	 * The method loops until the user enters an integer in the range
	 * {@code [0, args.length-1]}.
	 * </p>
	 *
	 * @param args the menu option labels in display order; index {@code 0} is
	 *             conventionally used as the "back / quit" option
	 * @return the zero-based index of the option selected by the user
	 */
	static int prompt(String...args) 
	{
		int index=0;
		System.out.println();
		for( int i=0;i<args.length;i++)
		{
			System.out.printf("%s (%d)\n",args[i],i);
		}
		while(true)
		{
			System.out.print("Enter choice: ");
			String inputs = SC_console.nextLine().trim();
        	//Checking if string is empty
			
        	if(inputs.isEmpty()||inputs==null)
        	{
        		System.out.println("Enter someting.");
        		continue;
        	}
        	try {
        		index=Integer.parseInt(inputs);
        		if (index >= 0 && index <= args.length-1) 
        		{
        			System.out.println("Entered :"+index);
                    return index;
                } 
        		else {
                    System.out.printf("Please enter a number between 0 and %d.\n", args.length-1);
                    continue;
                }
        	}
        	catch(NumberFormatException e){
        		System.out.println("INVALID INPUT");
        	}
        	
		}
		
	}
	/**
	 * Renders a plain-text ASCII table to standard output.
	 * <p>
	 * Column widths are calculated automatically to fit the widest value in each column
	 * (header or data).  The method returns immediately without printing anything if the
	 * arguments are invalid (null, empty, or inconsistent column counts).
	 * </p>
	 *
	 * @param column_fields the header labels for each column; must not be {@code null}
	 * @param data          a 2-D array of cell values where {@code data[row][col]}
	 *                      corresponds to {@code column_fields[col]}; individual cells
	 *                      may be {@code null} (rendered as blank)
	 */
	static void table(String[] column_fields,String[][] data)
	{
		if (column_fields == null || data == null || data.length == 0) return;
		if (column_fields.length!=data[0].length) return ;
		for(int i=1;i<data.length;i++)
		{
			if (data[i-1].length!=data[i].length) return ;
		}
		
		int column_length[]=new int[column_fields.length];
		
		for(int i=0;i<column_fields.length;i++)
		{
			int max = (column_fields[i] == null) ? 0 : column_fields[i].length();
			for(int j=0;j<data.length;j++)
			{
				if(data[j][i] != null && data[j][i].length() > max) max=data[j][i].length();
			}
			column_length[i]=max+2;
		}
		//Printing the table boundary
		for(int i=0;i<column_fields.length;i++)
		{
			System.out.print("+"+"-".repeat(column_length[i]));
		}
		System.out.println("+");
		// Printing Field Name
		for(int i=0;i<column_fields.length;i++)
		{
			System.out.print("|");
			if(column_fields[i]==null) System.out.print(" ".repeat(column_length[i]));
			else System.out.print(" "+column_fields[i]+" ".repeat(column_length[i]-1-column_fields[i].length()));
		}
		System.out.println("|");
		//printing label-data boundary
		for(int i=0;i<column_fields.length;i++)
		{
			System.out.print("+"+"-".repeat(column_length[i]));
		}
		System.out.println("+");
		//Printing data
		for(int i=0;i<data.length;i++)
		{
			if(data[i]==null) continue;
			for(int j=0;j<column_fields.length;j++)
			{
				System.out.print("|");
				if(data[i][j]==null) System.out.print(" ".repeat(column_length[j]));
				else System.out.print(" "+data[i][j]+" ".repeat(column_length[j]-1-data[i][j].length()));
			}
			System.out.println("|");
		}
		//printing end table boundary
		for(int i=0;i<column_fields.length;i++)
		{
			System.out.print("+"+"-".repeat(column_length[i]));
		}
		System.out.println("+");
	}
	/**
	 * Reads a password string from the console.
	 * <p>
	 * When a system {@link Console} is available the password would be read without
	 * echo ; otherwise the input is read via the shared
	 * {@link Scanner} and is therefore visible on screen.  Leading and trailing
	 * whitespace is trimmed.
	 * </p>
	 *
	 * @param s the prompt text displayed before reading input
	 * @return the trimmed password string entered by the user
	 */
	private static String TakePassword(String s)
	{
		if(PW_console != null) 
		{  
			char[] c1 = PW_console.readPassword(s); 
			return new String(c1).trim(); 
		} 
		else 
		{  
			System.out.print(s); 
			return SC_console.nextLine().trim();
		}
//		System.out.print(s); 
//		return SC_console.nextLine().trim();
	}

	/**
	 * Guides the user through the new-account registration flow.
	 * <p>
	 * Collects and validates a username (max 15 chars, unique, no commas), an email
	 * address, and a confirmed password (min 4 chars).  On success it persists the new
	 * {@link User} record via {@link FileIO} and creates all required user-specific
	 * directories and files.
	 * </p>
	 *
	 * @return {@code true} if the account was created successfully;
	 *         {@code false} if the user chose to go back (entered {@code "0"}) or a
	 *         fatal I/O error occurred
	 */
	static boolean Signup()
	{
		System.out.println("\nYou are in Signup menu. Press 0 to go back");
		String username,emailaddress,password,ID,con_password;
		while(true)
		{
			System.out.print("Enter your Username(Max 15 characters): ");
			username = SC_console.nextLine().trim();
			if(username.equals("0")) return false;
			else if(username.length()>15) UI_utility.PrintWarning("Max character limit exceeded");
			else if(username.isEmpty()) UI_utility.PrintWarning("Enter Something");
			else if(username.contains(",")) UI_utility.PrintWarning("Entering , corrupts file system. Try again");
			else if(FileIO.SearchUsername(username)!=null) UI_utility.PrintNoAccess("Username already taken.");
			else break;
		}
		while(true)
		{
			System.out.print("Enter your Email Address:");
			emailaddress=SC_console.nextLine().trim();
			if(emailaddress.equals("0")) return false;
			else if(!emailaddress.contains("@")|| !emailaddress.contains("."))
			{
				UI_utility.PrintWarning("Enter a Valid Email Address");
				continue;
			}
			else if(emailaddress.contains(","))
			{
				UI_utility.PrintWarning("Entering , corrupts file system. Try again");
				continue;
			}
			else break;
		}
		while(true)
		{
			password=TakePassword("Enter Password (Trailing and leading spaces will be trimmed): ");
			if(password.equals("0")) return false;
			else if(password.length()<4)
			{
				UI_utility.PrintWarning("Too Short Password. Enter atleast 4 characters.");
				continue;
			}
			con_password=TakePassword("Confirm Password: ");
			// entering , is safe as it is last entry in the line
			if(!password.equals(con_password))
			{
				UI_utility.PrintWarning("Password does not match");
				continue;
			}
			else break;
		}
		ID=FileIO.Create_UserID();
		if(ID==null) {
			UI_utility.PrintError("Error occured while creating ID");
			return false;
		}
		
		if(!FileIO.EnterNewUser(new User(ID,username,emailaddress,password)))
		{
			UI_utility.PrintError("Fatal Error occured while creating new account.");
			return false;
		}
		else UI_utility.PrintSuccess("New account registered.");
		String userfolder="user_"+ID;
		String eventfolder=userfolder+"/events";
		if(!FileIO.CreateFolder(userfolder) || !FileIO.CreateFile(userfolder+"/friendlist.txt")||
			!FileIO.CreateFile(userfolder+"/notifications.txt") || !FileIO.CreateFile(userfolder+"/userinfo.txt")||
			!FileIO.CreateFolder(eventfolder)|| !FileIO.CreateFile(userfolder+"/invitations.txt")||
			!FileIO.CreateFile(userfolder+"/event_attend.txt")|| !FileIO.CreateFile(userfolder+"/user_events.txt") ||
			!FileIO.CreateFile(userfolder+"/received_requests.txt") || !FileIO.CreateFile(userfolder+"/sent_requests.txt"))
		{
			UI_utility.PrintError("Local Directories can't be made while creating new account.\nFiles will be created at login");
			return false;
		}
		UI_utility.PrintSuccess("Account created Successfully.\n");
		return true;
	}
	/**
	 * Guides the user through the login flow.
	 * <p>
	 * Prompts for a username and password, authenticates the credentials against the
	 * stored user database via {@link FileIO#SearchUserforAuthentication}, and on
	 * success initialises a new {@link CurrentSession}.
	 * </p>
	 *
	 * @return {@code true} if authentication and session initialisation both succeeded;
	 *         {@code false} if the user entered {@code "0"} to go back or the session
	 *         could not be initialised
	 */
	static boolean Login()
	{
		System.out.println("\nYou are in Login menu.");
		String username,password;
		while(true)
		{
			System.out.println("Enter 0 to go back");
			while(true)
			{
				System.out.print("Enter your Username(Max 15 characters): ");
				username = SC_console.nextLine().trim();
				if(username.equals("0")) return false;
				else if(username.length()>15) UI_utility.PrintWarning("Max character limit exceeded");
				else if(username.isEmpty()) UI_utility.PrintWarning("Enter Something");
				else if(username.contains(",")) UI_utility.PrintWarning("Character , can't be in username");
				else break;
			}
			while(true)
			{
				password=TakePassword("Enter Password (Trailing and leading spaces will be trimmed): ");
				if(password.equals("0")) return false;
				if(password.length()<4)
				{
					UI_utility.PrintWarning("Too Short Password. Enter atleast 4 characters.");
					continue;
				}
				else break;
			}
			User TestUser=FileIO.SearchUserforAuthentication(username, password);
			if(TestUser!=null)
			{
				UI_utility.PrintSuccess("\nUser account found.");
				if(UI.CurrentSession.initialize(TestUser))
				{
					UI_utility.PrintSuccess("\nYou have successfully logged in.");
					return true;
				}
				else return false;
			}
			else
			{
				UI_utility.PrintNoAccess("\nInvalid User Name or Password. Try Again.");
				continue;
			}
		}
	}
	/**
	 * Ends the current user session by delegating to {@link CurrentSession#endSession()}
	 * and prints a confirmation message on success.
	 */
	static void Logout()
	{
		if(UI.CurrentSession.endSession()) UI_utility.PrintSuccess("You Have Logged out Successfully\n");
	}
	/**
	 * Displays the main application menu and dispatches to the appropriate sub-menu
	 * based on the user's choice.
	 * <p>
	 * The loop runs until the user selects the logout option (index {@code 0}), at
	 * which point {@link #Logout()} is called and the method returns.
	 * </p>
	 */
	static void Mainmenu()
	{
		int choice;
		while(true)
		{
			choice=prompt("Logout","Manage Events","Manage Invitations","Friends","View Upcoming Events","View Notifications","Update Profile");
		
			if(choice==0) {
				UI.Logout();
				return;
			}
			else if(choice==1) UI.Manage_Events();
			else if(choice==2) UI.Manage_Invitations();
			else if(choice==3) UI.Manage_Friends();
			else if(choice==4) UI.ViewUpcomingEvents();//yet to do
			else if(choice==5) UI.ViewNotification();
			else if(choice==6) UI.UpdateProfile();
		}
	}
	/**
	 * Allows the currently logged-in user to update their profile information.
	 * <p>
	 * The user may change their username, password, or email address.  All changes are
	 * batched in memory and only persisted to disk when the user selects "Save and
	 * Quit" (index {@code 0}).
	 * </p>
	 */
	private static void UpdateProfile()
	{
		int choice;
		boolean profile_changed=false;
		while(true)
		{
			choice=prompt("Save and Quit","Change Username","Change password","Change Email Address");
			if(choice==0)
			{
				if(profile_changed)
				{
					if(FileIO.ChangeUserProfile())
					{
						UI_utility.PrintSuccess("Profile Changed Successfully");
					}
					else
					{
						UI_utility.PrintError("Error occured while changing profile");
					}
				}
				return;
			}
			else if(choice==1)
			{
				while(true)
				{
					String username;
					System.out.print("Enter new Username(Max 15 characters): ");
					username= SC_console.nextLine().trim();
					if(username.equals("0")) break;
					else if(username.length()>15) UI_utility.PrintWarning("Max character limit exceeded");
					else if(username.isEmpty()) UI_utility.PrintWarning("Enter Something");
					else if(username.contains(",")) UI_utility.PrintWarning("Entering , corrupts file system. Try again");
					else if(FileIO.SearchUsername(username)!=null) UI_utility.PrintNoAccess("Username already taken.");
					else 
					{
						CurrentSession.CurrentUser.setUsername(username);
						profile_changed=true;
						break;
					}
				}
			}
			else if(choice==2)
			{
				String password,con_password;
				while(true)
				{
					password=TakePassword("Enter new Password (Trailing and leading spaces will be trimmed): ");
					if(password.equals("0")) break;
					else if(password.length()<4)
					{
						UI_utility.PrintWarning("Too Short Password. Enter atleast 4 characters.");
						continue;
					}
					con_password=TakePassword("Confirm Password: ");
					// entering , is safe as it is last entry in the line
					if(!password.equals(con_password))
					{
						UI_utility.PrintWarning("Password does not match");
						continue;
					}
					else
					{
						CurrentSession.CurrentUser.setPassword(password);
						profile_changed=true;
						break;
					}
				}
			}
			else if(choice==3)
			{
				String emailaddress;
				while(true)
				{
					System.out.print("Enter new Email Address:");
					emailaddress=SC_console.nextLine().trim();
					if(emailaddress.equals("0")) break;
					else if(!emailaddress.contains("@")|| !emailaddress.contains("."))
					{
						UI_utility.PrintWarning("Enter a Valid Email Address");
						continue;
					}
					else if(emailaddress.contains(","))
					{
						UI_utility.PrintWarning("Entering , corrupts file system. Try again");
						continue;
					}
					else
					{
						CurrentSession.CurrentUser.setEmailaddress(emailaddress);
						profile_changed=true;
						break;
					}
				}
			}
		}
	}
	/**
	 * Retrieves and displays all upcoming events that the current user is attending in
	 * a formatted table.
	 * <p>
	 * If the user has no upcoming events a message is printed and the method returns
	 * immediately.
	 * </p>
	 */
	private static void ViewUpcomingEvents()
	{
		Event Upcoming[]=FileIO.GetUpcomingUserEvents();
		if(Upcoming==null|| Upcoming.length==0)
		{
			System.out.println("You have no upcoming events to attend");
			return;
		}
		
		String Fields[]={"Event ID","Name","Description","Location","Event Type","Event Date Time"};
		String data[][]=new String[Upcoming.length][6];

		int k = 0; 
		for(int i=0;i<Upcoming.length;i++)
		{
		    if(Upcoming[i]==null) continue;
		    data[k][0]=Upcoming[i].getID();
		    data[k][1]=Upcoming[i].getName();
		    data[k][2]=Upcoming[i].getDescription();
		    data[k][3]=Upcoming[i].getLocation();
		    data[k][4]=Upcoming[i].getEventType().toString();
		    data[k][5]=Upcoming[i].getEvent_DateTime().toString();
		    k++;
		}
		table(Fields,data);
		System.out.print("Total Upcoming Events: ");
		System.out.println(k);
	}
	/**
	 * Displays the notification management sub-menu for the current user.
	 * <p>
	 * Options allow the user to view only unseen notifications (marking them as seen
	 * in the process), view all notifications, or clear all notifications.
	 * </p>
	 */
	private static void ViewNotification()
	{
		int choice;
		while(true)
		{
			choice=prompt("Go back","View Unseen notification","View All Notification","Clear all notification");
			if(choice==0)
			{
				return;
			}
			else if(choice==1)
			{
				int k=0;
				for(int i=0;i<CurrentSession.Notifications.size();i++)
				{
					if(CurrentSession.Notifications.get(i).getView_Status()==false)
					{
						System.out.println(CurrentSession.Notifications.get(i).GetNotification());
						CurrentSession.Notifications.get(i).SetViewed_Status(true);
						k++;
					}
				}
				if(k==0) System.out.println("You have no Unseen notifications");
			}
			else if(choice ==2)
			{
				if(CurrentSession.Notifications.size()==0)
				{
					System.out.println("You have no notifications");
					continue;
				}
				for(int i=0;i<CurrentSession.Notifications.size();i++)
				{
					System.out.println(CurrentSession.Notifications.get(i).GetNotification());
					CurrentSession.Notifications.get(i).SetViewed_Status(true);
				}
			}
			else if(choice==3)
			{
				CurrentSession.Notifications.clear();
				UI_utility.PrintSuccess("Notifications cleared");
			}
		}
	}
	/**
	 * Displays the invitation management sub-menu, allowing the user to view received
	 * invitations, respond to them, send new invitations for their own events, or view
	 * the responses to invitations they have sent.
	 */
	private static void Manage_Invitations()
	{
		while(true)
		{
			int choice=prompt("Go back","Received invitaions","Respond to Invitation","Send Invitations for your Event","View Invitation response for your event");
			if(choice==0)
			{
				return;
			}
			else if(choice==1)
			{
				Received_Invitations();
			}
			else if(choice==2)
			{
				Respond_Invitations();
			}
			else if(choice==3)
			{
				Send_Invitations();
			}
			else if(choice==4)
			{
				View_Invitation_Response();
			}
		}
	}
	/**
	 * Retrieves all invitations received by the current user and displays them in a
	 * formatted table showing event details and the sender's username.
	 */
	private static void Received_Invitations()
	{
		Invitation iv[]=FileIO.GetReceivedInvitations();
		String Data[][]=new String[iv.length][7];
		String Fields[]= {"Event ID","From","Event name","Event description","Event Location","Type","Event Date and Time"};
		Event ev[];
		for(int i=0;i<iv.length;i++)
		{
			if(iv[i]==null) continue;
			ev=FileIO.GetRootEvent(iv[i].getEventID());
			if(ev.length==0) continue;
			Data[i][0]=iv[i].getEventID();
			Data[i][1]=iv[i].getFrom().getUsername();
			Data[i][2]=ev[0].getName();
			Data[i][3]=ev[0].getDescription();
			Data[i][4]=ev[0].getLocation();
			Data[i][5]=ev[0].getEventType().toString();
			Data[i][6]=ev[0].getEvent_DateTime().toString();
		}
		table(Fields,Data);
		System.out.println("Total Invitations: "+iv.length);
	}
	/**
	 * Allows the current user to respond to a received invitation by supplying the
	 * event ID and choosing ACCEPT, DECLINE, or MAYBE.
	 * <p>
	 * The chosen {@link RSVP} status is persisted via {@link FileIO#RespondToInvitation}.
	 * </p>
	 */
	private static void Respond_Invitations()
	{
		Invitation iv[]=FileIO.GetReceivedInvitations();
		String EventID,ans;
		RSVP response=RSVP.PENDING;
		Invitation invite_found=null;
		boolean found=false;
		while(true)
		{
			EventID=input("Enter the Event ID of Invitation(Enter 0 to go back)");
			if(EventID.equals("0")) return;
			for(int i=0;i<iv.length;i++)
			{
				if(iv[i].getEventID().equals(EventID))
				{
					UI_utility.PrintSuccess("Invitation Found");
					invite_found=iv[i];
					found=true;
					break;
				}
			}
			if(found==false)
			{
				UI_utility.PrintWarning("No Such Invitation");
				continue;
			}
			while(true)
			{
				ans=input("Enter a Valid Response (ACCEPT/DECLINE/MAYBE)");
				if(ans.equals("0")) break;
				if(ans.equalsIgnoreCase("ACCEPT"))
				{
					response=RSVP.ACCEPTED;
					break;
				}
				else if(ans.equalsIgnoreCase("DECLINE"))
				{
					response=RSVP.DECLINED;
					break;
				}
				else if(ans.equalsIgnoreCase("MAYBE"))
				{
					response=RSVP.MAYBE;
					break;
				}
				else
				{
					UI_utility.PrintWarning("INVALID INPUT");
					continue;
				}
			}
			if(ans.equals("0")) continue;
			invite_found.setStatus(response);
			
			if(!FileIO.RespondToInvitation(invite_found))
			{
				UI_utility.PrintError("Error occured while editing files");
				continue;
			}
			else
			{
				UI_utility.PrintSuccess("Your Response save successfully");
				continue;
			}
		}
	}
	/**
	 * Allows the current user to send event invitations to other users for an event
	 * they own.
	 * <p>
	 * The target user can be found by ID or username.  The method enforces the
	 * following rules:
	 * <ul>
	 *   <li>Cannot invite yourself.</li>
	 *   <li>Cannot invite a non-friend to a {@code PRIVATE} event.</li>
	 *   <li>Cannot send a duplicate invitation if one is already pending or responded
	 *       to.</li>
	 *   <li>Cannot send invitations for past events.</li>
	 * </ul>
	 * </p>
	 */
	private static void Send_Invitations()
	{
		Event ev[];
		while(true)
		{
			String Event_ID=input("Enter the Event ID that you want to send invitations(0 to go back)");
			if (Event_ID.equals("0")) return;
			ev=FileIO.GetUserEvent(Event_ID);
			if(ev.length==0)
			{
				UI_utility.PrintWarning("You have not created this event");
				continue;
			}
			else UI_utility.PrintSuccess("Event found");
			if(ev[0].getEvent_DateTime().isBefore(ZonedDateTime.now()))
			{
				UI_utility.PrintNoAccess("Can't send invitation to an Event that is already finished");
				continue;
			}
			int choice=0;
			User receiver=null;
			while(true)
			{
				choice=prompt("Go back","Enter User ID to send invitations","Enter Username to send invitations");
				if(choice==0)
				{
					break;
				}
				else if(choice==1)
				{
					String ID=input("Enter a valid User ID");
					if(ID.equals("0")) continue;
					receiver=FileIO.SearchUserID(ID);
				}
				else
				{
					String name=input("Enter a valid Username");
					if(name.equals("0")) continue;
					receiver=FileIO.SearchUsername(name);
				}
				if(receiver==null)
				{
					UI_utility.PrintWarning("No such User");
					continue;
				}
				if(receiver.getID().equals(CurrentSession.CurrentUser.getID()))
				{
					UI_utility.PrintWarning("Can't send Invitation to yourself");
					continue;
				}
				if(CurrentSession.CurrentUser.getFriendByID(receiver.getID()).length==0 && ev[0].getEventType().equals(Event_t.PRIVATE))
				{
					UI_utility.PrintWarning("Can't send invitaions for private events to non-friend users");
					continue;
				}
				UI_utility.PrintSuccess("User found");
				Invitation response_inv[]=FileIO.GetUserEventResponse(FileIO.GetRootEvent(Event_ID)[0]);
				
				boolean already_sent=false;
				for(int i=0;i<response_inv.length;i++)
				{
					if(response_inv[i]==null) continue;
					if(response_inv[i].getTo().getID().equals(receiver.getID()))
					{
						already_sent=true;
						if(response_inv[i].getStatus()!=RSVP.PENDING) 
						{
							UI_utility.PrintWarning("User Already Responded to Invitation");
							break;
						}
						else
						{
							UI_utility.PrintWarning("Invitation already sent and pending");
						}
						break;
					}
				}
				if(already_sent) continue;
				
				table(new String[]{"User ID","Username"},new String[][]{{receiver.getID(),receiver.getUsername()}});
				boolean ans =prompt("Are you sure you want to send invitation?");
				if(ans==true)
				{
					String inv_id=FileIO.Create_InviteID(ev[0]);
					System.out.println(inv_id);
					if(inv_id!=null)
					{
						if(FileIO.EnterNewInvitation(new Invitation(inv_id,CurrentSession.CurrentUser,receiver)))
						{
							UI_utility.PrintSuccess("Invitation Sent");
						}
						else
						{
							UI_utility.PrintError("Error occured while sending invitation.");
						}
					}
					else
					{
						UI_utility.PrintError("Error occured while Creating new ID");
					}
				}
				else continue;
			}
		}
	}
	/**
	 * Displays the RSVP responses received for a specific event owned by the current
	 * user, along with a summary count of accepted, declined, maybe, and pending
	 * responses.
	 */
	private static void View_Invitation_Response()
	{
		Event ev[];
		while(true)
		{
			String Event_ID=input("Enter the Event ID that you want to view invitation response(0 to go back)");
			if (Event_ID.equals("0")) return;
			ev=FileIO.GetUserEvent(Event_ID);
			if(ev.length==0)
			{
				UI_utility.PrintWarning("No Such Event exists");
				continue;
			}
			Invitation invitations[]=FileIO.GetUserEventResponse(ev[0]);
			String Data[][]=new String[invitations.length][3];
			String Fields[]= {"User ID","Username","Response"};
			int accepted=0,declined=0,maybe=0,pending=0;
			for(int i=0;i<invitations.length;i++)
			{
				if(invitations[i]==null) continue;
				Data[i][0]=invitations[i].getTo().getID();
				Data[i][1]=invitations[i].getTo().getUsername();
				if(invitations[i].getStatus().equals(RSVP.ACCEPTED)) accepted++;
				else if(invitations[i].getStatus().equals(RSVP.MAYBE))maybe++;
				else if(invitations[i].getStatus().equals(RSVP.DECLINED))declined++;
				else if(invitations[i].getStatus().equals(RSVP.PENDING))pending++;
				Data[i][2]=invitations[i].getStatus().toString();
			}
			table(Fields,Data);
			System.out.println("Accepted: "+accepted+"\tMaybe: "+maybe+"\tDeclined: "+declined+"\tPending: "+pending);
			System.out.println("Total Invitations Sent: "+(accepted+maybe+declined+pending));
		}
	}
	/**
	 * Displays the event management sub-menu, allowing the user to view, create, edit,
	 * or delete their own events.
	 */
	private static void Manage_Events()
	{
		while(true)
		{
			int choice=prompt("Quit","View Your Events","Create new Events","Edit your Events","Delete your Events");
			if(choice==0)
			{
				return;
			}
			else if(choice==1)
			{
				View_Your_Events();
				
			}
			else if(choice==2)
			{
				Create_Event();
			}
			else if(choice==3)
			{
				Edit_Event();
			}
			else if(choice==4)
			{
				Delete_Event();
			}
		}
		
	}
	/**
	 * Displays all events owned by the current user in a formatted table.
	 * <p>
	 * The user can filter the list to show all events, only upcoming events, only past
	 * events, or look up a specific event by its ID.
	 * </p>
	 */
	private static void View_Your_Events()
	{
		Event Ev[];
		String Fields[]= {"ID","Name","Description","Location","Type","Date and Time"};
		String data[][];
		while(true)
		{
			int choice_in = prompt("Go Back","View All Events","View Upcoming Events","View Past Events","View Event By ID");
			if(choice_in ==0) return;
			if(choice_in ==1)
			{
				Ev = FileIO.GetUserEvent();
				data= new String[Ev.length][6];
			}
			else if(choice_in==2)
			{
				Ev = FileIO.GetUserEvent();
				ArrayList<Event> upcoming=new ArrayList<>();
				for(int i=0;i<Ev.length;i++)
				{
					if(Ev[i]==null) continue;
					if(Ev[i].getEvent_DateTime().isAfter(ZonedDateTime.now()))
					{
						upcoming.add(Ev[i]);
					}
				}
				upcoming.toArray(Ev);
				data=new String[Ev.length][6];
			}
			else if(choice_in==3)
			{
				Ev = FileIO.GetUserEvent();
				ArrayList<Event> upcoming=new ArrayList<>();
				for(int i=0;i<Ev.length;i++)
				{
					if(Ev[i]==null) continue;
					if(Ev[i].getEvent_DateTime().isBefore(ZonedDateTime.now()))
					{
						upcoming.add(Ev[i]);
					}
				}
				upcoming.toArray(Ev);
				data=new String[Ev.length][6];
			}
			else if (choice_in==4)
			{
				Ev = FileIO.GetUserEvent(input("Enter Valid Event ID"));
				data= new String[Ev.length][6];
			}
			else continue;
			
			if(data.length==0) 
			{
				System.out.println("Total Events: 0");
				continue;
			}
			int k = 0; 
			for(int i=0;i<Ev.length;i++)
			{
			    if(Ev[i]==null) continue;
			    data[k][0]=Ev[i].getID();
			    data[k][1]=Ev[i].getName();
			    data[k][2]=Ev[i].getDescription();
			    data[k][3]=Ev[i].getLocation();
			    data[k][4]=Ev[i].getEventType().toString();
			    data[k][5]=Ev[i].getEvent_DateTime().toString();
			    k++;
			}
			table(Fields,data);
			System.out.print("Total Events: ");
			System.out.println(k);
		}
		
	}
	/**
	 * Guides the user through creating a new event.
	 * <p>
	 * Collects the event name, description, location, type (PUBLIC/PRIVATE), and a
	 * future date/time.  The user is shown a summary table and asked to confirm before
	 * the event is persisted via {@link FileIO#EnterNewEvent}.
	 * </p>
	 */
	private static void Create_Event()
	{
		String ID,name,description,location;
		Event_t Ev_type;
		ZonedDateTime zonedatetime;
		Event event;
		while(true)
		{
			while(true) {
				name=input("Enter the name of Event (enter 0 to go back)");
				if(name.equals("0")) return;
				if(name.contains(","))
				{
					UI_utility.PrintWarning("Entering , corrupts file system. Try again");
					continue;
				}
				break;
			}
			while(true)
			{
				description= input("Enter the description of Event");
				if(description.equals("0")) return;
				if(description.contains(","))
				{
					UI_utility.PrintWarning("Entering , corrupts file system. Try again");
					continue;
				}
				break;
			}
			while(true)
			{
				location= input("Enter the location of Event");
				if(location.equals("0")) return;
				if(location.contains(","))
				{
					UI_utility.PrintWarning("Entering , corrupts file system. Try again");
					continue;
				}
				break;
			}
			
			while(true)
			{
				String type= input("Enter the Event type (PUBLIC / PRIVATE)");
				if(type.equals("0")) return;
				if(type.equalsIgnoreCase("PUBLIC"))
				{
					type="PUBLIC";
					Ev_type=Event_t.valueOf(type);
					break;
				}
				else if(type.equalsIgnoreCase("PRIVATE"))
				{
					type="PRIVATE";
					Ev_type=Event_t.valueOf(type);
					break;
				}
				else
				{
					UI_utility.PrintWarning("INVALID INPUT");
					continue;
				}
			}
			
			while(true)
			{
				String datetime= input("Enter the Date and Time of event (yyyy-MM-dd HH:mm:ss)");
				if(datetime.equals("0")) return;
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				try
				{
					zonedatetime=LocalDateTime.parse(datetime, formatter).atZone(ZoneId.systemDefault());
					if(zonedatetime.isBefore(ZonedDateTime.now()))
					{
						UI_utility.PrintNoAccess("Can't create an Event in past.");
						continue;
					}
					break;
				}
				catch(DateTimeParseException e)
				{
					UI_utility.PrintError("Failed to parse date: " + e.getMessage());
					UI_utility.PrintError("Error occurred at index: " + e.getErrorIndex());
				}
			}
			table(new String[]{"Name","Description","Location","Type","Date and Time"},new String[][]{{name,description,location,Ev_type.toString(),zonedatetime.toString()}});
			boolean ans=prompt("Are You sure you want to add the above event?");
			if(ans==true)
			{
				if((ID=FileIO.Create_EventID())!=null)
				{
					event=new Event(ID,name,description,location,Ev_type,zonedatetime);
					
					if(!FileIO.EnterNewEvent(event))
					{
						UI_utility.PrintError("Error occured while creating Event files and folder.");
						return;
					}
					else
					{
						UI_utility.PrintSuccess("Event Created Successfully");
						return;
					}
				}
				else
				{
					UI_utility.PrintError("Error occured while creating Event ID.");
					return;
				}
			}
			else continue;
			
		}
	}
	/**
	 * Guides the user through editing an existing event they own.
	 * <p>
	 * The user supplies the event ID, then re-enters all editable fields (name,
	 * description, location, type, and date/time except ID).  Past events cannot be edited.
	 * Changes are confirmed with a summary table before being saved via
	 * {@link FileIO#EditEvent}.
	 * </p>
	 */
	private static void Edit_Event()
	{
		String ID;
		Event Ev[];
		String name,description,location;
		Event_t Ev_type;
		ZonedDateTime zonedatetime;
		Event new_event;
		while(true)
		{
			 ID=input("Enter the ID of the Event that you wish to edit (Enter 0 to go back)");
			 if(ID.equals("0")) return;
			 Ev=FileIO.GetUserEvent(ID);
			 if(Ev==null || Ev.length==0)
			 {
				 UI_utility.PrintWarning("No such Event");
				 continue;
			 }
			 else if(Ev[0].getEvent_DateTime().isBefore(ZonedDateTime.now()))
			 {
				 UI_utility.PrintWarning("Can't edit an event that is already finished.");
				 continue;
			 }
			 else
			 {
				 table(new String[]{"ID","name","description","location","Type","Date and Time"},new String[][] {{ID,Ev[0].getName(),Ev[0].getDescription(),Ev[0].getLocation(),Ev[0].getEventType().toString(),Ev[0].getEvent_DateTime().toString()}});
				 UI_utility.PrintSuccess("Event Found.");
			 }
			 while(true) {
					name=input("Enter the name of Event (enter 0 to go back)");
					if(name.equals("0")) return;
					if(name.contains(","))
					{
						UI_utility.PrintWarning("Entering , corrupts file system. Try again");
						continue;
					}
					break;
				}
				while(true)
				{
					description= input("Enter the description of Event");
					if(description.equals("0")) return;
					if(description.contains(","))
					{
						UI_utility.PrintWarning("Entering , corrupts file system. Try again");
						continue;
					}
					break;
				}
				while(true)
				{
					location= input("Enter the location of Event");
					if(location.equals("0")) return;
					if(location.contains(","))
					{
						UI_utility.PrintWarning("Entering , corrupts file system. Try again");
						continue;
					}
					break;
				}
				
				while(true)
				{
					String type= input("Enter the Event type (PUBLIC / PRIVATE)");
					if(type.equals("0")) return;
					if(type.equalsIgnoreCase("PUBLIC"))
					{
						type="PUBLIC";
						Ev_type=Event_t.valueOf(type);
						break;
					}
					else if(type.equalsIgnoreCase("PRIVATE"))
					{
						type="PRIVATE";
						Ev_type=Event_t.valueOf(type);
						break;
					}
					else
					{
						UI_utility.PrintWarning("INVALID INPUT");
						continue;
					}
				}
				
				while(true)
				{
					String datetime= input("Enter the Date and Time of event (yyyy-MM-dd HH:mm:ss)");
					if(datetime.equals("0")) return;
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					try
					{
						zonedatetime=LocalDateTime.parse(datetime, formatter).atZone(ZoneId.systemDefault());
						if(zonedatetime.isBefore(ZonedDateTime.now()))
						{
							UI_utility.PrintNoAccess("Can't create an Event in past.");
							continue;
						}
						break;
					}
					catch(DateTimeParseException e)
					{
						UI_utility.PrintError("Failed to parse date: " + e.getMessage());
						UI_utility.PrintError("Error occurred at index: " + e.getErrorIndex());
					}
				}
				table(new String[]{"ID","Name","Description","Location","Type","Date and Time"},new String[][]{{ID,name,description,location,Ev_type.toString(),zonedatetime.toString()}});
				boolean ans=prompt("Are You sure you want to Save edit?");
				if(ans==true)
				{
					
					new_event=new Event(ID,name,description,location,Ev_type,zonedatetime);	
					if(!FileIO.EditEvent(new_event))
					{
						UI_utility.PrintError("Error occured while Editing Event files.");
						return;
					}
					else
					{
						UI_utility.PrintSuccess("Event Edited Successfully");
						return;
					}
				}
				else continue;
		}
	}
	/**
	 * Allows the current user to delete one of their own events.
	 * <p>
	 * Past events cannot be deleted.  The user is shown the event details and must
	 * confirm the deletion before {@link FileIO#DeleteEvent} is called.
	 * </p>
	 */
	private static void Delete_Event()
	{
		String ID;
		Event Ev[];
		while(true)
		{
			 ID=input("Enter the ID of the Event that you wish to Delete (Enter 0 to go back)");
			 if(ID.equals("0")) return;
			 Ev=FileIO.GetUserEvent(ID);
			 if(Ev==null || Ev.length==0)
			 {
				 UI_utility.PrintWarning("No such Event");
				 continue;
			 }
			 else if(Ev[0].getEvent_DateTime().isBefore(ZonedDateTime.now()))
			 {
				 UI_utility.PrintWarning("Can't delete an event that is already finished.");
				 continue;
			 }
			 else
			 {
				 UI_utility.PrintSuccess("Event Found.");
				 table(new String[]{"ID","name","description","location","Type","Date and Time"},new String[][] {{ID,Ev[0].getName(),Ev[0].getDescription(),Ev[0].getLocation(),Ev[0].getEventType().toString(),Ev[0].getEvent_DateTime().toString()}});

			 }
			 boolean ans=prompt("Are You sure you want to Delete the event?");
			 if(ans==true)
			{
				if(!FileIO.DeleteEvent(Ev[0]))
				{
					UI_utility.PrintError("Error occured while creating Event files and folder.");
					return;
				}
				else
				{
					UI_utility.PrintSuccess("Event Deleted Successfully");
						return;
				}
			}
				else continue;
		}
	}
	/**
	 * Displays the friends management sub-menu.
	 * <p>
	 * Changes to the friend list are held in memory and only written to disk when the
	 * user selects "Save and Quit" (index {@code 0}).
	 * </p>
	 */
	private static void Manage_Friends()
	{
		boolean Friend_List_Changed=false;
		while(true)
		{
			int choice=prompt("Save and Quit","View Friends","View Friend Requests","Send Friend Requests","Respond to Friend Request","Remove Friends");
			if(choice==0)
			{
				if(Friend_List_Changed==true)
				{
					if(FileIO.SaveUserFriends() && FileIO.SetUserFriends())
					{
						UI_utility.PrintSuccess("Changes Saved Successfully");
					}
					else UI_utility.PrintError("Error Occured while saving changes.");
				}
				return;
			}
			else if(choice==1)
			{
				View_Friends();
				
			}
			else if(choice==2)
			{
				View_FriendRequests();
			}
			else if(choice==3)
			{
				Send_FriendRequests();
			}
			else if(choice==4)
			{
				if(Respond_FriendRequests())
				{
					Friend_List_Changed=true;
				}
			}
			else if(choice==5)
			{
				if(Remove_Friends())
				{
					Friend_List_Changed=true;
				}
			}
		}
	}
	/**
	 * Displays the current user's friend list in a formatted table.
	 * <p>
	 * The user can view all friends, search by a username substring, or look up a
	 * friend by their exact ID.
	 * </p>
	 */
	private static void View_Friends()
	{
		User[] friends;
		String data[][];
		String Fields[]= {"ID","Username"};
		int choice_in;
		while(true)
		{
			choice_in = prompt("Go Back","View All Friends","Search Friends by Username Substring","Search Friend by ID");
			if(choice_in ==0) return;
			else if(choice_in ==1)
			{
				friends = CurrentSession.CurrentUser.getFriend();
				data= new String[CurrentSession.CurrentUser.getFriendCount()][2];
			}
			else if (choice_in==2)
			{	
				friends = CurrentSession.CurrentUser.getFriendByName(input("Enter the username(or substring)"));
				data= new String[friends.length][2];
			}
			else if (choice_in ==3)
			{
				friends = CurrentSession.CurrentUser.getFriendByID(input("Enter the ID"));
				data= new String[friends.length][2];
			}
			else continue;
			
			if(data.length==0) 
			{
				System.out.println("Total Friends: 0");
				continue;
			}
			int k = 0; 
			for(int i=0;i<friends.length;i++)
			{
			    if(friends[i]==null) continue;
			    data[k][0]=friends[i].getID();
			    data[k][1]=friends[i].getUsername();
			    k++;
			}
			table(Fields,data);
			System.out.print("Total Friends: ");
			System.out.println(friends.length);
		}
	}
	/**
	 * Retrieves all pending incoming friend requests for the current user and displays
	 * them in a formatted table showing the sender's ID and username.
	 */
	private static void View_FriendRequests()
	{
		FriendRequest FR[]=FileIO.GetFriendRequests();//yet to do
		int k=0;
		String fields[]= {"ID","Username"};
		String Data[][]=new String[FR.length][2];
		for(int i=0;i<FR.length;i++)
		{
			if(FR[i] ==null)continue;
			Data[i][0]=FR[i].getFrom();
			Data[i][1]=FileIO.SearchUserID(FR[i].getFrom()).getUsername();
			k++;
		}
		table(fields,Data);
		System.out.println("Total number of requests: "+k);
	}

	/**
	 * Allows the current user to send a friend request to another user by their ID or
	 * username.
	 * <p>
	 * The method prevents duplicate requests (both sent and already-received) and
	 * prevents a user from sending a request to themselves or to an existing friend.
	 * </p>
	 */
	private static void Send_FriendRequests()
	{
		User us=null;
		go_back:
		while (true)
		{
			int choice_in=prompt("Go Back","Send Friend Request By ID","Send Friend Request By Username");
			
			if(choice_in ==0) return;
			else if(choice_in==1)
			{
				while(true)
				{
					String ID=input("Enter a valid ID to send friend request(Enter 0 to exit)");
					if(ID.equals("0")) break go_back;
					if(ID.equals(CurrentSession.CurrentUser.getID()))
					{
						UI_utility.PrintWarning("Can't be friend to yourself.");
						continue;
					}
					if(CurrentSession.CurrentUser.getFriendByID(ID).length!=0)
					{
						UI_utility.PrintWarning("Friend Already exists");
						continue;
					}
					us=null;
					us=FileIO.SearchUserID(ID);
				}
			}
			else if(choice_in==2)
			{
				while(true)
				{	String name=input("Enter a Valid Username to send friend request(Enter 0 to exit)");
					if(name.equals("0")) break go_back;
					if(name.equals(CurrentSession.CurrentUser.getUsername()))
					{
						UI_utility.PrintWarning("Can't be friend to yourself.");
						continue;
					}
					if(CurrentSession.CurrentUser.getFriendByName(name).length!=0)
					{
						UI_utility.PrintWarning("Friend Already exists");
						continue;
					}
					us=null;
					us=FileIO.SearchUsername(name);
				}
			}
			if(us==null)
			{
				UI_utility.PrintWarning("No such User.");
				continue;
			}
			else
			{
				UI_utility.PrintSuccess("User Found.");
				table( new String[]{"ID","Name"}  ,new String[][] { {us.getID(),us.getUsername()} } );
				if(prompt("Do you want to send friend request?"))
				{
					boolean sent_already=false;
					boolean already_received=false;
					FriendRequest Received_FR[]=FileIO.GetFriendRequests();
					FriendRequest AlreadySent[]=FileIO.GetSentRequests();
					for(int i=0;i<AlreadySent.length;i++)
					{
						if(AlreadySent[i]==null)continue;
						if(AlreadySent[i].getTo().equals(us.getID()))
						{
							UI_utility.PrintWarning("You have already sent a friend request which has not been respond yet.");
							sent_already=true;
							break;
						}
					}
					for(int i=0;i<Received_FR.length;i++)
					{
						if(Received_FR[i]==null)continue;
						if(Received_FR[i].getFrom().equals(us.getID()))
						{
							UI_utility.PrintWarning("You have already received a friend request which you have not respond yet.");
							already_received=true;
							break;
						}
					}
					if(sent_already||already_received) continue;
					FileIO.EnterFriendRequest(new FriendRequest(CurrentSession.CurrentUser.getID(),us.getID()));
				}
			}
		}
	}
	/**
	 * Allows the current user to accept or reject a pending incoming friend request.
	 * <p>
	 * The user identifies the request by the sender's ID.  On acceptance the sender is
	 * added to the current user's friend list and the response is persisted via
	 * {@link FileIO#RespondToRequest}.
	 * </p>
	 *
	 * @return {@code true} if at least one friend was added during this interaction;
	 *         {@code false} otherwise
	 */
	private static boolean Respond_FriendRequests()
	{
		FriendRequest FR[]=FileIO.GetFriendRequests();
		String ID;
		boolean added=false;
		User friend;
		RequestResponse response;
		while(true)
		{
			ID=input("Enter the ID of Friend Request sender(0 to go back) ");
			if(ID.equals("0")) return added;
			boolean found=false;
			friend=FileIO.SearchUserID(ID);
			if(friend==null)
			{
				UI_utility.PrintWarning("No such User");
				continue;
			}
			for(int i=0;i<FR.length;i++)
			{
				if(FR[i]==null)continue;
				if(FR[i].getFrom().equals(ID))
				{
					UI_utility.PrintSuccess("Friend Request found");
					found=true;
					break;
				}
			}
			if(found==false)
			{
				UI_utility.PrintWarning("No such Friend Request");
				continue;
			}
			while (true)
			{
				String ans=input("Enter ACCEPT or REJECT(enter 0 to go back)");
				if(ans.equals("0"))break;
				if(ans.equalsIgnoreCase("ACCEPT"))
				{
					response=RequestResponse.ACCEPTED;
					added=true;
					CurrentSession.CurrentUser.addFriend(friend);
					FileIO.RespondToRequest(new FriendRequest(friend.getID(),CurrentSession.CurrentUser.getID(),response));
					UI_utility.PrintSuccess("Response send successfully");
					break;
				}
				else if(ans.equalsIgnoreCase("REJECT"))
				{
					response=RequestResponse.REJECTED;
					FileIO.RespondToRequest(new FriendRequest(friend.getID(),CurrentSession.CurrentUser.getID(),response));
					UI_utility.PrintSuccess("Response send successfully");
					break;
				}
				else
				{
					UI_utility.PrintWarning("INVALID INPUT");
					continue;
				}
			}
		}
	}
	/**
	 * Allows the current user to remove existing friends either by their ID or
	 * username.
	 * <p>
	 * Removal is confirmed interactively before calling
	 * {@link FileIO#SendRemoveFriendResponse}.
	 * </p>
	 *
	 * @return {@code true} if at least one friend was removed; {@code false} otherwise
	 */
	private static boolean Remove_Friends()
	{
		boolean friend_removed=false;
		while (true)
		{
			int choice_in=prompt("Go Back","Remove Friends By ID","Remove Friend By Username");
			
			if(choice_in ==0) return friend_removed;
			else if(choice_in==1)
			{
				while(true)
				{
					String ID=input("Enter a valid ID to remove friend(Enter 0 to exit)");
					if(ID.equals("0")) break;

					User us[]=UI.CurrentSession.CurrentUser.getFriendByID(ID);
					if(us.length==0) UI_utility.PrintWarning("No such Friend.");
					else
					{
						UI_utility.PrintSuccess("Friend Found.");
						table( new String[]{"ID","Name"}  ,new String[][] { {us[0].getID(),us[0].getUsername()} } );
						if(prompt("Do you want to remove as friend?"))
						{
							UI.CurrentSession.CurrentUser.removeFriend(us[0]);
							FileIO.SendRemoveFriendResponse(us[0]);
							friend_removed=true;
						}
					}
				}
			}
			else if(choice_in==2)
			{
				while(true)
				{	String name=input("Enter a Valid Username to remove a friend(Enter 0 to exit)");
					if(name.equals("0")) break;
					User us[]=CurrentSession.CurrentUser.getFriendByName(name);
					if(us.length!=1 || !us[0].getUsername().equals(name))
					{
						UI_utility.PrintWarning("No such friend.");
						continue;
					}
					else
					{
						UI_utility.PrintSuccess("User Found.");
						table( new String[]{"ID","Name"}  ,new String[][] { {us[0].getID(),us[0].getUsername()} } );
						if(prompt("Do you want to remove as friend?"))
						{
							UI.CurrentSession.CurrentUser.removeFriend(us[0]);
							FileIO.SendRemoveFriendResponse(us[0]);
							friend_removed=true;
						}
					}
				}
			}
		}
	}
	/**
	 * Holds all state that is scoped to a single authenticated user session.
	 * <p>
	 * Fields are populated by {@link #initialize(User)} at login and cleared by
	 * {@link #endSession()} at logout.  The class is {@code static} so it can be
	 * referenced without an enclosing {@link UI} instance.
	 * </p>
	 */
	static class CurrentSession
	{
		/** The {@link User} object representing the currently logged-in user. */
		static User CurrentUser;
 
		/** Path to the top-level folder for the current user (e.g. {@code "user_<ID>"}). */
		static String User_Folder;
 
		/** Path to the current user's notifications file. */
		static String User_notification_file;
 
		/** Path to the current user's invitations file. */
		static String User_invitations_file;
 
		/** Path to the current user's friend-list file. */
		static String User_friendlist_file;
 
		/** Path to the file recording the events created by the current user. */
		static String User_events_file;
 
		/** Path to the events folder that stores individual event files. */
		static String Event_Folder;
 
		/** Array of file names found inside {@link #Event_Folder} at session start. */
		static String Event_Files[];
 
		/** Path to the file storing miscellaneous user information (e.g. last login time). */
		static String User_Info_file;
 
		/** Path to the file listing events the current user has agreed to attend. */
		static String User_upcomingevent_file;
 
		/** Path to the file storing incoming friend requests. */
		static String User_received_request_file;
 
		/** Path to the file storing outgoing friend requests. */
		static String User_sent_request_file;
 
		/** Raw string content read from {@link #User_Info_file} (e.g. last session timestamp). */
		static String UserInfo;
 
		/** The {@link ZonedDateTime} at which the current session was started. */
		static ZonedDateTime StartTime;
 
		/** The {@link ZonedDateTime} at which the previous session ended (last login time). */
		static ZonedDateTime LastSessionTime;
 
		/** In-memory list of {@link Notification} objects loaded at session start. */
		static ArrayList<Notification> Notifications =new ArrayList<>();
		/**
		 * Initialises the session for the given authenticated user.
		 * <p>
		 * Builds all file/folder path strings, records the session start time, loads
		 * friends, notifications, invitations, upcoming events, invitation responses,
		 * and friend request data from disk.  If any critical load step fails the
		 * session is torn down via {@link #endSession()} and {@code false} is returned.
		 * </p>
		 *
		 * @param us the authenticated {@link User} for whom the session is being created
		 * @return {@code true} if all session data was loaded successfully;
		 *         {@code false} if a critical error occurred and the session was aborted
		 */
		static boolean initialize(User us)
		{
			CurrentUser=us;
			User_Folder			  ="user_"+us.getID();
			User_notification_file=User_Folder+"/notifications.txt";
			User_invitations_file  =User_Folder+"/invitations.txt";
			User_friendlist_file  =User_Folder+"/friendlist.txt";
			User_Info_file		  =User_Folder+"/userinfo.txt";
			User_events_file	  =User_Folder+"/user_events.txt";
			User_upcomingevent_file =User_Folder+"/event_attend.txt";
			Event_Folder		  =User_Folder+"/events";
			User_received_request_file=User_Folder+"/received_requests.txt";
			User_sent_request_file=User_Folder+"/sent_requests.txt";
			
			{
				
			StartTime=ZonedDateTime.now();
			
			if(!FileIO.SetUserInfo())//setting previous login time
			{
				UI_utility.PrintError("\nError occured while reading user info. User info Data Lost.\nTry Again");
				endSession();
				return false;
			}
			else
			{
				if(UserInfo!=null)
					{
						LastSessionTime= ZonedDateTime.parse(UserInfo);
						UI_utility.PrintSuccess("User info set.");
					}
				else LastSessionTime=ZonedDateTime.now().withYear(0);
			}
			if(!FileIO.SetUserFriends())
			{
				UI_utility.PrintError("\nError occured while reading friends. Friend data Lost.\nTry Again");
				endSession();
				return false;
			}
			else
			{
				UI_utility.PrintSuccess("User friendlist set.");
			}
			
			if(FileIO.CreateFile(User_notification_file)&&FileIO.CreateFile(User_invitations_file)&&
					FileIO.CreateFile(User_events_file)&& FileIO.CreateFile(User_upcomingevent_file)&&
					FileIO.CreateFolder(Event_Folder)&&FileIO.CreateFile(User_sent_request_file)&&
					FileIO.CreateFile(User_received_request_file))
			{
				UI_utility.PrintSuccess("Miscellaneous files set.");
			}
			else
			{
				UI_utility.PrintError("\nError occured while finding miscellaneous files. Possible data Lost.\nTry Again");
			}
			File EventsFolder= new File(Event_Folder);
			File[] files = EventsFolder.listFiles();
			
			if(files != null)
			{
			    Event_Files = new String[files.length];

			    int i = 0;
			    for(File f : files)
			    {
			        Event_Files[i] = f.getName();
			        i++;
			    }
			}
			else
			{
			    Event_Files = new String[0];// prevents null pointer exception
			}
			
			if(!FileIO.SetUserNotifications())
			{
				UI_utility.PrintError("Error occured while setting notification");
				endSession();
				return false;
			}
			if(FileIO.UpdateReceivedInvitations())
			{
				UI_utility.PrintSuccess("Received Invitations Updated");
			}
			else
			{
				UI_utility.PrintError("Error occured while updating received Invitations");
				endSession();
				return false;
			}
			if(FileIO.UpdateUpcomingUserEvents())
			{
				UI_utility.PrintSuccess("Upcoming events Updated");
			}
			else
			{
				UI_utility.PrintError("Error occured while updating Upcoming events");
				endSession();
				return false;
			}
			if(FileIO.UpdateResponseOfInvitations())
			{
				UI_utility.PrintSuccess("Response of Invitations Updated");
			}
			else
			{
				UI_utility.PrintError("Error occured while updating Response of Invitations");
				endSession();
				return false;
			}
			if(FileIO.UpdateReceivedAndSentFriendRequests())
			{
				UI_utility.PrintSuccess("Received and Sent Invitaion Updated");
			}
			else
			{
				UI_utility.PrintError("Error occured while updating Received and Sent Invitation");
				endSession();
				return false;
			}
		}
			return true;
		}
		/**
		 * Persists session data to disk and clears all session-scoped fields.
		 * <p>
		 * Saves user info (last session time), friends, and notifications.  Any save
		 * failure is reported via {@link UI_utility#PrintError} but does not prevent
		 * the remaining fields from being nulled out.
		 * </p>
		 *
		 * @return {@code true} always (cleanup is always attempted regardless of I/O errors)
		 */
		static boolean endSession()
		{
			if(!FileIO.SaveUserInfo())
			{
				UI_utility.PrintError("A Fatal Error occured while saving session info.");
			}
			if(!FileIO.SaveUserFriends())
			{
				UI_utility.PrintError("A Fatal Error occured while saving user friends");
			}
			if(!FileIO.SaveUserNotification())
			{
				UI_utility.PrintError("A Fatal Error occured while saving notifications.");
			}
			StartTime=null;
			LastSessionTime=null;
			CurrentUser=null;
			User_Folder=null;
			User_notification_file=null;
			User_invitations_file=null;
			User_friendlist_file=null;
			User_Info_file=null;
			User_upcomingevent_file=null;
			Event_Folder=null;
			for(int i = 0; i < Event_Files.length; i++)
			{
			    Event_Files[i] = null;
			}
			Event_Files=null;
			Notifications.clear();
			return true;
		}
	}
}