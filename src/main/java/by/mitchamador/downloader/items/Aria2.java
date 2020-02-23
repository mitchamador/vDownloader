package by.mitchamador.downloader.items;

import by.mitchamador.UrlItem;
import by.mitchamador.downloader.Downloader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Aria2 extends Downloader {

    public Aria2() {
    }

    @Override
    public boolean match(String name) {
        return "aria2".equals(name);
    }

    @Override
    public void download(UrlItem item) throws Exception {

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
            sendJsonToDownloader(common.aria2Url, json);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

}
