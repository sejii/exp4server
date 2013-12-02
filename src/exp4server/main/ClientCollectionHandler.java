package exp4server.main;

import java.net.Socket;
import java.net.URLDecoder;
import java.io.*;

import exp4server.frozen.Handler;
import exp4server.frozen.Request;

/**
 * Webクライアントと通信を行うクラス
 */
public class ClientCollectionHandler extends Handler {
    
    public ClientCollectionHandler(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected void perform(Request req) throws IOException {
        // 実装してください
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
                    String str = (URLDecoder.decode(req.getBody(), "UTF-8")).substring(3);
                    sendln("<p>名前</p>");
                    sendln("<form action=\"TH\" method=\"post\">");
                    sendln("<input type=\"hidden\" name=\"q1\" value=\"" + str + "\">");
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
                    String str1 = req.getBody().substring(16);
                    sendln("<p>感想</p>");
                    sendln("<form action=\"CH\" method=\"post\">");
                    sendln("<input type=\"hidden\" name=\"q1\" value=\"" + (URLDecoder.decode(req.getBody(), "UTF-8")).substring(3,4) + "\">");
                    str1 = str1.replaceAll("%26","%26amp%3B");
                    str1 = str1.replaceAll("%3C","%26lt%3B");
                    str1 = str1.replaceAll("%3E","%26gt%3B");
                    str1 = str1.replaceAll("%22","%26quot%3B");
                    sendln("<input type=\"hidden\" name=\"q2\" value=\"" + (URLDecoder.decode(str1, "UTF-8")) + "\">");
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<textarea name=\"q3\" rols=\"2\" cols=\"20\"></textarea>");
                    sendln("<p><input type=\"submit\" value=\"送信\"></p></form>");
                }catch  (NullPointerException e) {
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }
                break;
            case "/CH":
                try{
                    String str2 = req.getBody().substring(16);
                    sendln("<p>確認</p>");
                    sendln("<form action=\"TY\" method=\"post\">");
                    sendln("<p>性別</p>");
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(req.getBody(), "UTF-8")).substring(3,4) + "</p>");
                    sendln("<p>名前</p>");
                    str2 = str2.replaceAll("%26","%26amp%3B");
                    str2 = str2.replaceAll("%3C","%26lt%3B");
                    str2 = str2.replaceAll("%3E","%26gt%3B");
                    str2 = str2.replaceAll("%22","%26quot%3B");
                    str2 = str2.replaceAll("%0D%0A", "<br>");
                    int index = str2.indexOf("q3=");
                    String name = str2.substring(0,index-1);
                    String thought = str2.substring(index+3);
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(name, "UTF-8")) + "</p>");
                    sendln("<p>感想</p>");
                    sendln("<p style=\"display: inline\">　　</p>");
                    sendln("<p style=\"display: inline\">" + (URLDecoder.decode(thought, "UTF-8")) + "</p>");
                    sendln("<p><input type=\"submit\" name=\"end\" value=\"送信\"></p></form>");
                }catch  (NullPointerException e) {
                    sendln("<p>エラー</p>");
                    sendln("<p>操作をやり直してください</p>");
                    sendln("<form action=\"/\">");
                    sendln("<input type=\"submit\" value=\"Top\"></form>");
                }
                break;
            case "/TY": sendln("<p>ありがとうございました.</p>");
                sendln("<form action=\"TY\" method=\"post\">");
                break;
            default : sendln("<p>アンケートにようこそ!</p>");
                sendln("<form action=\"MF\" method=\"post\">");
                sendln("<input type=\"submit\" name=\"start\" value=\"送信\"></form>");
                break;
        }    
        sendln("</body></html>");	
    }
}