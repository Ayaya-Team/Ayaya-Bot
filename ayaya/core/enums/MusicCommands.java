package ayaya.core.enums;

import ayaya.commands.music.*;
import com.jagrosh.jdautilities.command.Command;

/**
 * The music commands.
 */
public enum MusicCommands {

    DEQUEUE("dequeue", new Dequeue()), JOIN("join", new Join()), LEAVE("leave", new Leave()),
    /*MOVE("move", new Move()),*/ MOVETRACK("movetrack", new MoveTrack()), NP("np", new NP()),
    PAUSE("pause", new Pause()), PLAY("play", new Play()), PREVIOUS("previous", new Previous()),
    QUEUE("queue", new Queue()), REPEAT("repeat", new Repeat()), RESUME("resume", new Resume()),
    SEEK ("seek", new Seek()), SHUFFLE("shuffle", new Shuffle()), SKIP("skip", new Skip()),
    STOP("stop", new Stop()), VOLUME("volume", new Volume());

    private String name;
    private Command cmd;

    MusicCommands(String name, Command cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    public String getName() {
        return name;
    }

    public Command getCommand() {
        return cmd;
    }

    public MusicCommand getCommandAsMusicCommand() {
        return (MusicCommand) cmd;
    }

}