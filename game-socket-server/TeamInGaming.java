import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TeamInGaming {

	public static void main(String[] args) {

		ArrayList<TeamInGamingThread> chatters = new ArrayList<TeamInGamingThread>();

		ServerSocket serverSocket = null;
		Socket socket = null;
		int PORT = 5005;
		String Server_IP = "10.146.0.3";

		TeamInGamingThread chatter = null;
		
		try {

			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(Server_IP, PORT));

			while (true) {
				System.out.println("***********inGaming*************");
				socket = serverSocket.accept();
				System.out.println("유저 접속 아이디 ");
				
				chatter = new TeamInGamingThread(socket, chatters);
				chatter.checkID();
				chatters.add(chatter);
				chatter.start();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();

				}

			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

}

