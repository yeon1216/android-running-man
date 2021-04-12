import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TeamInGamingThread extends Thread {

	
	//소켓
	Socket socket;
	//수신
	BufferedReader br;
	//발신
	PrintWriter pw;
	
	boolean startsign;
	boolean ismatching;

	//메인쓰레드에서 받아온 소켓연결한 모든 클라이언트 
	ArrayList<TeamInGamingThread> chatters;
	
	//상대방 소켓 담을 공간 
	TeamInGamingThread enermySocket;
	
	//내 아이디 
	String id;
	
	
	//상대방 아이디 
	String enermy1;
	String enermy2;
	String enermy3;
	
	
	boolean GameStart = true;

	
	//메인스레드에서 TeamInGamingThread 를 생성한테 br 과 pw 를 생성한다.
	public TeamInGamingThread(Socket socket, ArrayList<TeamInGamingThread> chatters){
		   this.socket = socket;
		   this.chatters = chatters;
		 
		  
		  try{
		   br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		   pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
		  }catch(IOException e){
		   System.out.println(e.getMessage());
		  }
		 }

	
	public void checkID() {
		try {
			String CheckId = br.readLine();
			
			String[] tmpMessage = CheckId.split(":");
			if(tmpMessage[0].contentEquals("join")) {
				id = tmpMessage[1];
				enermy1 = tmpMessage[2];
				enermy2 = tmpMessage[3];
				enermy3 = tmpMessage[4];
			}
			
			
			System.out.println("접속자 id : "+id +
					" , 상대방 1 : "+enermy1+
					" , 상대방 2 : "+enermy2+ 
					" , 상대방 3 : "+ enermy3);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("login()처리에서 예외 발생.....");

		}
	}

	
	
	public void run() {

		System.out.println("run 시작");

//		try {
//			while (GameStart) {
//				for (int i = 0; i < chatters.size(); i++) {
//					if (chatters.get(i).id.equals(enermy)) {
//						enermySocket = chatters.get(i);
//						System.out.println(id + " 찾았다");
//						GameStart = false;
//					}
//					Thread.sleep(1000);
//					System.out.println(id + " : " + chatters.size());
//				}
//
//			}
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}

		
		
		try {
//			this.sendMessage("GameStart"); // 시작 메세지 전송
			String[] tmpMessage = null;
			String message="";
			while (true) { // bye를 보낼경우 소켓 종료
				System.out.println("게임 시작 후 데이터 교환 ");
				System.out.println("요청 정보 " + id + ":" + message);
				message = br.readLine(); // 메시지 올떄가지 대기
				
				tmpMessage = message.split(":");
				
				if(tmpMessage[0].contentEquals("event1")) {
					//나한테 메시지를 보낸다 
					this.sendMessage(message);
					//enermy1에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy1)) {
							//chatters 리스트에 아이디가 a라는 유저를 찾고 그 유저의 소켓을 enermySocket에 담는다 
							// enermySocket에 담은 객체의 sendMEssage를 통해 메시지를 전송한다 
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
					//enermy2에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy2)) {
							//chatters 리스트에 아이디가 a라는 유저를 찾고 그 유저의 소켓을 enermySocket에 담는다 
							// enermySocket에 담은 객체의 sendMEssage를 통해 메시지를 전송한다 
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
					//enermy3에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy3)) {
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
					
				}else if(tmpMessage[0].contentEquals("event2")) {
					//나한테 메시지를 보낸다 
					this.sendMessage(message);
					//enermy1에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy1)) {
							//chatters 리스트에 아이디가 a라는 유저를 찾고 그 유저의 소켓을 enermySocket에 담는다 
							// enermySocket에 담은 객체의 sendMEssage를 통해 메시지를 전송한다 
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
					//enermy2에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy2)) {
							//chatters 리스트에 아이디가 a라는 유저를 찾고 그 유저의 소켓을 enermySocket에 담는다 
							// enermySocket에 담은 객체의 sendMEssage를 통해 메시지를 전송한다 
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
					//enermy3에게  메시지를 보낸다
					for(int i=0;i<chatters.size();i++) {
						if(chatters.get(i).id.contentEquals(enermy3)) {
							enermySocket=chatters.get(i);
							enermySocket.sendMessage(message);							
						}
					}
				}else if(tmpMessage[0].contentEquals("event3")) {
					//event3:bye라는 메시지를 전송하면 자신의 서버연결을 종료한다
					break;
				}
				
		

			}

		} catch (IOException e) {

			System.out.println(e.getMessage());
			System.out.println("메세지를 수신하여 송신중 예외 발생....");
		} finally {
			//while문을 빠져나오면 무조건 호출되는 부분
			System.out.println("연결을 닫고 쓰레드 종료....");
			close();
			
		}
	}

	
	
	//나에게 메시지 보내기 
	//this.sendMessage 는 나에게 메시지를 전송한다
	//enermySocket.sendMessage는 지정한 객체가 자기자신에게 메시지를 전송한다 
	void sendMessage(String message) {
		try {
			pw.println(message);
			pw.flush();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("sendMessage()에서 예외 발생....");
		}
		for (int i = 0; i < chatters.size(); i++) {
			System.out.println("id " + chatters.get(i).id);
		}

	}

	public void close() {
		try {
			br.close();
			pw.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("close()..도중 예외 발생!");
		}

	}

}

