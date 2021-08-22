package ayaya.commands.music;

import ayaya.core.enums.CommandCategories;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class Shuffle extends MusicCommand {

    public Shuffle() {
        this.name = "shuffle";
        this.help = "In need for some music randomization? Use this command to shuffle it.";
        this.arguments = "{prefix}shuffle";
        this.category = CommandCategories.MUSIC.asCategory();
        this.botPerms = new Permission[]{Permission.VOICE_CONNECT, Permission.MESSAGE_WRITE};
        this.cooldownTime = 5;
    }

    @Override
    protected void executeMusicCommand(CommandEvent event, VoiceChannel voiceChannel) {

        TextChannel textChannel = event.getTextChannel();
        Guild guild = event.getGuild();
        GuildVoiceState voiceState = event.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("I'm not playing anything right now.");
        } else if (voiceChannel == voiceState.getChannel()) {
            if (musicHandler.getMusicAmount(guild) == 0)
                event.reply("There are no musics to shuffle in the queue.");
            else {
                musicHandler.queueShuffle(guild);
                event.replySuccess("Queue shuffled with success. Note that any tracks already playing do not change positions.");
            }
        } else {
            event.reply("I only listen to the music commands of who is in the same voice channel as me.");
        }

    }

}