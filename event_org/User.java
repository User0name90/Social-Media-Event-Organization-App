package event_org;

/**
 * This class stores the basic information of the User. Both User name and ID are unique for every user.
 * Username can be changed but it still needs to be unique.
 */
class User {
	/** 
	 * Stores a user friends in an array. It was a part of initial development that is why an array without
	 * much research.
	 * @see java.util.ArrayList
	 */
	private User FriendList[];
	/** stores the frined count */
	private int FriendCount=0;
	/** Constant that helps in initializing the array of friend list and
	 * sets the limit of maximum numbers of friends
	 */
	public static final int MAXFRIENDS=200;
	/** It is somewhat a misnomer, it Stores the Username of the User*/
	private String Name;
	/**Stores the email address of the user */
	private String Emailaddress;
	/**Stores the password of the user */
	private String Password;
	/**Stores the password of the user. Since the ID of the User can't be changed it is declared final */
	final private String ID;
	 /**
     * Constructs a User with full details.
     *
     * @param ID Unique identifier for the user
     * @param Name Name of the user
     * @param Emailaddress Email address of the user
     * @param Password Password of the user
     */
	User(String ID,String Name,String Emailaddress,String Password)
	{
		this.Name=Name;
		this.Emailaddress=Emailaddress;
		this.Password=Password;
		this.ID=ID;
		FriendList=new User[MAXFRIENDS];
	}
	  /**
     * Constructs a User with minimal details (used for friend references).
     *
     * @param ID Unique identifier for the user
     * @param Name Name of the user
     */
	User(String ID,String Name) 
	{
		this.Name=Name;
		this.ID=ID;
	}
	 /**
     * Removes a friend from the friend list.
     * @param Friend The user object to be removed
     * @return true if the friend was removed, false otherwise
     */
	boolean removeFriend(User Friend)
	{
		if(Friend==null) return false;
		for(int i=0;i<FriendCount;i++)
		{
			if(FriendList[i]==null) continue;
			if(FriendList[i].ID.equals(Friend.ID))
			{
				for(int j=i;j<FriendCount-1;j++)
				{
					FriendList[j]=FriendList[j+1];
				}
				FriendList[--FriendCount]=null;
				return true;
			}
		}
		return false;
	}
	/**
	 * Overloaded method of addFriend()
     * Adds multiple friends to the friend list.
     *
     * @param Friend Array of users to be added
     * @return true if all friends were added successfully, false otherwise
     */
	boolean addFriend(User Friend[])
	{
		if(Friend==null) return false;
		if(Friend.length>User.MAXFRIENDS-this.FriendCount) return false;
		for(User u:Friend)
		{
			if(!addFriend(u)) return false;
		}
		return true;
	}
	 /**
     * Adds a single friend to the friend list.
     *
     * @param Friend The user to be added
     * @return true if added successfully, false otherwise
     */
	boolean addFriend(User Friend)
	{
		if(Friend==null) return false;
		
		if(FriendCount>=MAXFRIENDS)
			return false;
		
		FriendList[FriendCount++]=Friend;
		return true;
	}
    /**
     * Retrieves all friends in the friend list.
     *
     * @return Array of User objects representing friends
     */
	User[] getFriend()
	{
		User return_friends[];
		int indexes[]=new int[FriendCount];
		int count=0;
		User temp;
		for(int i=0;i<FriendList.length;i++)
		{
			temp=FriendList[i];
			if(temp==null) continue;
			indexes[count++]=i;
		}
		return_friends=new User[count];
		
		int j=0;
		for(int i=0;i<count;i++)
		{
			return_friends[j++]=FriendList[indexes[i]];
		}
		return return_friends;
	}
	 /**
     * Retrieves friends whose names contain a given substring.
     *
     * @param username_substring Substring to search in usernames
     * @return Array of matching User objects containing the substring in their IDs
     */
	User[] getFriendByName(String username_substring)
	{
		User return_friends[];
		int indexes[]=new int[FriendCount];
		int count=0;
		User temp;
		for(int i=0;i<FriendList.length;i++)
		{
			temp=FriendList[i];
			if(temp==null) continue;
			if(temp.getUsername().contains(username_substring))
			{
				indexes[count++]=i;
			}
		}
		return_friends=new User[count];
		
		int j=0;
		for(int i=0;i<count;i++)
		{
			return_friends[j++]=FriendList[indexes[i]];
		}
		return return_friends;
	}
	/**
     * Retrieves a friend by their ID.
     *
     * @param ID The ID to search for
     * @return Array containing the matching User, or empty array if not found
     */
	User[] getFriendByID(String ID)
	{
		User temp;
		for(int i=0;i<FriendCount;i++)
		{
			temp=FriendList[i];
			if(temp==null) continue;
			if(temp.getID().equalsIgnoreCase(ID))
			{
				return new User[]{temp};
			}
		}
		return new User[0];
	}
	
	 /**
     * Gets the user's ID.
     * @return User ID
     */
    String getID() {
        return this.ID;
    }

    /**
     * Gets the user's name.
     * @return Username
     */
    String getUsername() {
        return this.Name;
    }

    /**
     * Gets the user's email address.
     * @return Email address
     */
    String getEmailaddress() {
        return this.Emailaddress;
    }

    /**
     * Gets the user's password.
     * @return Password
     */
    String getPassword() {
        return this.Password;
    }

    /**
     * Gets the number of friends.
     * @return Friend count
     */
    int getFriendCount() {
        return FriendCount;
    }

    /**
     * Sets a new password.
     * @param Password New password
     */
    void setPassword(String Password) {
        this.Password = Password;
    }

    /**
     * Sets a new email address.
     * @param Emailaddress New email address
     */
    void setEmailaddress(String Emailaddress) {
        this.Emailaddress = Emailaddress;
    }

    /**
     * Sets a new username.
     * @param UserName New username
     */
    void setUsername(String UserName) {
        this.Name = UserName;
    }

    /**
     * Clears the friend list.
     */
    void clearFriendList() {
        FriendList = new User[MAXFRIENDS];
    }
}

