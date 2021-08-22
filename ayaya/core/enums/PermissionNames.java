package ayaya.core.enums;

import net.dv8tion.jda.api.Permission;

/**
 * The permission names.
 */
public enum PermissionNames {

    CREATE_INSTANT_INVITE("invite", "Create Instant Invite"),
    KICK_MEMBERS("kick", "Kick Members"),
    BAN_MEMBERS("ban", "Ban Members"),
    ADMINISTRATOR("admin", "Administrator"),
    MANAGE_CHANNEL("manage_channels", "Manage Channels"),
    MANAGE_SERVER("manage_server", "Manage Server"),
    MESSAGE_ADD_REACTION("reactions", "Add Reactions"),
    VIEW_AUDIT_LOGS("audit_logs", "View Audit Logs"),
    PRIORITY_SPEAKER("priority_speaker", "Priority Speaker"),

    // Applicable to all channel types
    VIEW_CHANNEL("read", "Read Text Channels & See Voice Channels"),

    // Text Permissions
    MESSAGE_WRITE("send_messages", "Send Messages"),
    MESSAGE_TTS("tts", "Send TTS Messages"),
    MESSAGE_MANAGE("manage_messages", "Manage Messages"),
    MESSAGE_EMBED_LINKS("embed", "Embed Links"),
    MESSAGE_ATTACH_FILES("attach", "Attach Files"),
    MESSAGE_HISTORY("history", "Read History"),
    MENTIONS("mentions", "Mention @everyone, @here and All Roles"),
    MESSAGE_EXT_EMOJI("external_emoji", "Use External Emojis"),
    USE_SLASH_COMMANDS("slash_commands", "Use Slash Commands"),

    MANAGE_THREADS("manage_threads", "Manage Threads"),
    USE_PUBLIC_THREADS("public_threads", "Use Public Threads"),
    USE_PRIVATE_THREADS("private_threads", "Use Private Threads"),

    // Voice Permissions
    VOICE_CONNECT("connect", "Connect"),
    VOICE_SPEAK("speak", "Speak"),
    VOICE_MUTE_OTHERS("mute", "Mute Members"),
    VOICE_DEAF_OTHERS("deafen", "Deafen Members"),
    VOICE_MOVE_OTHERS("move", "Move Members"),
    VOICE_USE_VAD("voice_activity", "Use Voice Activity"),

    NICKNAME_CHANGE("change_nickname", "Change Nickname"),
    NICKNAME_MANAGE("manage_nicknames", "Manage Nicknames"),

    MANAGE_ROLES("manage_roles", "Manage Roles"),
    MANAGE_PERMISSIONS("manage_permissions", "Manage Permissions"),
    MANAGE_WEBHOOKS("manage_webhooks", "Manage Webhooks"),
    MANAGE_EMOTES("manage_emojis", "Manage Emojis");

    private String short_name;
    private String name;

    PermissionNames(String s, String n) {
        short_name = s;
        name = n;
    }

    public boolean hasShortName() {
        return short_name.length() > 0;
    }

    public String getShortName() {
        return short_name;
    }

    public String getName() {
        return name;
    }

    public Permission getPermission() {
        return Permission.valueOf(name);
    }

}