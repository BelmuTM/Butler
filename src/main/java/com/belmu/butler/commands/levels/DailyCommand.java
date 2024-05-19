package com.belmu.butler.commands.levels;

import com.belmu.butler.Butler;
import com.belmu.butler.level.Levels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;

public class DailyCommand extends ListenerAdapter {

    public String cmdName = "daily";
    public String cmdDescription = "Claims your daily EXP (50xp/day)";

    private final String dailiesDataPath = "src/main/java/com/belmu/butler/data/dailies_data.json";
    private final JSONObject dailiesData = (JSONObject) Butler.dataParser.readJSON(dailiesDataPath);

    private final long dayMs = 86400000; // 24 hours in milliseconds

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();

        if (event.getName().equals(cmdName)) {
            event.deferReply(true).queue();

            if (dailiesData.get(user.getId()) != null) {
                long timestamp = Long.parseLong(dailiesData.get(user.getId()).toString());
                long timeLeft  = timestamp - System.currentTimeMillis();

                if(timeLeft > 0) {
                    long hours   = TimeUnit.MILLISECONDS.toHours(timeLeft);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60;

                    String formattedTimeLeft = String.format("%02dh %02dm %02ds", hours, minutes, seconds);

                    final EmbedBuilder alreadyClaimed = new EmbedBuilder()
                            .setColor(Butler.red)
                            .setDescription("⌛ You've already claimed your dailies!")
                            .setFooter("Come back in " + formattedTimeLeft + ".");
                    event.getHook().sendMessageEmbeds(alreadyClaimed.build()).queue();
                    return;
                }
            }

            dailiesData.put(user.getId(), System.currentTimeMillis() + dayMs);
            Butler.dataParser.writeJSON(dailiesDataPath, dailiesData);

            Levels.addExp(user, 50D);

            final EmbedBuilder dailies = new EmbedBuilder()
                    .setColor(Butler.green)
                    .setDescription("\uD83D\uDCB0 Claimed Dailies! | **+50 xp**")
                    .setFooter("Dailies reset after 24 hours.");
            event.getHook().sendMessageEmbeds(dailies.build()).queue();
        }
    }
}
