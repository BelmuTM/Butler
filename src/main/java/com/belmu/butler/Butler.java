package com.belmu.butler;

import com.belmu.butler.commands.*;
import com.belmu.butler.commands.admin.*;
import com.belmu.butler.commands.levels.CalcCommand;
import com.belmu.butler.commands.levels.DailyCommand;
import com.belmu.butler.commands.levels.RankCommand;
import com.belmu.butler.commands.levels.TopCommand;
import com.belmu.butler.commands.music.*;
import com.belmu.butler.level.GainExpEvent;
import com.belmu.butler.level.Levels;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Butler extends ListenerAdapter {

    public static boolean ready = false;

    public static DataParser dataParser = new DataParser();

    public static final int deleteTime = 25;
    public static final TimeUnit unit = TimeUnit.SECONDS;

    public static final int darkGray = 0x474747;
    public static final int gold     = 0xfcba03;
    public static final int green    = 0x75cf21;
    public static final int red      = 0xde381b;

    public static Object[] listeners = new Object[] {
            // Events
            new Butler(),
            new GainExpEvent(),

            // Admin commands
            new SetLevelCommand(),
            new DebugCommand(),

            // Levels commands
            new CalcCommand(),
            new DailyCommand(),
            new RankCommand(),
            new TopCommand(),

            // Misc commands
            new PingCommand(),

            // Music commands
            new StopCommand(),
            new PlayCommand(),
            new SkipCommand(),
            new QueueCommand()
    };

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        JDABuilder builder = JDABuilder
                .create(Credentials.token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setAudioSendFactory(new NativeAudioSendFactory());

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Chainsaw Man"));

        jda = builder.build();
        jda.addEventListener(listeners);
        jda.awaitReady();

        Credentials.clientCredentialsSync();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Levels.retrieveXpBackup();
        Levels.startXpBackupRoutine();
        ready = true;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        for(Object listener : listeners) {
            if(listener == this) continue;

            try {
                String name         = listener.getClass().getField("cmdName").get(listener).toString();
                String description  = listener.getClass().getField("cmdDescription").get(listener).toString();

                try {
                    OptionData[] options = (OptionData[]) listener.getClass().getField("options").get(listener);
                    commandData.add(Commands.slash(name, description).addOptions(options));

                } catch(NoSuchFieldException nsfe) {
                    commandData.add(Commands.slash(name, description));
                }
            } catch (IllegalAccessException | NoSuchFieldException ignored) {}
        }
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
