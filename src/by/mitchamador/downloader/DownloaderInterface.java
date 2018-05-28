package by.mitchamador.downloader;

import by.mitchamador.UrlItem;

public interface DownloaderInterface {

    boolean match(String name);

    void download(UrlItem item) throws Exception;

}
