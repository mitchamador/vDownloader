package by.mitchamador.downloader;

import by.mitchamador.UrlItem;
import org.json.JSONObject;

public class DownloaderTransmission extends Downloader implements DownloaderInterface {

    public DownloaderTransmission() {
        setDownloader(this);
    }

    @Override
    public boolean match(String name) {
        return "transmission".equals(name);
    }

    @Override
    public void download(UrlItem item) throws Exception {
        JSONObject json = new JSONObject();
        json.put("jsonrpc", "2.0");
        json.put("method", "torrent-add");

        JSONObject params = new JSONObject();

        params.put(item.torrent.startsWith("magnet:") ? "filename" : "metainfo", item.torrent);

        if (item.dir != null && !item.dir.isEmpty()) {
            params.put("dir", item.dir);
        }

        params.put("paused", false);

        json.put("arguments", params);

        try {
            auth = "transmission:transmission";
            sendJsonToDownloader(common.transmissionUrl, json);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

}
