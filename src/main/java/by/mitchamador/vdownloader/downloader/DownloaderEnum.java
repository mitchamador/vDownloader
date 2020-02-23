package by.mitchamador.vdownloader.downloader;

import by.mitchamador.vdownloader.downloader.items.Aria2;
import by.mitchamador.vdownloader.downloader.items.Transmission;

public enum DownloaderEnum {
    DOWNLOADER_ARIA2(new Aria2()),
    DOWNLOADER_TRANSMISSION(new Transmission());

    /**
     * downloader implementation
     */
    private Downloader downloader;

    public Downloader getDownloader() {
        return downloader;
    }

    DownloaderEnum(Downloader downloader) {
        this.downloader = downloader;
    }
}
