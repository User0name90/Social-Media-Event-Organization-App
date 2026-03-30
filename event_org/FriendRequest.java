package event_org;

import java.util.Objects;

enum RequestResponse {ACCEPTED,REJECTED,PENDING,DELETED}

class FriendRequest {
	final private String From_ID;
	final private String To_ID;
	private RequestResponse Response=RequestResponse.PENDING;
	FriendRequest(String From_ID,String To_ID,RequestResponse r)
	{
		this.From_ID=From_ID;
		this.To_ID=To_ID;
		this.Response=r;
	}
	FriendRequest(String From_ID,String To_ID)
	{
		this.From_ID=From_ID;
		this.To_ID=To_ID;
	}
	String getFrom()
	{
		return From_ID;
	}
	String getTo()
	{
		return To_ID;
	}
	RequestResponse getResponse()
	{
		return this.Response;
	}
	void SetResponse (RequestResponse r)
	{
		this.Response=r;
	}
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;

	    FriendRequest fr = (FriendRequest) obj;
	    
		return (fr.getFrom().equals(From_ID))&&(fr.getTo().equals(To_ID))&&(fr.getResponse().equals(Response));
	}
	@Override
    public int hashCode() {
        return Objects.hash(From_ID,To_ID,Response);
    }
}
