package com.belmu.butler.lavaplayer;

import com.belmu.butler.Butler;
import com.belmu.butler.utility.CooldownMessages;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager getInstance() {
        if(INSTANCE == null) INSTANCE = new PlayerManager(); return INSTANCE;
    }

    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String url) {
        GuildMusicManager musicManager = getMusicManager(event.getGuild());

        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                User user = event.getUser();

                final EmbedBuilder trackLoaded = new EmbedBuilder()
                        .setColor(Butler.gold)
                        .setDescription("\uD83D\uDCE5 **Added** `" + track.getInfo().title + "` to the queue")
                        .addField("Duration", new SimpleDateFormat("mm:ss").format(track.getDuration()), true)
                        .addField("Author", track.getInfo().author, true)
                        .addField("Source", "[Link](" + track.getInfo().uri + ")", true)
                        .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getHook().sendMessageEmbeds(trackLoaded.build()).queue();

                musicManager.trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if(tracks.isEmpty()) return;

                if(url.startsWith("ytsearch:")) {
                    trackLoaded(tracks.get(0));
                    return;
                }

                long totalDuration = 0;
                for(AudioTrack track : tracks) {
                    musicManager.trackScheduler.queue(track);
                    totalDuration += track.getDuration();
                }

                User user = event.getUser();

                final EmbedBuilder playlistLoaded = new EmbedBuilder()
                        .setColor(Butler.gold)
                        .setDescription("\uD83D\uDCE5 **Added** `" + playlist.getName() + "` [" + playlist.getTracks().size() + " songs] to the queue")
                        .addField("Total Duration", new SimpleDateFormat("mm:ss").format(totalDuration), true)
                        .addField("Source", "[Link](" + url + ")", true)
                        .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getHook().sendMessageEmbeds(playlistLoaded.build()).queue();
            }

            @Override
            public void noMatches() {
                final EmbedBuilder noMatch = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription(":gear: **Error:** No song matches found.");
                CooldownMessages.reply(event, noMatch.build());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                final EmbedBuilder failed = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription(":gear: **Error:** " + exception.getMessage());
                CooldownMessages.reply(event, failed.build());
            }
        });
    }

    public final EmbedBuilder notConnected = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: I am not connected to any channel.");

    public final EmbedBuilder sameChannel = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: Please join my channel first.");

    public final EmbedBuilder noTrackNext = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: There is no track to play next.");

    public final EmbedBuilder notPlaying = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: There is no track playing.");
}
