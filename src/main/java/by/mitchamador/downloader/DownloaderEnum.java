package by.mitchamador.downloader;

import by.mitchamador.downloader.items.Aria2;
import by.mitchamador.downloader.items.Transmission;

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
