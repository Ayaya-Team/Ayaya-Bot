package ayaya.core.enums;

/**
 * The trusted hosts for music tracks.
 */
public enum TrustedHosts {

    YOUTUBE("youtube.com"), YOUTU_BE("youtu.be"), YOUTUBE_MOBILE("m.youtube.com"),
    SOUNDCLOUD("soundcloud.com"), SOUNDCLOUD_MOBILE("m.soundcloud.com"),
    VIMEO("vimeo.com"), TWITCH("twitch.tv"), TWITCH_MOBILE("m.twitch.tv");

    private String hostname;

    TrustedHosts(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

}