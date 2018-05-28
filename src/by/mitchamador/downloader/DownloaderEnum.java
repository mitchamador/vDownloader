package by.mitchamador.downloader;

public enum DownloaderEnum {
    DOWNLOADER_ARIA2(new DownloaderAria2()),
    DOWNLOADER_TRANSMISSION(new DownloaderTransmission());

    public Downloader getDownloader() {
        return downloader;
    }

    Downloader downloader;

    DownloaderEnum(Downloader downloader) {
        this.downloader = downloader;
    }
}
