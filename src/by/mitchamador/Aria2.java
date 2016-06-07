package by.mitchamador;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import sun.misc.BASE64Encoder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vicok on 01.06.2016.
 */
public class Aria2 {

    public static void sendToAria2(Common common, UrlItem item) throws Exception {
        try {
            if (item.torrent.startsWith("magnet:")) {
                if (allowSendToAria2(common, item)) {
                    String str = "download \"" + item.name + "\" to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir);
                    if (!common.test) {
                        sendUriToAria2(common, item);
                    } else {
                        str = "simulate " + str;
                    }
                    common.log(Common.LOGLEVEL_DEFAULT, str);
                } else {
                    common.log(Common.LOGLEVEL_VERBOSE, "already downloaded \"" + item.name + "\" to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir));
                }

            } else {
                if (common.cookies == null) {
                    item.torrent = new BASE64Encoder().encode(Jsoup.connect(item.torrent).timeout(Common.TIMEOUT).ignoreContentType(true).execute().bodyAsBytes());
                } else {
                    item.torrent = new BASE64Encoder().encode(Jsoup.connect(item.torrent).timeout(Common.TIMEOUT).ignoreContentType(true).cookies(common.cookies).execute().bodyAsBytes());
                }

                if (allowSendToAria2(common, item)) {
                    String str = "download torrent \"" + item.name + "\" to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir);
                    if (!common.test) {
                        sendUriToAria2(common, item);
                    } else {
                        str = "simulate " + str;
                    }
                    common.log(Common.LOGLEVEL_DEFAULT, str);
                } else {
                    common.log(Common.LOGLEVEL_VERBOSE, "already downloaded \"" + item.name + "\" to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir));
                }
            }
            saveUrlItemToDb(common, item);
        } catch (Exception e) {
            common.log(Common.LOGLEVEL_DEFAULT, e.getMessage() + " in " + item.name);
        }
    }

    private static boolean allowSendToAria2(Common common, UrlItem item) {
        try {
            return common.forceDownload | UrlItemDB.containsHash(item.getHash()) == -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void saveUrlItemToDb(Common common, UrlItem item) {
        try {
            item.rowID = UrlItemDB.insertItem(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendUriToAria2(Common common, UrlItem item) throws Exception {

        ArrayList params = new ArrayList();
        JSONObject json = new JSONObject();
        json.put("jsonrpc", "2.0");
        json.put("id", "qwer");
        if (item.torrent.startsWith("magnet:")) {
            json.put("method", "aria2.addUri");

            ArrayList uris = new ArrayList();
            uris.add(item.torrent);

            params.add(new JSONArray(uris));
        } else {
            json.put("method", "aria2.addTorrent");

            params.add(item.torrent);

            params.add(new JSONArray());
        }

        if (item.dir != null && !item.dir.isEmpty()) {
            JSONObject o = new JSONObject();
            o.put("dir", item.dir);
            params.add(o);
        }

        json.put("params", new JSONArray(params));

        try {
            sendJsonToAria2(common, json);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    private static JSONObject sendJsonToAria2(Common common, JSONObject json) throws Exception {
        String postParams = json.toString();

        URL obj = new URL(common.aria2Url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "Content-Type: application:/json");
        //conn.setRequestProperty("Content-Length", Integer.toString(postParams.length() + aria2Url.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();

        common.log(Common.LOGLEVEL_DEBUG, "Sending 'POST' request to URL : " + common.aria2Url);
        common.log(Common.LOGLEVEL_DEBUG, "Post parameters : " + postParams);
        common.log(Common.LOGLEVEL_DEBUG, "Response Code : " + responseCode);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JSONObject result = null;

        if (responseCode == 200) {
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            byte[] buf = new byte[Common.BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            in.close();

            result = new JSONObject(new String(outputStream.toByteArray()));

            common.log(Common.LOGLEVEL_DEBUG, result.toString(4));
        } else {
            throw new Exception("aria2 response " + responseCode + ":" + conn.getResponseMessage());
        }

        return result;
    }


}
