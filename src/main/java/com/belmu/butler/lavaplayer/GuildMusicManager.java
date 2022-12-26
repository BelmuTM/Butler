package com.belmu.butler.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {

    public final AudioPlayer audioPlayer;
    public final TrackScheduler trackScheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.trackScheduler = new TrackScheduler(this.audioPlayer);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.audioPlayer.addListener(this.trackScheduler);
    }

    public AudioPlayerSendHandler getSendHandler() { return sendHandler; }
}
