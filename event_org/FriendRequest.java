package event_org;

import java.util.Objects;

/**
 * {@summary enum RequestResponse contains the status of the sent friend request. It has 4 different values}
 */
enum RequestResponse {ACCEPTED,REJECTED,PENDING,DELETED}
/**
 * Contains information regarding the The sender and receiver of friend request along with the response
 */
class FriendRequest {
	final private String From_ID;
	final private String To_ID;
	private RequestResponse Response=RequestResponse.PENDING;
	/**
	 * This constructor is specifically used while responding to friend request
	 * @param From_ID the 10 character ID of the sender
	 * @param To_ID the 10 character ID of the receiver
	 * @param r is the response of the receiver
	 */
	FriendRequest(String From_ID,String To_ID,RequestResponse r)
	{
		this.From_ID=From_ID;
		this.To_ID=To_ID;
		this.Response=r;
	}
	/**
	 * This constructor is specifically used while generating friend request as it does not initialize the Response
	 * @param From_ID The 10 character ID of the sender
	 * @param To_ID the 10 character ID of the receiver
	 */
	FriendRequest(String From_ID,String To_ID)
	{
		this.From_ID=From_ID;
		this.To_ID=To_ID;
	}
	/**
	 * Gets the ID of the sender
	 * @return 10 character length ID of the sender
	 */
	String getFrom()
	{
		return From_ID;
	}
	/**
	 * Gets the ID of the receiver
	 * @return String containing 10 character length ID of the receiver
	 */
	String getTo()
	{
		return To_ID;
	}
	/**
	 * Gets the response of the friend Request
	 * @return an enum RequestResponse value containing the Response of the receiver
	 */
	RequestResponse getResponse()
	{
		return this.Response;
	}
	/**
	 * Sets the response of the friend Request
	 * @param r in an enum RequestResponse value containing the Response of the receiver
	 */
	void SetResponse (RequestResponse r)
	{
		this.Response=r;
	}
	/**
	 * FriendRequest object is used with HashMap so it requires to be overloaded.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;

	    FriendRequest fr = (FriendRequest) obj;
	    
		return (fr.getFrom().equals(From_ID))&&(fr.getTo().equals(To_ID))&&(fr.getResponse().equals(Response));
	}
	/**
	 * The correct bucket in HashMap is identified using hashCode(). So It needed to be overloaded.
	 */
	@Override
    public int hashCode() {
        return Objects.hash(From_ID,To_ID,Response);
    }
}
