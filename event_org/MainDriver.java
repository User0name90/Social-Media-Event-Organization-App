package event_org;
/**
 * @author RISHIT KHANNA 2024A3PS0343G
 * ACCkNOWLEDGEMENT
 * I would like to thanks my friends for encouraging, supporting 
 * and testing my program that helped me find various crashes and bugs. I had the feeling of giving up on the
 * project but due their constant feedback, I was able to move forward and made it working.
 */
/**
 * This the main driver class that initializes or creates the files
 * in the root directory if they are missing. It prompts theS user with three
 * options on startup i.e. Exit, Login and Sign up.
 */
public class MainDriver {
	public static void main(String args[])
	{
		if(!FileIO.InitializeFiles()) UI.PrintError("File Initilaization failed");
		else UI.PrintSuccess("Program Started Successfully\n");
		
		System.out.println("Welcome to the Social Media Event Organizer\n");
		while(true){
			
			int choice=UI.prompt("Exit","Login","Signup");
			if(choice == 0)
			{
				System.out.println("Programm exited successfully");
				return;
			}
			else if (choice == 2)
			{
				if(!UI.Signup()) continue;
			}
			else if(choice==1)
			{
				if(!UI.Login()) continue;
				UI.Mainmenu();
			}
			
		}
		
	}
}
