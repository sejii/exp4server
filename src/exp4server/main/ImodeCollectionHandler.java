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
public class ImodeCollectionHandler extends Handler {
    
    static Map<String, String> Session = new HashMap();
    
    public ImodeCollectionHandler(Socket socket) throws IOException {
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
    
    static private void setData(Map<String, String> Session, String id, String data){
        Session.put(id, data);
    }

    @Override
    protected void perform(Request req) throws IOException {
        // 実装してください
        String id;
        String data;
        Data d1 = new Data();
        // 標準出力にメソッド，ヘッダ，ボディを出力
        outputRawLog(req);
        // レスポンスを返す
        sendln("HTTP/1.0 200 OK");
        sendln("Content-Type: text/html; charset=utf-8");
        sendln(""); // ヘッダの終り

        sendln("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">");
        sendln("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"ja-JP\" xml:lang=\"ja-JP\">");
        sendln("<head><title>It works!</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=shift_jis\"></head>");
        sendln("<body>");
        //URLにセッションIDが付加されているか確認
        if(req.getRequestURI().equals("/") || req.getRequestURI().equals("/?")){
            sendln("<p>アンケートにようこそ!</p>");
            id = generateRandomId();
            sendln("<form action=\"MF." + id + "\" method=\"post\">");
            sendln("<input type=\"submit\" name=\"start\" value=\"送信\"></form>");
            data = generateRandomdata();
            setData(Session,id,data);
            SampleSerializer.save(data + ".bin",d1);
        }else{
	    //セッションIDの不正検知
	    try{
		//URLにセッションIDあり
		id = req.getRequestURI().substring(4,36);
		data = Session.get(id);
		String URI = req.getRequestURI().substring(0,3);
		if(data == null){
		    URI = "/";
		}
		if(new File(data + ".bin").exists()){
		    Data d2 = (Data) SampleSerializer.load(data + ".bin");
		    switch(URI){
                case "/MF":
                    sendln("<p>性別</p>");
                    sendln("<form action=\"NM." + id + "\" method=\"post\">");
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<input type=\"radio\" name=\"q1\" value=\"男\">男");
                    sendln("<input type=\"radio\" name=\"q1\" value=\"女\">女");
                    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                    break;
                case "/NM":
                    try{
                        sendln("<p>名前</p>");
                        d1.gender = req.getBody().substring(3);
                        SampleSerializer.save(data + ".bin",d1);
                        sendln("<form action=\"TH." + id + "\" method=\"post\">");
                        sendln("<p style=\"display: inline\">　　</p>");
                        sendln("<input type=\"text\" name=\"q2\" autocomplete=\"off\">");
                        sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                    }catch  (NullPointerException e) {
                        sendln("<p>エラー</p>");
                        sendln("<p>操作をやり直してください</p>");
                        sendln("<form action=\"/\">");
                        sendln("<input type=\"submit\" value=\"Top\"></form>");
                        Session.remove(id);
                        new File(data + ".bin").delete();
                    }
                    break;
                case "/TH":
                    try{
                        sendln("<p>感想</p>");
                        String d3 = d2.gender;
                        d1.gender = d3;
                        d1.name = req.getBody().substring(3);
                        SampleSerializer.save(data + ".bin",d1);
                        sendln("<form action=\"CH." + id + "\" method=\"post\">");
                        sendln("<p style=\"display: inline\">　　</p>");
                        sendln("<textarea name=\"q3\" rols=\"2\" cols=\"20\"></textarea>");
                        sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                    }catch  (NullPointerException e) {
                        sendln("<p>エラー</p>");
                        sendln("<p>操作をやり直してください</p>");
                        sendln("<form action=\"/\">");
                        sendln("<input type=\"submit\" value=\"Top\"></form>");
                        Session.remove(id);
                        new File(data + ".bin").delete();
                    }
                    break;
                case "/CH":
                    try{
                        sendln("<p>確認</p>");
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
                        sendln("<form action=\"TY." + id + "\" method=\"post\">");
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
                        Session.remove(id);
                        new File(data + ".bin").delete();
                    }
                    break;
                case "/TY": sendln("<p>ありがとうございました.</p>");
                    Session.remove(id);
                    System.out.println(new File(data + ".bin").delete());
                    break;
                default : sendln("<p>アンケートにようこそ!</p>");
                    id = generateRandomId();
                    sendln("<form action=\"MF." + id + "\" method=\"post\">");
                    sendln("<input type=\"submit\" name=\"start\" value=\"送信\"></form>");
                    if(URI.equals("/")){
                        data = generateRandomdata();
                        setData(Session,id,data);
                        SampleSerializer.save(data + ".bin",d1);
                    }
                    break;   
                }
            }else{
                sendln("<p>エラー</p>");
                sendln("<p>操作をやり直してください</p>");
                sendln("<form action=\"/\">");
                sendln("<input type=\"submit\" value=\"Top\"></form>");
                Session.remove(id);
            }
        }catch (StringIndexOutOfBoundsException e){
            sendln("<p>エラー</p>");
            sendln("<p>操作をやり直してください</p>");
            sendln("<form action=\"/\">");
            sendln("<input type=\"submit\" value=\"Top\"></form>");
        }
            sendln("</body></html>");
        }
    }
}