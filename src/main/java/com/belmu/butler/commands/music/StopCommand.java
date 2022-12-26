package com.belmu.butler.commands.music;

import com.belmu.butler.lavaplayer.GuildMusicManager;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class StopCommand extends ListenerAdapter {

    public String cmdName = "stop";
    public String cmdDescription = "Stops the track and leaves the channel";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            event.deferReply(true).queue();

            Guild guild = event.getGuild();

            if (!guild.getSelfMember().getVoiceState().inAudioChannel()) {
                CooldownMessages.reply(event, PlayerManager.getInstance().notConnected.build());
                return;
            }

            AudioManager audioManager = guild.getAudioManager();

            if (event.getMember().getVoiceState().inAudioChannel() && !audioManager.getConnectedChannel().getMembers().contains(event.getMember())) {
                CooldownMessages.reply(event, PlayerManager.getInstance().sameChannel.build());
                return;
            }

            final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getMusicManager(guild);
            guildMusicManager.audioPlayer.stopTrack();
            guildMusicManager.trackScheduler.queue.clear();
            audioManager.closeAudioConnection();

            event.getHook().sendMessage("Bye! :wave:").queue();
        }
    }
}
