package com.belmu.butler.commands.levels;

import com.belmu.butler.Butler;
import com.belmu.butler.level.Levels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

public class TopCommand extends ListenerAdapter {

    public String cmdName = "top";
    public String cmdDescription = "Displays the EXP leaderboard";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.STRING, "type", "The desired type of leaderboard", true)
                    .addChoice("Global", "global")
                    .addChoice("Server", "server"),
            new OptionData(OptionType.INTEGER, "page", "The leaderboard page you want to consult", false)
    };

    private final String[] prefixes = { ":first_place:", ":second_place:", ":third_place:", "**[%]**" };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();

        if(event.getName().equals(cmdName)) {
            event.deferReply().queue();

            OptionMapping type = event.getOption("type");
            assert type != null;

            ArrayList<String> uuidList = switch (type.getAsString().toLowerCase()) {
                case "global" -> new ArrayList(Levels.globalRanking.keySet());
                case "server" -> new ArrayList(Levels.getGuildSortedRanking(Objects.requireNonNull(event.getGuild())).keySet());
                default -> new ArrayList<>();
            };

            int page;
            try {
                page = Math.max(1, Objects.requireNonNull(event.getOption("page")).getAsInt());
            } catch(NullPointerException npe) {
                page = 1;
            }

            StringBuilder leaderboard = new StringBuilder();
            final int maxLineLength = 45;

            for (int i = (10 * (page - 1)) + 1; i <= 10 * page; i++) {
                String text   = "";
                String name   = "None";
                String prefix = prefixes[Math.min(3, i - 1)].replace("%", Integer.toString(i));

                try {
                    User u = Butler.jda.getUserById(uuidList.get(i - 1));

                    if (!user.isBot()) {
                        name = u.getName();
                        text = prefix + "%`" + name + "`%" + "[`" + Levels.getXp(u).intValue() + "`]";
                    } else {
                        uuidList.remove(i);
                    }
                } catch (IndexOutOfBoundsException | NullPointerException exc) {
                    text = prefix + "%`" + name + "`%" + "[`N/A`]";
                }

                String blanks = String.join("", Collections.nCopies(Math.max(0, (int) Math.round((maxLineLength - name.length()) * 0.5)), " "));
                leaderboard.append(text.replaceAll("%", blanks)).append("\n");
            }

            String rank  = "#" + Levels.getRank(user, uuidList);
            String level = String.valueOf(Levels.getLevel(user));
            int    xp    = Levels.getXp(user).intValue();

            EmbedBuilder lb = new EmbedBuilder()
                .setColor(Objects.requireNonNull(event.getGuild()).getSelfMember().getColor())
                .setTitle(":trophy: **" + type.getAsString() + " EXP Leaderboard**")

                .addField("Ranking", leaderboard.toString(), false)
                .addField(":small_orange_diamond: " + type.getAsString() + " Rank", rank, true)
                .addField(":small_orange_diamond: Level", level, true)
                .addField(":small_orange_diamond: Total EXP", String.valueOf(xp), true)

                .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                .setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(lb.build()).queue();
        }
    }
}
