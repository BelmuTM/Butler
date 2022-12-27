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

                if(Long.parseLong(userDailies.get(user.getId()).toString()) > System.currentTimeMillis()) {
                    final EmbedBuilder alreadyClaimed = new EmbedBuilder()
                            .setColor(Butler.red)
                            .setDescription("⌛ You've already claimed your dailies!")
                            .setFooter("Come back after 12PM UTC");
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
                    .setFooter("Dailies reset at 12PM UTC");
            event.getHook().sendMessageEmbeds(dailies.build()).queue();
        }
    }
}
