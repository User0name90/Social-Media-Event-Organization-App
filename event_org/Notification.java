package event_org;

import java.time.ZonedDateTime;

class Notification {
	protected ZonedDateTime generation_time;
	protected boolean Viewed_Status;
	protected String message;
	Notification()
	{
		Viewed_Status=false;
		generation_time=ZonedDateTime.now();
	}
	Notification(String msg,boolean status,ZonedDateTime generation_time)
	{
		this.message=msg;
		this.Viewed_Status=status;
		this.generation_time=generation_time;
	}
	void SetViewed_Status(boolean status)
	{
		this.Viewed_Status=status;
	}
	boolean getView_Status()
	{
		return this.Viewed_Status;
	}
	String GetNotification()
	{
		return message;
	}
	ZonedDateTime getGenerationTime()
	{
		return this.generation_time;
	}
}
class EventCancel_Notification extends Notification
{
	Event DeletedEvent;
	EventCancel_Notification(Event ev)
	{
		this.DeletedEvent=ev;
		message="Upcomming event "+this.DeletedEvent.getID()+" is cancelled.";
	}
}
class EventChange_Notification extends Notification
{
	Event ChangedEvent;
	Event OldEvent;
	EventChange_Notification(Event old_ev,Event changed_ev)
	{
		this.OldEvent=old_ev;
		this.ChangedEvent=changed_ev;
		if(this.OldEvent.equals(this.ChangedEvent))
		{
			super.message=null;
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

class Invite_Notification extends Notification
{
	Invitation invitation;
	Invite_Notification(Invitation iv,ZonedDateTime dt)
	{
		this.invitation=iv;
		this.generation_time=dt;
		message=invitation.getFrom().getUsername()+" ("+invitation.getFrom().getID()+")"+" invited you for an event with ID "+ invitation.getEventID();
		message=message+"  at"+generation_time.toString();
	}
}
class Response_Notification extends Notification
{
	Invitation invitation;
	Response_Notification(Invitation iv,ZonedDateTime dt)
	{
		this.invitation=iv;
		this.generation_time=dt;
		message=invitation.getTo().getUsername()+" ("+invitation.getTo().getID()+")"+" has responded to invitation for Event with ID "+invitation.getEventID()+": "+this.invitation.getStatus().toString();
		message=message+"  at "+generation_time.toString();
	}
}