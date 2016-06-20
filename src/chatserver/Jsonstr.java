package chatserver;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Class is used by server to create json format package
 * 
 * @author Yan NA
 *
 */

public class Jsonstr 
{
	public synchronized static String changeroomjson(String former, String current,
			String identity) 
	{
		JSONObject objectr = new JSONObject(); // roomid
		objectr.put("type","roomchange");
		objectr.put("identity",identity);
		objectr.put("former",former);
		objectr.put("roomid", current);
		return (String)objectr.toJSONString();
		// TODO Auto-generated method stub

	}
	
	public synchronized static String messagejson (String sender, String content)
	{
		JSONObject objmsg = new JSONObject();
		objmsg.put("type","message");
		objmsg.put("identity", sender);						
		objmsg.put("content", content);
		return (String)objmsg.toJSONString();
	}

	public synchronized static String newidentityjson(String former,String current)
	{
		JSONObject objni = new JSONObject();
		objni.put("type","newidentity");
		objni.put("former",former);
		objni.put("identity",current);
		return (String)objni.toJSONString();
	}
	
	public synchronized static String roomcontent(String roomid)
	{
		JSONObject objectrc = new JSONObject();
		objectrc.put("type","roomcontents");
		objectrc.put("roomid",roomid);
		
		Room theroom = null;
		for (Room cr:chatserver.rooms)
		{
			if (cr.getid().equals(roomid))
			{
				theroom=cr;
			}
		}
	
		ArrayList<Connection> members;
		members=theroom.getmembers();
		JSONArray list1 = new JSONArray();
		
		for (Connection mem:members)
		{
			list1.add(mem.identity);
		}
		objectrc.put("identities",list1);		
		
		if (roomid.equals("MainHall"))
		{
		  objectrc.put("owner","");		
		}
		else
		{
		  objectrc.put("owner",theroom.getowner());				
		}
		
		return (String)objectrc.toJSONString();
	}
}

