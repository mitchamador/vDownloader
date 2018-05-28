package by.mitchamador.downloader;

import by.mitchamador.Common;
import by.mitchamador.UrlItem;
import by.mitchamador.UrlItemDB;
import by.mitchamador.parser.Parser;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static by.mitchamador.Common.LogLevel.*;

public class Downloader {
    private DownloaderInterface downloader;
    private String xTransmissionSessionId;

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    Common common;

    String auth = null;

    public Downloader() {
    }

    void setDownloader(DownloaderInterface downloader) {
        this.downloader = downloader;
    }

    public boolean match(String name) {
        if (downloader != null) {
            return downloader.match(name);
        }
        return false;
    }

    public void download(UrlItem item, Parser parser) {
        try {
            String str = "";
            str += common.appendLog(LOGLEVEL_VERBOSE, "download ");
            if (!item.torrent.startsWith("magnet:")) {
                str += common.appendLog(LOGLEVEL_VERBOSE, "torrent ");
                item.torrent = new BASE64Encoder().encode(parser.getFile(item.torrent));
            }

            if (allowDownload(item)) {
                str += common.appendLog(LOGLEVEL_DEFAULT, "\"" + item.name + "\"");
                str += common.appendLog(LOGLEVEL_VERBOSE, " to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir));
                if (!common.test) {
                    if (downloader != null) {
                        downloader.download(item);
                    } else {
                        common.log(LOGLEVEL_DEFAULT, "no downloader!!!");
                    }
                } else {
                    str = common.appendLog(LOGLEVEL_VERBOSE, "simulate ") + str;
                }
                common.log(LOGLEVEL_DEFAULT, str);
            } else {
                common.log(LOGLEVEL_VERBOSE, "already downloaded \"" + item.name + "\" to " + (item.dir == null || item.dir.isEmpty() ? "default dir" : item.dir));
            }

            saveUrlItemToDb(item);
        } catch (Exception e) {
            common.log(LOGLEVEL_VERBOSE, e.getMessage() + " in " + item.name);
        }
    }


    private boolean allowDownload(UrlItem item) {
        try {
            return common.forceDownload | UrlItemDB.containsHash(item.getHash()) == -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void saveUrlItemToDb(UrlItem item) {
        try {
            item.rowID = UrlItemDB.insertItem(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    JSONObject sendJsonToDownloader(String url, JSONObject json) throws Exception {
        String postParams = json.toString();

        int responseCode = 0;

        JSONObject result;
        do {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            // Acts like a browser
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "Content-Type: application:/json");

            if (auth != null) {
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
                conn.setRequestProperty ("Authorization", basicAuth);
            }

            if (xTransmissionSessionId != null) {
                conn.setRequestProperty("X-Transmission-Session-Id", xTransmissionSessionId);
            }
            //conn.setRequestProperty("Content-Length", Integer.toString(postParams.length() + aria2Url.length()));

            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Send post request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(postParams);
            wr.flush();
            wr.close();

            responseCode = conn.getResponseCode();

            common.log(LOGLEVEL_DEBUG, "Sending 'POST' request to URL : " + url);
            common.log(LOGLEVEL_DEBUG, "Post parameters : " + postParams);
            common.log(LOGLEVEL_DEBUG, "Response Code : " + responseCode);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            result = null;

            if (responseCode == 200) {
                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                byte[] buf = new byte[Common.BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = in.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                in.close();

                result = new JSONObject(new String(outputStream.toByteArray()));

                common.log(LOGLEVEL_DEBUG, result.toString(4));

            } else if (responseCode == 409 && conn.getHeaderField("X-Transmission-Session-Id") != null && xTransmissionSessionId == null) {
                xTransmissionSessionId = conn.getHeaderField("X-Transmission-Session-Id");
            } else {
                throw new Exception("downloader response " + responseCode + ":" + conn.getResponseMessage());
            }
        } while (responseCode != 200);

        return result;
    }

}
