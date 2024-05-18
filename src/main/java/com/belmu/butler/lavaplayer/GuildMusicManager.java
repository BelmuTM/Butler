package com.belmu.butler.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class GuildMusicManager {

    public final AudioPlayer audioPlayer;
    public final TrackScheduler trackScheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager, MessageChannelUnion channel) {
        this.audioPlayer    = manager.createPlayer();
        this.trackScheduler = new TrackScheduler(this.audioPlayer, channel);
        this.sendHandler    = new AudioPlayerSendHandler(this.audioPlayer);
        this.audioPlayer.addListener(this.trackScheduler);
    }

    public AudioPlayerSendHandler getSendHandler() { return sendHandler; }
}
