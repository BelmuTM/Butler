
package com.belmu.butler.commands.music;

import com.belmu.butler.Butler;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends ListenerAdapter {

    public String cmdName = "play";
    public String cmdDescription = "Plays the desired track(s) in your voice channel";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.STRING, "song", "The song you want me to play", true)
    };

    public final String spotifyTrackUrl    = "https://open.spotify.com/track/";
    public final String spotifyPlaylistUrl = "https://open.spotify.com/playlist/";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            Member member = event.getMember();
            GuildVoiceState memberVoiceState = member.getVoiceState();

            if (!memberVoiceState.inAudioChannel()) {
                event.deferReply(true).queue();

                final EmbedBuilder a = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription("Please join a channel first.");
                CooldownMessages.reply(event, a.build());
                return;
            }
            event.deferReply().queue();

            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());

            String url = event.getOption("song").getAsString();

            if(url.contains(spotifyTrackUrl)) {
                String trimmedUrl = url.replace(spotifyTrackUrl, "");
                String trackId = trimmedUrl.substring(0, trimmedUrl.indexOf("?"));

                GetTrackRequest trackRequest = Butler.spotifyApi.getTrack(trackId).build();

                try {
                    Track track = trackRequest.execute();
                    url = "ytsearch:" + String.join(" ", track.getArtists()[0].getName() + track.getName());

                } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
                    e.printStackTrace();
                    return;
                }
            } else if(url.contains(spotifyPlaylistUrl)) {
                String trimmedUrl = url.replace(spotifyPlaylistUrl, "");
                String playlistId = trimmedUrl.substring(0, trimmedUrl.indexOf("?"));

                GetPlaylistRequest playlistRequest = Butler.spotifyApi.getPlaylist(playlistId).build();

                try {
                    Playlist playlist = playlistRequest.execute();
                    Paging<PlaylistTrack> playlistTracks = playlist.getTracks();

                    long totalDuration = 0;
                    for(PlaylistTrack playlistTrack : playlistTracks.getItems()) {
                        GetTrackRequest trackRequest = Butler.spotifyApi.getTrack(playlistTrack.getTrack().getId()).build();
                        Track track = null;

                        try {
                            track = trackRequest.execute();
                            url = "ytsearch:" + String.join(" ", track.getArtists()[0].getName() + track.getName());
                        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
                            final EmbedBuilder failed = new EmbedBuilder()
                                    .setColor(Butler.darkGray)
                                    .setDescription(":gear: **Error:** " + e.getMessage());
                            CooldownMessages.reply(event, failed.build());
                            return;
                        }

                        totalDuration += track.getDurationMs().longValue();
                        PlayerManager.getInstance().silentLoadAndPlay(event, url);
                    }

                    User user = event.getUser();

                    long hours   = TimeUnit.MILLISECONDS.toHours(totalDuration);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(totalDuration) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration) % 60;

                    String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    final EmbedBuilder playlistLoaded = new EmbedBuilder()
                            .setColor(Butler.gold)
                            .setDescription("\uD83D\uDCE5 **Added** `" + playlist.getName() + "` [" + playlist.getTracks().getItems().length + " songs] to the queue")
                            .addField("Total Duration", formattedDuration, true)
                            .addField("Source", "[Link](" + event.getOption("song").getAsString() + ")", true)
                            .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                            .setTimestamp(Instant.now());
                    event.getHook().sendMessageEmbeds(playlistLoaded.build()).queue();
                    return;

                } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
                    final EmbedBuilder failed = new EmbedBuilder()
                            .setColor(Butler.darkGray)
                            .setDescription(":gear: **Error:** " + e.getMessage());
                    CooldownMessages.reply(event, failed.build());
                    return;
                }
            } else {
                try {
                    new URI(url);
                } catch (URISyntaxException use) {
                    url = "ytsearch:" + String.join(" ", url) + " audio";
                }
            }
            PlayerManager.getInstance().loadAndPlay(event, url);
        }
    }
}
