package exp4server.main;

import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;
import java.io.File;

import exp4server.frozen.Handler;
import exp4server.frozen.Request;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.security.NoSuchAlgorithmException;

/**
 * Webクライアントと通信を行うクラス
 */
public class ServerCollectionHandler extends Handler {
    
    static Map<String, String> cookie = new HashMap();
    
    public ServerCollectionHandler(Socket socket) throws IOException {
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
    protected void perform(Request req) throws IOException {
        // 実装してください
        String key;
        String data;
        Data d1 = new Data();
        // 標準出力にメソッド，ヘッダ，ボディを出力
        outputRawLog(req);
    //クッキーの存在確認
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
	    //Mapにデータが存在しない場合
	    if(!cookie.containsKey(key)){
            if(!req.getRequestURI().equals("/CH")){
                data = generateRandomdata();
                setData(cookie,key,data);
                SampleSerializer.save(cookie.get(key) + ".bin",d1);
            }
	    }
	}else{
	    //初回アクセス
            sendln("HTTP/1.0 200 OK");
            sendln("Content-Type: text/html; charset=utf-8");
            key = generateRandomId();
            sendln("Set-Cookie: key=" + key);
            data = generateRandomdata();
            setData(cookie, key,data);
            SampleSerializer.save(cookie.get(key) + ".bin",d1);
            sendln(""); 
	}
	// ヘッダの終り        
    // レスポンスを返す
	data = cookie.get(key);
	sendln("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">");
	sendln("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"ja-JP\" xml:lang=\"ja-JP\">");
	sendln("<head><title>It works!</title></head>");
	sendln("<body>");
    //ファイルの存在を確認、なければエラー表示
        if(new File(data + ".bin").exists()){
	    Data d2 = (Data) SampleSerializer.load(data + ".bin");
	    
	    switch(req.getRequestURI()){
	    case "/MF": 
		sendln("<p>性別</p>");
		sendln("<form action=\"NM\" method=\"post\">");
        sendln("<p style=\"display: inline\">　　</p>");
		sendln("<input type=\"radio\" name=\"q1\" value=\"男\">男");
		sendln("<input type=\"radio\" name=\"q1\" value=\"女\">女");
		sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
		break;
	    case "/NM":
        //不正なURLからのアクセスなら例外処理
		try{
		    d1.gender = req.getBody().substring(3);
		    SampleSerializer.save(data + ".bin",d1);
		    sendln("<p>名前</p>");
		    sendln("<form action=\"TH\" method=\"post\">");
            sendln("<p style=\"display: inline\">　　</p>");
		    sendln("<input type=\"text\" name=\"q2\" autocomplete=\"off\">");
		    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
		}catch  (NullPointerException e) {
		    sendln("<p>エラー</p>");
		    sendln("<p>操作をやり直してください</p>");
		    sendln("<form action=\"/\">");
		    sendln("<input type=\"submit\" value=\"Top\"></form>");
		    cookie.remove(key);
		    new File(data + ".bin").delete();
		}
		break;
	    case "/TH":
		try{
		    String d3 = d2.gender;
		    d1.gender = d3;
		    d1.name = req.getBody().substring(3);
		    SampleSerializer.save(data + ".bin",d1);
		    sendln("<p>感想</p>");
		    sendln("<form action=\"CH\" method=\"post\">");
            sendln("<p style=\"display: inline\">　　</p>");
		    sendln("<textarea name=\"q3\" rols=\"2\" cols=\"20\"></textarea>");
		    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
		}catch  (NullPointerException e) {
		    sendln("<p>エラー</p>");
		    sendln("<p>操作をやり直してください</p>");
		    sendln("<form action=\"/\">");
		    sendln("<input type=\"submit\" value=\"Top\"></form>");
		    cookie.remove(key);
		    new File(data + ".bin").delete();
		}
		break;
	    case "/CH":
                try{
		    String d4 = d2.gender;
		    String d5 = d2.name;
		    d1.gender = d4;
		    d1.name = d5;
		    d5 = d5.replaceAll("%26", "%26amp%3B");
		    d5 = d5.replaceAll("%3C","%26lt%3B");
		    d5 = d5.replaceAll("%3E","%26gt%3B");
		    d5 = d5.replaceAll("%22","%26quot%3B");
		    d5 = d5.replaceAll("%0D%0A", "<br>");
		    String d6 = req.getBody().substring(3);
		    d1.description = d6;
		    d6 = d6.replaceAll("%26", "%26amp%3B");
		    d6 = d6.replaceAll("%3C","%26lt%3B");
		    d6 = d6.replaceAll("%3E","%26gt%3B");
		    d6 = d6.replaceAll("%22","%26quot%3B");
		    d6 = d6.replaceAll("%0D%0A", "<br>");
		    SampleSerializer.save(data + ".bin",d1);
		    sendln("<p>確認</p>");
		    sendln("<form action=\"TY\" method=\"post\">");
		    sendln("<p>性別</p>");
            sendln("<p style=\"display: inline\">　　</p>");
		    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(d4, "UTF-8")) + "</p>");
		    sendln("<p>名前</p>");
            sendln("<p style=\"display: inline\">　　</p>");
		    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(d5, "UTF-8")) + "</p>");
		    sendln("<p>感想</p>");
            sendln("<p style=\"display: inline\">　　</p>");
		    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(d6, "UTF-8")) + "</p>");
		    sendln("<p><input type=\"submit\" name=\"end\" value=\"送信\"></p></form>");
		}catch  (NullPointerException e) {
		    sendln("<p>エラー</p>");
		    sendln("<p>操作をやり直してください</p>");
		    sendln("<form action=\"/\">");
		    sendln("<input type=\"submit\" value=\"Top\"></form>");
		    cookie.remove(key);
		    new File(data + ".bin").delete();
		}
		break;
	    case "/TY":
		sendln("<p>ありがとうございました.</p>");
		cookie.remove(key);
		new File(data + ".bin").delete();	    
		break;
	    default:
		sendln("<p>アンケートにようこそ!</p>");
		sendln("<form action=\"MF\" method=\"post\">");
		sendln("<input type=\"submit\" name=\"start\" value=\"送信\"></form>");
		break;
	    }
	}else{
	    sendln("<p>エラー</p>");
	    sendln("<p>操作をやり直してください</p>");
	    sendln("<form action=\"/\">");
	    sendln("<input type=\"submit\" value=\"Top\"></form>");
	    cookie.remove(key);
	}
	sendln("</body></html>");
    }
}
