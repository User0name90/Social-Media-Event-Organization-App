package event_org;
import java.time.ZonedDateTime;
import java.util.Objects;

enum Event_t {PRIVATE,PUBLIC}
class Event {
	
	final private String ID;
	final private String name;
	private String Description;
	private String Location;
	private Event_t EventType;
	private ZonedDateTime Event_DateTime;
	
	Event(String ID,String name,String description,String Location,Event_t EventType,ZonedDateTime Event_DT)
	{
		this.ID=ID;
		this.name=name;
		this.Description=description;
		this.Location=Location;
		this.EventType=EventType;
		this.Event_DateTime=Event_DT;
	}
	void setDescription(String Description)
	{
		this.Description=Description;
	}
	void setLocation(String Location)
	{
		this.Location=Location;
	}
	void setEvent_DateTime(ZonedDateTime Event_DT)
	{
		this.Event_DateTime=Event_DT;
	}
	String getDescription()
	{
		return this.Description;
	}
	String getName()
	{
		return this.name;
	}
	String getLocation()
	{
		return this.Location;
	}
	String getID()
	{
		return this.ID;
	}
	ZonedDateTime getEvent_DateTime()
	{
		return this.Event_DateTime;
	}
	Event_t getEventType()
	{
		return this.EventType;
	}
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
	@Override
	public int hashCode()
	{
		return Objects.hash(ID,Event_DateTime,name,Description,EventType,Location);
	}
}
