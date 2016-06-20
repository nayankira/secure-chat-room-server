package chatserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class used by server to parse the incoming JSON packages and response to them
 * 
 * @author Yan NA
 *
 */

public class Connection implements Runnable {
	public Socket socket;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private boolean connected=false;
	
	String identity;
	String password=null;
	boolean isRegistered=false;
	
	public String roomid;
	
	public Connection(Socket socket) {
		this.socket = socket;
		try {

			dis =new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			connected=true;

			identity = "guest"+ chatserver.findMinNum();   //Integer.toString(chatserver.clients.indexOf(socket));
			chatserver.connections.add(this);
			chatserver.joinroom("MainHall",this);
			initialmsg();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private void Sychronized(Connection connection) {
//		// TODO Auto-generated method stub
//		
//	}

	public String getroomid()
	{
		return roomid;
	}
	
	public void send (String s) throws Exception
	{
		
		dos.writeUTF(s);
	}

	public void sendtoall (String s)
	{
		for (Socket rsocket : chatserver.clients) 
		{
			DataOutputStream dosta;
			try {
				dosta = new DataOutputStream(rsocket.getOutputStream());
				dosta.writeUTF(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
	public void sendtoroom(String s)
	{
		for (Connection connection : chatserver.connections) 
		{
			DataOutputStream dostr = null;
			try {
				if(connection.roomid.equals(this.roomid))
				{
					dostr = new DataOutputStream(connection.socket.getOutputStream());
					dostr.writeUTF(s);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	public void sendtootherroom(String roomid, String content)
	{
		for (Connection connection : chatserver.connections) 
		{
			DataOutputStream dostr = null;
			try {
				if(connection.roomid.equals(roomid))
				{
					dostr = new DataOutputStream(connection.socket.getOutputStream());
					dostr.writeUTF(content);

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void initialmsg()
	{		
		try {
			send(Jsonstr.newidentityjson("", identity));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			sendtoroom(Jsonstr.changeroomjson("","MainHall",this.identity));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			send(chatserver.getjsonroomlist());
			send(Jsonstr.roomcontent(roomid));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
	}

	
	@Override
	public void run() {
			
			try 
			{
				while (connected) 
				{
					synchronized(this)
					{
					
					JSONObject object = null;
					JSONParser parser = new JSONParser();
					String msg = dis.readUTF();
					try {
						object = (JSONObject) parser.parse(msg);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					String type = (String) object.get("type");
					System.out.println(type + " from " + this.identity);
					System.out.println("The received JSON package is "+object);
					if (type.equals("message"))
					{
						String content = (String) object.get("content");
						System.out.println( chatserver.clients.indexOf(socket) + content);						
						sendtoroom(Jsonstr.messagejson(identity, content));
						
					}
					else if (type.equals("identitychange"))
					{
						String formerid=this.identity;
						String newidentity=(String) object.get("identity");
						chatserver.changeIdentity(this, newidentity, formerid);
						
//						if(!chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
//						{
//							this.identity = newidentity;
//							for (Room theroom:chatserver.rooms)
//							{
//								if (theroom.getowner().equals(formerid))
//								{
//									theroom.setownerid(this.identity);
//								}
//							}
//							System.out.println(formerid+" is now "+identity);
//							sendtoall(Jsonstr.newidentityjson(formerid, identity));	
//						}
//						else if (chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
//						{
//							try {
//								send(Jsonstr.messagejson("", "this identity has been used"));
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}	
//						}
										
					}
					else if (type.equals("list"))
					{
						try {
							send(chatserver.getjsonroomlist());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (type.equals("createroom"))
					{
						if (!chatserver.roomexist((String)object.get("roomid"))
								&&((String)object.get("roomid")).matches("[a-zA-Z][a-zA-Z0-9]{2,31}"))
						{
							Room newroom = new Room((String)object.get("roomid"),this);
							try {
								send(chatserver.getjsonroomlist());
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						else if(chatserver.roomexist((String)object.get("roomid"))
								&&((String)object.get("roomid")).matches("[a-zA-Z][a-zA-Z0-9]{2,31}"))
						{
							try {
								send(Jsonstr.messagejson("system", "Room "+ (String)object.get("roomid")+ " is already in use"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
												
					}
					else if (type.equals("join"))
					{
						String newroom=(String) object.get("roomid");
						if(chatserver.roomexist(newroom)&&!chatserver.isblocked(newroom,this))
						{
						
							String formerroom=this.roomid;
							if(formerroom.equals(newroom))
							{
								try {
									send(Jsonstr.changeroomjson(formerroom,newroom,this.identity));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							else
							{
							chatserver.leaveroom(this);
							chatserver.joinroom(newroom,this);			
								if(newroom.equals("MainHall"))
								{
									try {
										sendtootherroom(formerroom,Jsonstr.changeroomjson(formerroom,newroom,this.identity));
										sendtoroom(Jsonstr.changeroomjson(formerroom,newroom,this.identity));	
										send(Jsonstr.roomcontent(newroom));
										send(chatserver.getjsonroomlist());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								else
								{
								sendtootherroom(formerroom,Jsonstr.changeroomjson(formerroom,newroom,this.identity));
								sendtoroom(Jsonstr.changeroomjson(formerroom,newroom,this.identity));	
								}
							}
							
						}
						else if (chatserver.roomexist(newroom)&&chatserver.isblocked(newroom,this))
						{
							try {
								send(Jsonstr.messagejson ("", "you are blocked now"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							
							try {
								send(Jsonstr.messagejson ("", "The requested room is invalid or non existent"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else if (type.equals("who"))
					{
						String roomid=(String)object.get("roomid");
						try {
							send(Jsonstr.roomcontent(roomid));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (type.equals("delete"))
					{
						String roomid=(String)object.get("roomid");
						if (chatserver.roomexist(roomid))
						{
							chatserver.deleteroom(roomid,this.identity);
							try {
								send(chatserver.getjsonroomlist());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							try {
								send(Jsonstr.messagejson("", "invalid operarion"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}
					else if (type.equals("signup"))
					{
						String formerid=this.identity;
						String newidentity=(String) object.get("newidentity");
						String newpassword=(String) object.get("password");
						
						chatserver.userSignup(this, newidentity, newpassword, formerid);
						
//						if(!chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
//						{
////							this.identity = newidentity;
////							for (Room theroom:chatserver.rooms)
////							{
////								if (theroom.getowner().equals(formerid))
////								{
////									theroom.setownerid(this.identity);
////								}
////							}
////							this.password=(String)object.get("password");
//							RegisteredUser.registering(newidentity, newpassword);
////							isRegistered=true;
//							System.out.println(formerid+" is now "+identity+ " whose password is " + this.password);
////							sendtoall(Jsonstr.newidentityjson(formerid, identity));	
//							send(Jsonstr.messagejson("", "you have signed up as a user, you can log in by #login."));
//						}
//						else if (chatserver.isclientexist(newidentity)&&newidentity.matches("[a-zA-Z][a-zA-Z0-9]{2,15}"))
//						{
//							try {
//								send(Jsonstr.messagejson("", "this identity has been used"));
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}	
//						}							
					}
					else if (type.equals("login"))
					{
						String formerid=this.identity;
						String theidentity=(String) object.get("identity");
						String thepassword=(String) object.get("password");
						
						if(RegisteredUser.loginValidation(theidentity, thepassword))
						{
							if(this.isRegistered==false)
							{
								chatserver.cancelownership(this);
								chatserver.userLogin(this, theidentity, thepassword, formerid);
							}
							else
							{
								chatserver.userLogoff(formerid);
								chatserver.userLogin(this, theidentity, thepassword, formerid);
							}
						}
						else
						{
							try {
								this.send(Jsonstr.messagejson("", "invalid username or password"));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}					
					}
					else if (type.equals("kick"))
					{
						String roomid=(String)object.get("roomid");
						Long time = (Long) object.get("time");
						String kickedId= (String)object.get("identity");
						Connection kickeduser = null;
						for (Connection user: chatserver.connections)
						{
							if (user.identity.equals(kickedId))
							{
								 kickeduser=user;
							}
						}
						
						if(chatserver.roomexist(roomid))
						{
							for (Room room:chatserver.rooms)
							{   
								if (room.getmembers().contains(kickeduser))
								{
									String formerroom=room.getid();
									chatserver.leaveroom(kickeduser);
									chatserver.joinroom("MainHall",kickeduser);
									kickeduser.sendtootherroom(formerroom,Jsonstr.changeroomjson(formerroom,"MainHall",kickeduser.identity));
									kickeduser.sendtoroom(Jsonstr.changeroomjson(formerroom,"MainHall",kickeduser.identity));									
									try {
										kickeduser.send(Jsonstr.roomcontent("MainHall"));
										kickeduser.send(chatserver.getjsonroomlist());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									room.addblocklist(kickeduser);									
									Delaytime dt = new Delaytime(kickeduser, room, time);
									Thread dtt=new Thread(dt);
									dtt.start();									
								}
							}
						}
						else
						{}
						
					}
					else if (type.equals("quit"))
					{
						connected= false; 
						break;
					}
					else;
					}

				}
				
				sendtoroom(Jsonstr.changeroomjson(roomid,"", this.identity));
				Room crroom=chatserver.roomfinder(roomid);
				crroom.kickmember(this);
				if(this.isRegistered==false)
				{
				   chatserver.cancelownership(this);
				}
				else
				{
					chatserver.userLogoff(this.identity);
				}
				
				chatserver.clients.remove(socket);
				chatserver.connections.remove(this);
				dis.close();
				dos.close();
				socket.close();
							
			} catch (EOFException e) {
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				System.out.println("Client disconnected.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(this.identity+" has a abrupt disconnection.");
				
				try {
					sendtoroom(Jsonstr.changeroomjson(roomid,"", this.identity));
					Room crroom=chatserver.roomfinder(roomid);
					crroom.kickmember(this);
					chatserver.clients.remove(socket);
					chatserver.connections.remove(this);
					if (this.isRegistered==false)
					{
						chatserver.cancelownership(this);
					}
					else
					{
						chatserver.userLogoff(this.identity);
					}
					dis.close();
					dos.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	
		//A thread finishes if run method finishes 
	}



}
