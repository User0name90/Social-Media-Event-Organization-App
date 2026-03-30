package event_org;
/**
 *  {@summary enum RSVP acts as an indicator of the current status of invitation
 *  as well as the reply. It can have 4 different values}
 */
enum RSVP{ACCEPTED,DECLINED,MAYBE,PENDING}
/**
 * This class act as a container that stores information regarding the sender and receiver as well
 * as the status. Each Invitation for each event has a separate ID. A user CAN NOT send an
 * invitation twice to the same person event after decline.
 */
class Invitation {
	/** The ID of the invitation is stored in the format Event_ID.Unique_Invitation_ID 
	 * the former part of the IDs before '.' are same for a particular event.
	 * the latter part of 2 ID's can also be same for different events like 0000000000.00000000003 and 0000000004.0000000003
	 * but both the former and latter of 2 different ID's can never be the same.
	 */
	final private String ID;
	/** Stores the User who is sending the invitation*/
	final private User From;
	/** Stores the User who is receiving the invitation*/
	final private User To;
	/** Tells the status as well as the reply of the invitation*/
	private RSVP Status;
	/**
	 * Constructor for the Invitation class that is used while sending the invitation by setting 
	 * Status to initial state of PENDING.
	 * @param ID : Full unique ID of the invitation 
	 * @param from : The User object who is sending the invite
	 * @param to : The User object that will receive the file 
	 */
	Invitation(String ID,User from,User to)
	{
		this.ID=ID;
		this.From=from;
		this.To=to;
		this.Status=RSVP.PENDING;
	}
	/**
	 * It is an overloaded method that relaxes the condition of parameters from User to String
	 * @param ID : Full unique ID of the invitation 
	 * @param from_ID : The ID of the User object sending the invitation
	 * @param to_ID : The ID of the User object that will receive the invitation
	 */
	Invitation(String ID,String from_ID,String to_ID)
	{
		this.ID=ID;
		this.From=FileIO.SearchUserID(from_ID);
		this.To=FileIO.SearchUserID(to_ID);
		this.Status=RSVP.PENDING;
	}
	/**
	 * Constructor for the Invitation class that is used while responding the invitation by setting 
	 * Status to the three predefined values
	 * @param ID : Full unique ID of the invitation 
	 * @param from : The User object who is sending the invite
	 * @param to : The User object that will receive the file 
	 * @param status: the reply from the receiver
	 */
	Invitation(String ID,User from,User to,RSVP status)
	{
		this.ID=ID;
		this.From=from;
		this.To=to;
		this.Status=status;
	}
	/**
	 * It is an overloaded method that relaxes the condition of parameters from User to String
	 * @param ID : Full unique ID of the invitation 
	 * @param from : The User object who is sending the invite
	 * @param to : The User object that will receive the file 
	 * @param status: the reply from the receiver
	 */
	Invitation(String ID,String from_ID,String to_ID,RSVP status)
	{
		this.ID=ID;
		this.From=FileIO.SearchUserID(from_ID);
		this.To=FileIO.SearchUserID(to_ID);
		this.Status=status;
	}
	/**
	 * sets a new value to the Status instance variable
	 * @param new_status : The RSVP value of the reply
	 */
	void setStatus(RSVP new_status)
	{
		this.Status=new_status;
	}
	/**
	 * @return enum RSVP value of the Status instance variable
	 */
	RSVP getStatus()
	{
		return this.Status;
	}
	/**
	 * @return a String containing Event ID part of the full_ID
	 */
	String getEventID()
	{
		return this.ID.substring(0, 10);
	}
	/**
	 * @return A User object containing the information of the sender
	 */
	User getFrom()
	{
		return this.From;
	}
	/**
	 * @return A User object containing the information of the receiver
	 */
	User getTo()
	{
		return this.To;
	}
	/**
	 * @return the String containing the full ID of 21 characters of invitation
	 */
	String getID()
	{
		return this.ID;
	}
	/**
	 * @return the String containing the segment of ID after '.' character.
	 */
	String getInviteID()
	{
		return this.ID.substring(11);
	}
}
