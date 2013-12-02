package exp4server.main;

import java.net.Socket;
import java.net.URLDecoder;
import java.io.*;
import java.util.*;

import exp4server.frozen.Handler;
import exp4server.frozen.Request;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
/**
 * Webクライアントと通信を行うクラス
 */
public class NewCollectionHandler extends Handler {
    
    static Map<String, String> cookie = new HashMap();

    public NewCollectionHandler(Socket socket) throws IOException {
        super(socket);
    }

    private static Random RANDOM;
    static {
        try {
            RANDOM = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            RANDOM = new Random(new Date().getTime());
        }
    }
    
    private static String generateRandomId() {
        final byte b[] = new byte[16];
        RANDOM.nextBytes(b);
        return bytesToHexString(b);
    }
    private static String generateRandomdata() {
        final byte b[] = new byte[8];
        RANDOM.nextBytes(b);
        return bytesToHexString(b);
    }
    
    public static String bytesToHexString(byte[] bytes) {
        final StringBuffer sb = new StringBuffer();
        for (final byte b: bytes) {
            final String s = Integer.toHexString(0xff & b);
            sb.append(s.length() == 1 ? "0" + s : s);
        }
        return sb.toString();
    }
    static private void setData(Map<String, String> cookie, String key, String data){
	cookie.put(key, data);
    }

    
    @Override
    protected void perform(Request req) throws IOException {
        // 実装してください
	String key;
    String data;
    String algorithm = "AES";
	//String algorithm = "BLOWFISH";
        // 標準出力にメソッド，ヘッダ，ボディを出力
        outputRawLog(req);
	// レスポンスを返す
	if(req.getHeaders().containsKey("Cookie")){
	    sendln("HTTP/1.0 200 OK");
            sendln("Content-Type: text/html; charset=utf-8");
	    int index;
	    index = req.getHeaders().get("Cookie").indexOf("key");
	    //keyが存在しない場合
	    if(index == -1){
		key = generateRandomId();
		sendln("Set-Cookie: key=" + key);
	    }else{
		index += 4;
		int end = index + 32;
		key = req.getHeaders().get("Cookie").substring(index, end);
	    }
	    sendln("");
	    //Map内にデータが存在しない場合
	    if(!cookie.containsKey(key)){
		data = generateRandomdata();
		setData(cookie,key,data);
	    }
	    data = cookie.get(key);
	}else{
	    //初回アクセス
            sendln("HTTP/1.0 200 OK");
            sendln("Content-Type: text/html; charset=utf-8");
            key = generateRandomId();
            sendln("Set-Cookie: key=" + key);
	    data = generateRandomdata();
	    setData(cookie, key,data);
            sendln(""); 
	}	
	
        sendln("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">");
        sendln("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"ja-JP\" xml:lang=\"ja-JP\">");
        sendln("<head><title>It works!</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=shift_jis\"></head>");
        sendln("<body>");
        switch(req.getRequestURI()){
            case "/MF": sendln("<p>性別</p>");
                sendln("<form action=\"NM\" method=\"post\">");
                sendln("<p style=\"display: inline\">　　</p>");
                sendln("<input type=\"radio\" name=\"q1\" value=\"男\">男");
                sendln("<input type=\"radio\" name=\"q1\" value=\"女\">女");
                sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                break;
            case "/NM":
                try{
                    sendln("<p>名前</p>");
                    sendln("<form action=\"TH\" method=\"post\">");
                    try{
                        String d1 = (URLDecoder.decode(req.getBody(), "UTF-8")).substring(3);
                        SecretKeySpec sksSpec1 = new SecretKeySpec(data.getBytes(), algorithm);
                        Cipher cipher1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                        cipher1.init(Cipher.ENCRYPT_MODE, sksSpec1);
                        byte[] encrypted1 = cipher1.doFinal(d1.getBytes());
                        String encryptedText1 = new String(encrypted1,"iso-8859-1");
                        sendln("<input type=\"hidden\" name=\"q1\" value=\"" + encryptedText1 + "\">");
                    }catch (Exception e){
                        System.err.println("エラー" + e.getMessage());
                    }
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<input type=\"text\" name=\"q2\" autocomplete=\"off\">");
                    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                }catch  (NullPointerException e) {
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }
                break;
            case "/TH":
                try{
                    sendln("<p>感想</p>");
                    sendln("<form action=\"CH\" method=\"post\">");
                    String str1 = req.getBody();
                    int n1 = str1.indexOf("q2=");
                    String d2 = str1.substring(3,n1-1);
                    sendln("<input type=\"hidden\" name=\"q1\" value=\"" + (URLDecoder.decode(d2, "UTF-8")) + "\">");
                    n1 += 3;
                    str1 = str1.substring(n1);
                    str1 = str1.replaceAll("%26","%26amp%3B");
                    str1 = str1.replaceAll("%3C","%26lt%3B");
                    str1 = str1.replaceAll("%3E","%26gt%3B");
                    str1 = str1.replaceAll("%22","%26quot%3B");
                    try{
                        SecretKeySpec sksSpec2 = new SecretKeySpec(data.getBytes(), algorithm);
                        Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
                        cipher2.init(Cipher.ENCRYPT_MODE, sksSpec2);
                        byte[] encrypted2 = cipher2.doFinal(str1.getBytes());
                        String encryptedText2 = new String(encrypted2,"iso-8859-1");
                        sendln("<input type=\"hidden\" name=\"q2\" value=\"" + encryptedText2 + "\">");
                    }catch (Exception e){
                        System.err.println("エラー" + e.getMessage());
                    }
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<textarea name=\"q3\" rols=\"2\" cols=\"20\"></textarea>");
                    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                }catch  (NullPointerException e) {
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }catch (StringIndexOutOfBoundsException e){
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }
                break;
            case "/CH":
                try{
                    String str2 = req.getBody();
                    sendln("<p>確認</p>");
                    sendln("<form action=\"TY\" method=\"post\">");
                    sendln("<p>性別</p>");
                    int n2 = str2.indexOf("q2=");
                    String d3 = str2.substring(3,n2-1);
                    d3 = (URLDecoder.decode(d3, "UTF-8"));
                    byte[] encrypted3 = d3.getBytes("iso-8859-1");
                    n2 += 3;
                    str2 = str2.substring(n2);
                    int index = str2.indexOf("q3=");
                    String name = str2.substring(0,index-1);
                    name = (URLDecoder.decode(name, "UTF-8"));
                    byte[] encrypted4 = name.getBytes("iso-8859-1");
            
                    String thought = str2.substring(index+3);
                    thought = thought.replaceAll("%26","%26amp%3B");
                    thought = thought.replaceAll("%3C","%26lt%3B");
                    thought = thought.replaceAll("%3E","%26gt%3B");
                    thought = thought.replaceAll("%22","%26quot%3B");
                    thought = thought.replaceAll("%0D%0A", "<br>");

                    try{
                        SecretKeySpec sksSpec3 = new SecretKeySpec(data.getBytes(), algorithm);
                        Cipher cipher3 = Cipher.getInstance(algorithm);
                        cipher3.init(Cipher.DECRYPT_MODE, sksSpec3);
                        byte[] decrypted3 = cipher3.doFinal(encrypted3);
                        String decryptedText3 = new String(decrypted3);
                        sendln("<p style=\"display: inline\">　　</p>");
                        sendln("<p style=\"display: inline\">" + decryptedText3 + "</p>");
                    }catch (Exception e){
                        System.err.println("エラー1" + e.getMessage());
                    }
                    sendln("<p>名前</p>");
                    try{
                        SecretKeySpec sksSpec4 = new SecretKeySpec(data.getBytes(), algorithm);
                        Cipher cipher4 = Cipher.getInstance(algorithm);
                        cipher4.init(Cipher.DECRYPT_MODE, sksSpec4);
                        byte[] decrypted4 = cipher4.doFinal(encrypted4);
                        String decryptedText4 = new String(decrypted4);
                        sendln("<p style=\"display: inline\">　　</p>");
                        sendln("<p style=\"display: inline\">" + (URLDecoder.decode(decryptedText4, "UTF-8")) + "</p>");
                    }catch (Exception e){
                        System.err.println("エラー2" + e.getMessage());
                    }
                    sendln("<p>感想</p>");
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(thought, "UTF-8")) + "</p>");
                    sendln("<p><input type=\"submit\" name=\"end\" value=\"送信\"></p></form>");       
                }catch  (NullPointerException e) {
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }catch (StringIndexOutOfBoundsException e){
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }
                break;
            case "/TY": sendln("<p>ありがとうございました.</p>");
                sendln("<form action=\"TY\" method=\"post\">");
                cookie.remove(key);
                break;
            default : sendln("<p>アンケートにようこそ!</p>");
                sendln("<form action=\"MF\" method=\"post\">");
                sendln("<input type=\"submit\" name=\"start\" value=\"送信\"></form>");
                break;
        }    
        sendln("</body></html>");	
    }
}
