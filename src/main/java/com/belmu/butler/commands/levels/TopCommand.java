package com.belmu.butler.commands.levels;

import com.belmu.butler.Butler;
import com.belmu.butler.level.Levels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
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
                    .addChoice("Global", "Global")
                    .addChoice("Server", "Server"),
            new OptionData(OptionType.INTEGER, "page", "The leaderboard page you want to consult", false)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        User user = event.getMember().getUser();

        if(cmd.equals(cmdName)) {
            event.deferReply().queue();

            OptionMapping type = event.getOption("type");

            List keys = switch (type.getAsString().toLowerCase()) {
                case "global" -> new ArrayList(Levels.sortedRanking.keySet());
                case "server" -> new ArrayList(Levels.getGuildSortedRanking(event.getGuild()).keySet());
                default -> null;
            };

            int page;
            try {
                page = event.getOption("page").getAsInt();
                page = page <= 0 ? 1 : page;
            } catch(NullPointerException npe) {
                page = 1;
            }

            StringBuilder leaderboard = new StringBuilder();
            final int maxLineLength = 45;

            for (int i = (10 * (page - 1)) + 1; i <= 10 * page; i++) {
                String text = "";
                String name = "None";
                String prefix = switch (i) {
                    case 1 -> ":first_place:";
                    case 2 -> ":second_place:";
                    case 3 -> ":third_place:";
                    default -> "**[" + i + "]**";
                };

                try {
                    User u = Butler.jda.getUserById(keys.get(i - 1).toString());

                    if (!user.isBot()) {
                        name = u.getName();
                        text = prefix + "%`" + name + "`%" + "[`" + Levels.getXp(u).intValue() + "`]";
                    } else {
                        keys.remove(i);
                    }
                } catch (IndexOutOfBoundsException | NullPointerException exc) {
                    text = prefix + "%`" + name + "`%" + "[`N/A`]";
                }
                leaderboard.append(text.replaceAll("%", addBlank((maxLineLength - name.length()) / 2))).append("\n");
            }

            String rank = "N/A";
            try {
                rank = "#" + Levels.getRank(user, keys);
            } catch(NullPointerException ignored) {}

            String level = String.valueOf(Levels.getLevel(user).intValue());
            int xp = Levels.getXp(user).intValue();

            EmbedBuilder lb = new EmbedBuilder();

            lb.setColor(event.getGuild().getSelfMember().getColor());
            lb.setTitle(":trophy: **" + type.getAsString() + " EXP Leaderboard**");

            lb.addField("Ranking", leaderboard.toString(), false);
            lb.addField(":small_orange_diamond: " + type.getAsString() + " Rank", rank, true);
            lb.addField(":small_orange_diamond: Level", level, true);
            lb.addField(":small_orange_diamond: Total EXP", String.valueOf(xp), true);

            lb.setFooter("Requested by " + user.getName(), user.getAvatarUrl());
            lb.setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(lb.build()).queue();
        }
    }

    public static String addBlank(int number) {
        return String.join("", Collections.nCopies(Math.max(0, number), " "));
    }
}
