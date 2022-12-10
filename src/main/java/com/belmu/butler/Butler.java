package com.belmu.butler;

import com.belmu.butler.commands.Calc;
import com.belmu.butler.commands.Ping;
import com.belmu.butler.commands.Rank;
import com.belmu.butler.commands.Top;
import com.belmu.butler.commands.admin.SetLevel;
import com.belmu.butler.level.GainExpEvent;
import com.belmu.butler.level.LevelConfig;
import com.belmu.butler.level.LevelUpEvent;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Butler extends ListenerAdapter {

    public static final String dataPath = "src/main/java/com/belmu/butler/data/";

    public static final int deleteTime = 25;
    public static final TimeUnit unit = TimeUnit.SECONDS;

    public static final int darkGray = 0x474747;

    public static Object[] listeners = new Object[] {
            // Events
            new Butler(),
            new GainExpEvent(),
            new LevelUpEvent(),

            // Admin commands
            new SetLevel(),

            // Misc commands
            new Ping(),
            new Calc(),
            new Rank(),
            new Top()
    };

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        JDABuilder builder = JDABuilder
                .create(Token.token, GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL);

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Chainsaw Man"));

        jda = builder.build();
        jda.addEventListener(listeners);
        jda.awaitReady();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LevelConfig.retrieveBackup();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LevelConfig.backupLevels();
                System.out.println("[INFO] Saved levels backup on " + new java.util.Date());
            }
        }, 0L, 600000L); // 10 minutes
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