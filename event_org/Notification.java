package event_org;

import java.time.ZonedDateTime;

/**
 * Represents a generic notification in the system.
 * Contains message content, generation time, and view status.
 */
class Notification {
	
	/** Time when the notification was generated */
	protected ZonedDateTime generation_time;
	/** Indicates whether the notification has been viewed. true if notification is viewed */
	protected boolean Viewed_Status;
	/** Message content of the notification */
	protected String message;
	/**
	 * Default constructor.
	 * Initializes the notification with current time and not viewed status.
	 */
	Notification()
	{
		Viewed_Status=false;
		generation_time=ZonedDateTime.now();
	}
	/**
	 * Parameterized constructor. It can be used for creating notifications that are not very frequent like
	 * when a friend has changed their user name.
	 * @param msg Message content
	 * @param status Viewed status
	 * @param generation_time Time of generation
	 */
	Notification(String msg,boolean status,ZonedDateTime generation_time)
	{
		this.message=msg;
		this.Viewed_Status=status;
		this.generation_time=generation_time;
	}
	/**
	 * Sets the viewed status of the notification.
	 * @param status true if viewed, false otherwise
	 */
	void SetViewed_Status(boolean status)
	{
		this.Viewed_Status=status;
	}

	/**
	 * Gets the viewed status of the notification.
	 * @return true if viewed, false otherwise
	 */
	boolean getView_Status()
	{
		return this.Viewed_Status;
	}

	/**
	 * Returns the notification message.
	 * @return message string
	 */
	String GetNotification()
	{
		return message;
	}

	/**
	 * Returns the generation time of the notification.
	 * @return ZonedDateTime object containing the date and time of generation
	 */
	ZonedDateTime getGenerationTime()
	{
		return this.generation_time;
	}
}
/**
 * This class inherits Notification and helps in creating notifications for Event cancellation.
 */
class EventCancel_Notification extends Notification
{
	/** The event that has been cancelled */
	private Event DeletedEvent;

	/**
	 * Constructor for event cancellation notification.
	 * @param ev Event that was deleted/cancelled
	 */
	EventCancel_Notification(Event ev)
	{
		super();
		this.DeletedEvent=ev;
		message="Upcomming event "+this.DeletedEvent.getID()+" is cancelled.";
	}
}
/**
 * This class inherits Notification class and helps in creating Notification for events that are changed.
 */
class EventChange_Notification extends Notification
{
	/** Updated event */
	private Event ChangedEvent;

	/** Original event before changes */
	private Event OldEvent;
	/**
	 * Constructor for event change notification. Initializes message depending on which field of the event
	 * has changed.
	 * @param old_ev Original event
	 * @param changed_ev Updated event
	 */
	EventChange_Notification(Event old_ev,Event changed_ev)
	{
		super();
		this.OldEvent=old_ev;
		this.ChangedEvent=changed_ev;
		if(this.OldEvent.equals(this.ChangedEvent))
		{
			super.message=null;
			return;
		}
		message="Upcoming event "+ChangedEvent.getID()+"Changed.";
		if(!this.OldEvent.getName().equals(this.ChangedEvent.getName()))
		{
			message=message.concat("\tName changed to: "+ChangedEvent.getName());
		}
		if(!this.OldEvent.getDescription().equals(this.ChangedEvent.getDescription()))
		{
			message=message.concat("\tDescription changed to: "+ChangedEvent.getDescription());
		}
		if(!this.OldEvent.getLocation().equals(this.ChangedEvent.getLocation()))
		{
			message=message.concat("\tLocation changed to: "+ChangedEvent.getLocation());
		}
		if(!this.OldEvent.getEventType().equals(this.ChangedEvent.getEventType()))
		{
			message=message.concat("\tEvent Type changed to: "+ChangedEvent.getEventType().toString());
		}
		if(!this.OldEvent.getEvent_DateTime().equals(this.ChangedEvent.getEvent_DateTime()))
		{
			message=message.concat("\tEvent Date and Time changed to: "+this.ChangedEvent.getEvent_DateTime());
		}
	}
}
/**
 * This class inherits Notification class and helps in creating Notification for a received invitation for an event.
 */
class Invite_Notification extends Notification
{
	/** Invitation associated with the notification */
	private Invitation invitation;

	/**
	 * Constructor for invitation notification.
	 *
	 * @param iv Invitation object
	 * @param dt Time when notification is generated
	 */
	Invite_Notification(Invitation iv,ZonedDateTime dt)
	{
		this.invitation=iv;
		this.generation_time=dt;
		message=invitation.getFrom().getUsername()+" ("+invitation.getFrom().getID()+")"+" invited you for an event with ID "+ invitation.getEventID();
		message=message+"  at"+generation_time.toString();
	}
}
/**
 * This class inherits Notification class and helps in creating Notification when an invited user has responded to an invitation.
 */
class Response_Notification extends Notification
{
	private Invitation invitation;
	Response_Notification(Invitation iv,ZonedDateTime dt)
	{
		this.invitation=iv;
		this.generation_time=dt;
		message=invitation.getTo().getUsername()+" ("+invitation.getTo().getID()+")"+" has responded to invitation for Event with ID "+invitation.getEventID()+": "+this.invitation.getStatus().toString();
		message=message+"  at "+generation_time.toString();
	}
}