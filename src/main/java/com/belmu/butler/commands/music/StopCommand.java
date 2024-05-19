package com.belmu.butler.commands.music;

import com.belmu.butler.Butler;
import com.belmu.butler.lavaplayer.GuildMusicManager;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class StopCommand extends ListenerAdapter {

    public String cmdName = "stop";
    public String cmdDescription = "Stops the track and leaves the channel";

    private final Map<Message, List<Member>> stopVotes =  new HashMap<>();
    private final int minStopVotes = 2;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals(cmdName)) {
            Guild guild = event.getGuild();
            assert guild != null;
            AudioChannelUnion channel = guild.getAudioManager().getConnectedChannel();

            if (channel == null || !guild.getSelfMember().getVoiceState().inAudioChannel()) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().notConnected.build());
                return;
            }

            Member member = event.getMember();

            if (member.getVoiceState().inAudioChannel() && !channel.getMembers().contains(member)) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().sameChannel.build());
                return;
            }

            int minMembersInChannel = 3;
            if(channel.getMembers().size() >= minMembersInChannel) {
                event.deferReply().queue();
                List<Member> votes = new ArrayList<>(); votes.add(member);

                event.getHook().sendMessageEmbeds(getVoteMessage(guild, event.getChannel(), member.getUser(), votes.size()))
                        .addActionRow(Button.of(ButtonStyle.DANGER, "stop-music", "Vote"))
                        .queue(msg -> stopVotes.put(msg, votes));
                return;
            }
            event.deferReply(true).queue();

            stopMusic(guild, event.getChannel());
            event.getHook().sendMessage("Bye! :wave:").queue();
        }
    }

    private MessageEmbed getVoteMessage(Guild guild, MessageChannelUnion channel, User user, int votes) {
        AudioTrack playing = PlayerManager.getInstance().getMusicManager(guild, channel).audioPlayer.getPlayingTrack();
        String duration    = new SimpleDateFormat("mm:ss").format(playing.getDuration());
        String nowPlaying  = "[NOW PLAYING] " + playing.getInfo().author + " » \"" + playing.getInfo().title + "\" [" + duration + "]";

        final EmbedBuilder vote = new EmbedBuilder()
                .setColor(guild.getSelfMember().getColor())
                .setDescription("\uD83D\uDD07 " + user.getAsMention() + " wants to stop the music. **[" + votes + "/" + minStopVotes + " votes]**")
                .setFooter(nowPlaying);
        return vote.build();
    }

    private void stopMusic(Guild guild, MessageChannelUnion channel) {
        final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getMusicManager(guild, channel);
        guildMusicManager.audioPlayer.stopTrack();
        guildMusicManager.trackScheduler.queue.clear();
        guild.getAudioManager().closeAudioConnection();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Message message = event.getMessage();

        if(!stopVotes.containsKey(message)) return;

        Member member = event.getMember();
        assert member != null;

        if(Objects.equals(event.getButton().getId(), "stop-music")) {

            if(stopVotes.get(message).contains(member)) {
                event.deferReply(true).queue();
                event.getHook().sendMessageEmbeds(PlayerManager.getInstance().alreadyVoted.build()).queue(msg -> {
                    if(!msg.isEphemeral()) msg.delete().queueAfter(Butler.deleteTime, Butler.unit, null, fail -> {});
                });
                return;
            }
            event.deferReply().queue();

            List<Member> votes = stopVotes.get(message); votes.add(member);
            stopVotes.put(message, votes);

            Guild guild = event.getGuild();
            event.getInteraction().getMessage().editMessageEmbeds(getVoteMessage(guild, event.getChannel(), message.getAuthor(), votes.size())).queue();

            event.getHook().sendMessage(":small_orange_diamond: | " + member.getAsMention() + " voted to stop the music.").queue();

            if(votes.size() >= minStopVotes) {
                stopVotes.remove(message);
                stopMusic(guild, event.getChannel());
            }
        }
    }
}
