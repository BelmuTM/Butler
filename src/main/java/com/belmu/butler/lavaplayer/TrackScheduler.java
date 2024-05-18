package com.belmu.butler.lavaplayer;

import com.belmu.butler.Butler;
import com.belmu.butler.utility.Duration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public AudioPlayer player;
    private final MessageChannelUnion channel;
    public BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player, MessageChannelUnion channel) {
        this.player  = player;
        this.channel = channel;
        this.queue   = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) queue.offer(track);
    }

    public void nextTrack() { player.startTrack(queue.poll(), false); }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        String formattedDuration = Duration.getFormattedDuration(track.getDuration());

        final EmbedBuilder trackLoaded = new EmbedBuilder()
                .setColor(Butler.green)
                .setDescription("\uD83C\uDFB5 **Now playing** `" + track.getInfo().title + "`")
                .addField("Duration", formattedDuration, true)
                .addField("Author", track.getInfo().author, true)
                .addField("Source", "[Link](" + track.getInfo().uri + ")", true)
                .setThumbnail(track.getInfo().artworkUrl)
                .setTimestamp(Instant.now());
        this.channel.sendMessageEmbeds(trackLoaded.build()).queue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) nextTrack();
    }
}
