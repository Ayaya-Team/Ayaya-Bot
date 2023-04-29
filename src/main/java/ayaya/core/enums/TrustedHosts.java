package ayaya.core.enums;

/**
 * The trusted hosts for music tracks.
 */
public enum TrustedHosts {

    YOUTUBE("www.youtube.com"), YOUTU_BE("youtu.be"), YOUTUBE_MOBILE("m.youtube.com"),
    YOUTUBE_MUSIC("music.youtube.com"), SOUNDCLOUD("soundcloud.com"),
    SOUNDCLOUD_MOBILE("m.soundcloud.com"), VIMEO("vimeo.com"),
    TWITCH("www.twitch.tv"), TWITCH_MOBILE("m.twitch.tv");

    private String hostname;

    TrustedHosts(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    /**
     * Checks if a hostname is in one of the enums.
     *
     * @param hostname the hostname to compare with existent ones
     * @return true if the hostname is recognized, false on the contrary
     */
    public static boolean hostnameTrusted(String hostname) {
        for (TrustedHosts th: TrustedHosts.values()) {
            if (th.hostname.equals(hostname))
                return true;
        }
        return false;
    }

}