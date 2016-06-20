package chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Class used as server to connect to the clients, and define some methods that affects all the threads
 * 
 * @author Yan NA
 *
 */

public class chatserver 
{
	static ServerSocket serverSocket = null;
	public static ArrayList<Socket> clients = new ArrayList<Socket>();
	public static ArrayList<Room> rooms = new ArrayList<Room>();
	public static ArrayList<Connection> connections = new ArrayList<Connection>();
	public static ArrayList<RegisteredUser> registereduser=new ArrayList<RegisteredUser>();
	
	public static void main(String[] args) throws IOException 
	{
		try {
			CmdLineParser parser;
			CommandLineValues values = new CommandLineValues();
			parser = new CmdLineParser(values);
			// parse the command line options with the args4j library

		    try {
				parser.parseArgument(args);
			} catch (CmdLineException e) {
				System.err.println(e.getMessage());
				parser.printUsage(System.err);
				System.exit(-1);			
			}
		    
			//Server is listening on port 4444
//			serverSocket = new ServerSocket(values.getPort());
//			System.out.println("Server is listening...");
//			int i = 0;
//			Room mainhall=new Room ("MainHall");

//			while (true) {
//				//Server waits for a new connection
//				Socket socket = serverSocket.accept();
//				// Java creates new socket object for each connection.
//				i++;
//				System.out.println("Client Connected..." + i);
//				clients.add(socket);
//				// A new thread is created per client
//				Connection cn = new Connection(socket);
//				Thread client = new Thread(cn);
//				
//				// It starts running the thread by calling run() method	
//				client.start();
//
//				
//			}
			
			// You can hardcode the values of the JVM variables as follows:  
			System.setProperty("javax.net.ssl.keyStore","zn.keystore");		
//			System.setProperty("javax.net.ssl.keyStore","C:/Program Files/Java/jdk1.8.0_51/bin/zn.keystore");
			System.setProperty("javax.net.ssl.keyStorePassword","123456");
			
			// Create SSL server socket factory, which creates SSLServerSocket instances
			ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
			
			// Use the factory to instantiate SSLServerSocket
			try(ServerSocket server = factory.createServerSocket(values.getPort())) 
			{
				System.out.println("Server is listening at port " + values.getPort());
				int i = 0;
				Room mainhall=new Room ("MainHall");
				
				while (true)
				{
					Socket socket = server.accept();					
					i++;
					System.out.println("Client Connected..." + i);
					clients.add(socket);
					// A new thread is created per client
					Connection cn = new Connection(socket);
					Thread client = new Thread(cn);
					
					// It starts running the thread by calling run() method	
					client.start();
				}
								
//				DataInputStream in = new DataInputStream(socket.getInputStream());
//				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//				
//				String msg = in.readUTF();
//				System.out.println("Received: " + msg);
//				out.writeUTF("Received: " + msg);
//				out.flush();
				
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
		
	}

	
	public synchronized static void deleteroom(String theroom,String id)
	{
		Room crroom = chatserver.roomfinder(theroom);

		if (crroom.getid().equals(theroom)&&crroom.getowner().equals(id))
		{
			crroom.deletroom();
			for (Connection member :crroom.getmembers())
			{
				chatserver.leaveroom(member);
				chatserver.joinroom("MainHall",member);
				member.sendtoroom(Jsonstr.changeroomjson(theroom,"MainHall",member.identity));
				
				try {
					member.send(Jsonstr.roomcontent("MainHall"));
					member.send(chatserver.getjsonroomlist());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
	}
	
	public synchronized static String getjsonroomlist()
	{
		JSONObject objectrl = new JSONObject();
		objectrl.put("type","roomlist");
		JSONArray list = new JSONArray();
		
		for (Room cr:chatserver.rooms)
		{
			String roomid=cr.getid();
			int count=cr.getcount(); 

			JSONObject room = new JSONObject();
			room.put("roomid",roomid);
			room.put("count",count);
			list.add(room);
			
		}
		
		objectrl.put("rooms",list);
		
		return objectrl.toJSONString();

	}
	
	public synchronized static boolean roomexist(String roomid)
	{
		boolean exist=false;
		for (Room room: chatserver.rooms)
		{
			if (room.getid().equals(roomid))
			{
				exist=true;
			}
		}
		return exist;
	}
	
	public synchronized static Room roomfinder(String roomid)
	{
		Room foundroom = null;
		for (Room room: chatserver.rooms)
		{
			if (room.getid().equals(roomid))
			{
				foundroom=room;
			}
		}
		return foundroom;
	}
	
	public synchronized static int findMinNum ()
	{
		int i=0;
		try
		{
			for (Connection client: chatserver.connections)
			{
				if (client.identity.equals("guest"+i))
				{
					i++;
				}
			}
			return i;
		}
		catch (NullPointerException ne)
		{
			return 0;
		}

	}
	
	//check whether the particular userid is currently in use
	public synchronized static boolean isclientexist (String newclientid)
	{
		boolean exist=false;
		for (Connection client: chatserver.connections)
		{
			if (client.identity.equals(newclientid))
			{
				exist=true;
			}
		}
		
		for (RegisteredUser ru:chatserver.registereduser)
		{
			if (ru.getUsername().equals(newclientid))
			{
				exist=true;
			}
		}
		
		return exist;	
	}
	
	public synchronized static boolean isblocked(String roomid, Connection user)
	{
		boolean isblocked=false;
		Room theroom=chatserver.roomfinder(roomid);
		if(theroom.getblocklist().contains(user))
		{
			isblocked=true;
		}
		return isblocked;
	}
	
	public synchronized static void joinroom(String newroom, Connection user)
	{
		for (Room crroom : chatserver.rooms) 
		{
			if (crroom.getid().equals(newroom))
			{
				user.roomid=newroom;                 //2 steps all done or none
//				crroom.joinmember(identity);   //2 steps all done or none
				crroom.joinmember(user);
				chatserver.clearemptyroom();
			}
		}
	}

	
	public synchronized static void leaveroom(Connection user)
	{
		for (Room crroom : chatserver.rooms) 
		{
			if(crroom.getid().equals(user.roomid))
			{
				user.roomid="";
				crroom.kickmember(user);
			}
		}
	}
	
	public synchronized static void cancelownership(Connection user)
	{   
		System.out.println("try to cancel all the ownership of " + user.identity);
		for (Room crroom: chatserver.rooms)
		{
			System.out.println("room "+ crroom.getid());

			if (crroom.getowner().equals(user.identity))
			{
				crroom.setownerid("");
				System.out.println("room "+ crroom.getid() + " has been set as ownerless");
			}
			else
				continue;
		}
		
		java.util.Iterator<Room> itr=chatserver.rooms.iterator();
		int c=chatserver.rooms.size();
		for (int i=0;i<c;i++)
		{
			if (chatserver.rooms.get(i).getmembers().size()==0
					&&!chatserver.rooms.get(i).getid().equals("MainHall")
					&&chatserver.rooms.get(i).getowner().equals(""))
			{
				chatserver.rooms.remove(i);
				i--;
				c--;
			}
		}
		System.out.println("All the room owned by "+user.identity+" has been deleted");
	}

	public synchronized static void clearemptyroom() 
	{
		// TODO Auto-generated method stub
		java.util.Iterator<Room> itr=chatserver.rooms.iterator();
		int c=chatserver.rooms.size();
		for (int i=0;i<c;i++)
		{
			if (chatserver.rooms.get(i).getmembers().size()==0
					&&!chatserver.rooms.get(i).getid().equals("MainHall")
					&&chatserver.rooms.get(i).getowner().equals(""))
			{
				chatserver.rooms.remove(i);
				i--;
				c--;
			}
		}
		System.out.println("All the empry room have been deleted");
	}
	
	public synchronized static void changeIdentity(Connection cn,String newidentity, String formerid)
	{
		if(!chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
		{
			cn.identity = newidentity;
			for (Room theroom:chatserver.rooms)
			{
				if (theroom.getowner().equals(formerid))
				{
					theroom.setownerid(cn.identity);
				}
			}
			for (RegisteredUser ru:chatserver.registereduser)
			{
				if (ru.getUsername().equals(formerid))
				{
					ru.setUsername(newidentity);
				}
			}
			System.out.println(formerid+" is now "+cn.identity);
			cn.sendtoall(Jsonstr.newidentityjson(formerid, cn.identity));	
			
		}
		else if (chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
		{
			try {
				cn.send(Jsonstr.messagejson("", "this identity has been used"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public synchronized static void userSignup (Connection cn,String newIdentity,String newPassword,String formerId)
	{
		if(!chatserver.isclientexist(newIdentity)&&newIdentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
		{
//			this.identity = newidentity;
//			for (Room theroom:chatserver.rooms)
//			{
//				if (theroom.getowner().equals(formerid))
//				{
//					theroom.setownerid(this.identity);
//				}
//			}
//			this.password=(String)object.get("password");
			RegisteredUser.registering(newIdentity, newPassword);
//			isRegistered=true;
			System.out.println(newIdentity+" has signed as new registered user name"+ " whose password is " + newPassword);
//			sendtoall(Jsonstr.newidentityjson(formerid, identity));	
			try {
				cn.send(Jsonstr.messagejson("", "you have signed up as a user, you can log in by #login."));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (chatserver.isclientexist(newIdentity)&&newIdentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
		{
			try {
				cn.send(Jsonstr.messagejson("", "this identity has been used"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public synchronized static void userLogin (Connection cn,String theIdentity,String thePassword,String formerId)
	{
		for (RegisteredUser ru:chatserver.registereduser)
		{
			if (ru.getUsername().equals(theIdentity)&&ru.getPassword().equals(thePassword))
			{
				if(ru.hasLogin==true)
				{
					   try {
						 
						cn.send(Jsonstr.messagejson("", "this user has logined."));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				   
				}
				else
				{
					cn.identity = theIdentity;
					cn.password = thePassword;
//					for (Room theroom:chatserver.rooms)
//					{
//						if (theroom.getowner().equals(formerId))
//						{
//							theroom.setownerid(cn.identity);
//						}
//					}
					cn.isRegistered=true;					
					ru.hasLogin=true;
					System.out.println(cn.identity+" has logined, his password is " + cn.password);
					cn.sendtoall(Jsonstr.newidentityjson(formerId, cn.identity));	
					try {
						cn.send(Jsonstr.messagejson("", "you have logined as "+ cn.identity));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				break;
			}
//			else
//			{
//				try {
//					cn.send(Jsonstr.messagejson("", "invalid username or password"));
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			
		}
	}
	
	public synchronized static void userLogoff (String theidentity)
	{
		for (RegisteredUser ru:chatserver.registereduser)
		{
			if (ru.getUsername().equals(theidentity))
			{
				ru.hasLogin=false;
			}
		}
	}		
}
