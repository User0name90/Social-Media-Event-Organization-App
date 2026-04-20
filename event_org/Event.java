package event_org;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
/**
 * enum Event_t contains two values PRIVATE and PUBLIC which helps in storing the event type in Event class
 */
enum Event_t {PRIVATE,PUBLIC}
/**
 * This class stores all information of an Event and methods relating to Creating and Editing Events.
 * Each event has a unique ID of 10 characters and each character can take value from  0 to 9 and A to Z 
 */
class Event {
	/**
	 * Unique 10 character ID of the event.
	 */
	final private String ID;
	/**
	 * Name of the event. Two Events can have same name.
	 */
	private String name;
	/**
	 * Description of the Event
	 */
	private String Description;
	/**
	 * Location of the Event
	 */
	private String Location;
	/**
	 * Stores the type of the event, either PUBLIC of PRIVATE
	 */
	private Event_t EventType;
	/**
	 * Stores Date and Time of the Event
	 */
	private ZonedDateTime Event_DateTime;
	/**
	 * Constructor of the Event class. It requires values of all the values of Data members.
	 * @param ID : The String object containing unique ID generated from FileIO.Create_EventID()
	 * @param name : String Object containing Name of the upcoming event
	 * @param description : Description of the upcoming Event
	 * @param Location : Location of the upcoming Event
	 * @param EventType : Type of the Event.
	 * @param Event_DT : ZonedDateTime object containing upcoming date and time of the Event.
	 */
	Event(String ID,String name,String description,String Location,Event_t EventType,ZonedDateTime Event_DT)
	{
		this.ID=ID;
		this.name=name;
		this.Description=description;
		this.Location=Location;
		this.EventType=EventType;
		this.Event_DateTime=Event_DT;
	}
	/**
	 * Sets a new Description of the Event
	 * @param Description : New Description of Event
	 */
	void setDescription(String Description)
	{
		this.Description=Description;
	}
	/**
	 * Sets a new location of the Event
	 * @param Location : New Location of the Event
	 */
	void setLocation(String Location)
	{
		this.Location=Location;
	}
	/**
	 * Sets the date and time of the Event
	 * @param Event_DT: ZondedDateTime object of the new date and time to be set.
	 */
	void setEvent_DateTime(ZonedDateTime Event_DT)
	{
		this.Event_DateTime=Event_DT;
	}
	/**
	 * Sets the date and time of the Event. The String should be parsable to ZondedDateTime. If not, then previous value is retained.
	 * @param Event_DT : String containing Date and Time that is parsable to ZonedDateTime.
	 */
	void setEvent_DateTime(String Event_DT)
	{
		ZonedDateTime prev=this.Event_DateTime;
		try
		{
			this.Event_DateTime=ZonedDateTime.parse(Event_DT);
		}
		catch(DateTimeParseException e)
		{
			this.Event_DateTime=prev;
			e.getStackTrace();
		}
	}
	/**
	 * Gets the Description of the Event
	 * @return the String containing the description of the event.
	 */
	String getDescription()
	{
		return this.Description;
	}
	/**
	 * Gets the Name of the event 
	 * @return the String containing the name of the event.
	 */
	String getName()
	{
		return this.name;
	}
	/**
	 * Gets the Location of the event 
	 * @return the String containing the location of the event.
	 */
	String getLocation()
	{
		return this.Location;
	}
	/**
	 * Gets the ID of the event 
	 * @return the String containing the ID of the event.
	 */
	String getID()
	{
		return this.ID;
	}
	/**
	 * Gets the Date and Time of the Event
	 * @return the ZonedDateTime object containing the Date and Time of the Event.
	 */
	ZonedDateTime getEvent_DateTime()
	{
		return this.Event_DateTime;
	}
	/**
	 * Gets the Type of the Event
	 * @return enum Event_t containing the event Type
	 */
	Event_t getEventType()
	{
		return this.EventType;
	}
	/**
	 * Event object is used with HashMap so it requires to be overloaded.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;

	    Event ev = (Event) obj;
	    
		return this.ID.equals(ev.ID) && this.Event_DateTime.isEqual(ev.getEvent_DateTime())&&
				this.name.equals(ev.getName()) && this.Description.equals(ev.getDescription())&&
				this.EventType.equals(ev.getEventType()) &&this.Location.equals(ev.getLocation());
	}
	/**
	 * The correct bucket in HashMap is identified using hashCode(). So It needed to be overloaded.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(ID,Event_DateTime,name,Description,EventType,Location);
	}
}
