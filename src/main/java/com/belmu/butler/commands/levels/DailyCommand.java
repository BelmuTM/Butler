package com.belmu.butler.commands.levels;

import com.belmu.butler.Butler;
import com.belmu.butler.DataParser;
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

    private final long dayMs = 86400000; // 24 hours in milliseconds

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        User user = event.getMember().getUser();

        if (cmd.equals(cmdName)) {
            event.deferReply(true).queue();

            Object dailyObject = Butler.data.get("daily");

            if (!dailyObject.toString().isEmpty()) {
                JSONObject userDailies = (JSONObject) dailyObject;

                long timestamp = Long.parseLong(userDailies.get(user.getId()).toString());
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

            JSONObject userTimestamp = new JSONObject();
            userTimestamp.put(user.getId(), (System.currentTimeMillis() + dayMs));

            Butler.data.put("daily", userTimestamp);
            DataParser.writeJSON(Butler.dataPath, Butler.data);

            Levels.addExp(user, 50D);

            final EmbedBuilder dailies = new EmbedBuilder()
                    .setColor(Butler.green)
                    .setDescription("\uD83D\uDCB0 Claimed Dailies! | **+50 xp**")
                    .setFooter("Dailies reset after 24 hours.");
            event.getHook().sendMessageEmbeds(dailies.build()).queue();
        }
    }
}
