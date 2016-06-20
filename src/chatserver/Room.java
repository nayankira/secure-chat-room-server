package chatserver;

/**
 * Class used by server to manage the rooms in the chat system 
 * 
 * @author Yan NA
 *
 */

import java.net.Socket;
import java.util.ArrayList;

public class Room 
{
	private String roomid;
	private String ownerid="";
//	private Connection owner;
	private ArrayList<Connection> member= new ArrayList<Connection>();
	private ArrayList<Connection> block= new ArrayList<Connection>();
	
	public Room (String roomid)
	{
		this.roomid = roomid;
		chatserver.rooms.add(this);
		
	}
	
	public Room (String roomid,Connection owner)
	{
		this.roomid = roomid;
//		this.owner=owner;
		chatserver.rooms.add(this);
		ownerid=owner.identity;
	}

	public ArrayList<Connection> getmembers()
	{
		return member;
	}
	
	public void joinmember(Connection member)
	{
		this.member.add(member);
	}

	public void kickmember(Connection member)
	{
		this.member.remove(member);
	}
	
	public void deletroom ()
	{
		chatserver.rooms.remove(this);
	}
	
	public String getid()
	{
		return roomid;
	}
	
	public int getcount()
	{
		return member.size();
	}
	
	public String getowner()
	{
		return ownerid;
	}
	
	public void setownerid(String newid)
	{
		this.ownerid=newid;
	}
	
	public void addblocklist(Connection user)
	{
		block.add(user);
		System.out.println(user.identity+" has been blocked by " + this.roomid);
	}
	
	public void removeblockid(Connection user)
	{
		block.remove(user);
		System.out.println(user.identity+" has been removed from the block list of " + this.roomid);
	}
	public ArrayList<Connection> getblocklist()
	{
		return block;
	}
}
