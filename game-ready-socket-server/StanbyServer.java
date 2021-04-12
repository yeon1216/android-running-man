
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * ChatServer 클래스
 */
public class StanbyServer {

    public static void main(String[] args) {

    	System.out.println("[서버 시작]");

        try {

            ServerSocket server = new ServerSocket(5050); // ServerSocket 생성
            HashMap<String, Object> hm = new HashMap<String, Object>(); // HashMap 생성

            /**
             * 클라이언트 연결 대기
             */
            while(true) {
                Socket sock = server.accept(); // 클라이언트 접속
                new ChatThread(sock, hm).start(); // 소켓과 해쉬맵으로 챗 쓰레드를 생성
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    } // main() 메소드

} // ChatServer 클래스



/**
 * ChatThread 클래스
 */
class ChatThread extends Thread{
	private String TAG = "yeon["+getClass().getSimpleName().toString()+"]"; // 로그를 위한 태그

    private Socket sock;
    private String member_id;
    private BufferedReader br;
    private PrintWriter pw;
    private HashMap<String, Object> hm;
    private boolean quit_check = false;

    /**
     * 생성자
     * @param sock 소켓
     * @param hm 해쉬맵
     */
    public ChatThread(Socket sock, HashMap<String,Object> hm) {
        this.sock = sock;
        this.hm = hm;
    } // 생성자

    /**
     * run() 메소드
     */
    public void run() {
        try {

        	/**
        	 * PrintWriter, BufferedReader 생성
        	 */
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));

            /**
             * while문을 돌려 계속 수신 대기
             */
            while(true){

            	String request = br.readLine();

              if(request != null){
                  String[] tokens = request.split("¡"); //  request를 통해 tokens 배열 얻기

                 /**
                  * token을 통해 각종 메소드 실행
                  */
                  if("join".equals(tokens[0])) {
                    doJoin(tokens[1],pw); // 게임방 참여 메소드
                  }
                  else if("ready".equals(tokens[0])){
                	  doReady(tokens[1],pw);
                  }
                  else if("message".equals(tokens[0])) {
                    doMessage(tokens[1]); // 메시지 보내기 메소드
                  }
                  else if("quit".equals(tokens[0])) {
                    doQuit(tokens[1],pw); // 게임방 나감 메소드
                    quit_check=true;
                  }

              }else{
                consoleLog(TAG, "request null 된 멤버 번호: "+this.member_id);
                break;
              }


            } // end while

        }catch(SocketException se){
        	if(!quit_check){
        		consoleLog(TAG, "SocketException 된 멤버 번호: "+this.member_id);
        		removeWriter(pw);
//        		doQuit(pw);
        	}
        }catch (IOException e) {
        	 consoleLog(TAG, "IOException 에러 생긴 멤버 번호: "+this.member_id);
        }catch (Exception e){
          consoleLog(TAG, "Exception 생긴 멤버 번호: "+this.member_id);
          consoleLog(TAG,"e: "+e.toString());
        }finally {

            try {
                if(sock != null) {
                    sock.close();
                }
                removeWriter((PrintWriter) hm.get(this.member_id));
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    } // run() 메소드

    /**
     * 게임방 입장 메소드
     */
    private void doJoin(String data, PrintWriter pw) {
        synchronized (hm) {

	        String[] data_arr = data.split("ㅣ");
	        this.member_id = data_arr[0];
	        addWriter(pw); // 채팅방에 클라이언트 추가 메소드 (writer pool에 저장)
	        String[] game_join_user_arr = data_arr[1].split(",");

	        Set<String> set = hm.keySet();
	        Iterator<?> iterator = set.iterator();

	        /**
	         * member_id와 printwriter를 가지고 있는 hashmap을 조회하여
	         */
	        while(iterator.hasNext()) {
	           String key = (String)iterator.next();
	           for (int i = 0; i < game_join_user_arr.length; i++) {
	        	   if(key.equals(game_join_user_arr[i])){
	        		   if(!member_id.equals(game_join_user_arr[i])){
	        			   PrintWriter temp_pw = (PrintWriter) hm.get(game_join_user_arr[i]);
							temp_pw.println("joinㅣ"+this.member_id);
							temp_pw.flush();
	        		   }

	               }
	           }
	        }
        }
    } // doJoin() 메소드


    /**
     * 채팅방에 입장한 클라이언트에게 wirter를 주는 메소드 (writer pool에 저장)
     */
    private void addWriter(PrintWriter pw) {
		/**
		 * 소켓에 들어온 멤버와 소켓 연결
		 */
    	synchronized (hm) {

            /**
        	 * 만약 해당 키의 writer가 있다면 제거
        	 */
    		if(hm.get(this.member_id)!=null){
                consoleLog(TAG,this.member_id+" writer 없음");
                PrintWriter remove_pw = (PrintWriter) hm.get(this.member_id);
                removeWriter(remove_pw);
            }

      		consoleLog(TAG,this.member_id+" writer 추가");
              hm.put(this.member_id, pw);
        }

    } // addWriter() 메소드


    /**
     * 퇴장 메소드
     */
    private void doQuit(String data, PrintWriter pw) {
        synchronized (hm) {

	        String[] data_arr = data.split("ㅣ");
	        removeWriter(pw);
	        String[] game_join_user_arr = data_arr[0].split(",");

	        Set<String> set = hm.keySet();
	        Iterator<?> iterator = set.iterator();

	        /**
	         * member_id와 printwriter를 가지고 있는 hashmap을 조회하여
	         */
	        while(iterator.hasNext()) {
	           String key = (String)iterator.next();
	           for (int i = 0; i < game_join_user_arr.length; i++) {
	        	   if(key.equals(game_join_user_arr[i])){
	        		   if(!member_id.equals(game_join_user_arr[i])){
	        			   PrintWriter temp_pw = (PrintWriter) hm.get(game_join_user_arr[i]);
							temp_pw.println("quitㅣ"+this.member_id);
							temp_pw.flush();
	        		   }
	               }
	           }
	        }

        }

    } // doQuit() 메소드

    /**
     * 준비하는 메소드
     */
    private void doReady(String data, PrintWriter pw){
    	synchronized (hm) {
    		String[] data_arr = data.split("ㅣ");
    		String[] game_join_user_arr = data_arr[0].split(",");
    		Set<String> set = hm.keySet();
	        Iterator<?> iterator = set.iterator();
    		String random = random();
    		while(iterator.hasNext()) {
 	           String key = (String)iterator.next();
 	           for (int i = 0; i < game_join_user_arr.length; i++) {
 	        	   if(key.equals(game_join_user_arr[i])){
 	        		   PrintWriter temp_pw = (PrintWriter) hm.get(game_join_user_arr[i]);
        			   temp_pw.println("readyㅣ"+this.member_id+"ㅣ"+random);
        			   temp_pw.flush();
 	               }
 	           }
    		} // end while
		}
    } // doReady()


    /**
     * 클라이언트가 채팅방에서 나가면서 해당 클라이언트가 가지고있는 PrintWriter를 제거해줌
     */
    private void removeWriter(PrintWriter pw) {
    	synchronized (hm) {

            if(hm.get(this.member_id)==null){
                consoleLog(TAG,this.member_id+" writer 없음");
            }else{
                consoleLog(TAG,this.member_id+" writer 제거");
                hm.remove(this.member_id);
            }

        }
    } // removeWriter() 메소드

    /**
     * 메시지 작성, 전송
     */
    private void doMessage(String data) {

        consoleLog(TAG,"[doMessage] "+data);

        synchronized (hm) {

	        String[] data_arr = data.split("ㅣ");
	        String[] chat_member_id_arr = data_arr[0].split(",");

	        Set<String> set = hm.keySet();
	        Iterator<?> iterator = set.iterator();

	        /**
	         * member_id와 printwriter를 가지고 있는 hashmap을 조회하여
	         */
	        while(iterator.hasNext()) {
	           String key = (String)iterator.next();
	           for (int i = 0; i < chat_member_id_arr.length; i++) {
	        	   if(key.equals(chat_member_id_arr[i])){

						PrintWriter pw = (PrintWriter) hm.get(chat_member_id_arr[i]);
						pw.println("messageㅣ"+data);
						pw.flush();

	               }
	           }
	        }

        }

    } // doMessage() 메소드


    /**
     * 서버에서 클라이언트에게 데이터 전송 (메시지, 클라이언트 입장, 클라이언트 퇴장 등)
     */
    private void broadcast(String data) {
    	consoleLog(TAG,"[broadcast] "+data);

	      synchronized (hm) {
	          Collection<Object> collection = hm.values(); // 해쉬맵의 value를 Collection에 반환
	          Iterator<?> iter = collection.iterator(); //Collection을 순서화시킴
	          while(iter.hasNext()) {
	              PrintWriter pw = (PrintWriter)iter.next();
	              pw.println(data);
	              pw.flush();
	          }
	      }

    } // broadcast() 메소드




    /**
     * 로그찍는 메소드
     */
    private void consoleLog(String tag, String log) {
        System.out.println(tag+" : "+log);
    } // consoleLog 메소드

    /**
     * 랜덤숫자 메소드
     */
    private String random(){
    	Random random = new Random();

    	int[] r_arr = new int[4];
    	String str = "";
    	for (int i = 0; i < 4; i++) {
    		int r = random.nextInt(4)+1;
    		boolean check = false;
    		for(int j=0;j<r_arr.length;j++){
    			if(r_arr[j]==r){
    				check = true;
    			}
    		}
    		if(check){
    			i--;
    		}else{
    			if(i==3){
        			str = str + r;
        			r_arr[i]=r;
        		}else{
        			str = str + r + ",";
        			r_arr[i]=r;
        		}
    		}
		}
    	// return str;
      return "1,2,3,4";
    }


} // ChatThread 클래스
