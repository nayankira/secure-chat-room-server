package chatserver;

/**
 * Class used by server to manage the registered users
 * 
 * @author Yan NA
 *
 */
public class RegisteredUser {
	private String username;
	private String password;
	boolean hasLogin=false;
	
	public RegisteredUser(String newusername, String newpassword)
	{
		this.username=newusername;
		this.password=newpassword;		
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String newpassword)
	{
		this.password=newpassword;
	}
	
	public void setUsername(String newusername)
	{
		this.username=newusername;
	}
	
	public synchronized static void registering(String username, String password)
	{
		RegisteredUser newuser= new RegisteredUser(username,password);
		chatserver.registereduser.add(newuser);
	}
	
	public synchronized static boolean isRegisteredUserExist(String username)
	{
		boolean isexist=false;
		
		for (RegisteredUser ru:chatserver.registereduser)
		{
			if (ru.getUsername().equals(username))
			{
				isexist=true;
			}
		}
		return isexist;
	}
		
	public synchronized static boolean loginValidation (String username, String password)
	{
		boolean isValidated=false;
		for (RegisteredUser ru:chatserver.registereduser)
		{
			if (ru.getUsername().equals(username)&&ru.getPassword().equals(password))
			{
				isValidated=true;
			}
		}
		return isValidated;
	}
}
