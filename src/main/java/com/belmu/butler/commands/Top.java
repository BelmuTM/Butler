package com.belmu.butler.commands;

import com.belmu.butler.Butler;
import com.belmu.butler.level.LevelUtils;
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

public class Top extends ListenerAdapter {

    public String cmdName = "top";
    public String cmdDescription = "Displays XP leaderboard";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.STRING, "type", "The desired type of leaderboard", true)
                    .addChoice("Global", "Global")
                    .addChoice("Server", "Server"),
            new OptionData(OptionType.INTEGER, "page", "The leaderboard page you want to consult", false)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        Member member = event.getMember();
        User user = member.getUser();

        if(cmd.equals(cmdName)) {
            event.deferReply().queue();

            OptionMapping type = event.getOption("type");

            List keys = switch (type.getAsString().toLowerCase()) {
                case "global" -> new ArrayList(LevelUtils.sortedRanking.keySet());
                case "server" -> new ArrayList(LevelUtils.getGuildSortedRanking(event.getGuild()).keySet());
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
                        text = prefix + "%`" + name + "`%" + "[`" + LevelUtils.getXp(u).intValue() + "`]";
                    } else {
                        keys.remove(i);
                    }
                } catch (IndexOutOfBoundsException ioobe) {
                    text = prefix + "%`" + name + "`%" + "[`N/A`]";
                }
                leaderboard.append(text.replaceAll("%", addBlank((maxLineLength - name.length()) / 2))).append("\n");
            }

            String rank = "N/A";
            try {
                rank = "#" + LevelUtils.getRank(user, keys);
            } catch(NullPointerException ignored) {}

            String level = String.valueOf(LevelUtils.getLevel(user).intValue());
            int xp = LevelUtils.getXp(user).intValue();

            EmbedBuilder l = new EmbedBuilder();

            l.setColor(Objects.requireNonNull(event.getGuild().getMemberById(event.getJDA().getSelfUser().getId())).getColor());
            l.setTitle(":trophy: **" + type.getAsString() + " EXP Leaderboard**");

            l.addField("Ranking", leaderboard.toString(), false);

            l.addField(":small_orange_diamond: " + type.getAsString() + " Rank", rank, true);
            l.addField(":small_orange_diamond: Level", level, true);
            l.addField(":small_orange_diamond: Total EXP", String.valueOf(xp), true);

            l.setImage("https://cdn.discordapp.com/attachments/736608562490376202/1049084436295188530/bot_level_card.png");

            l.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());
            l.setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(l.build()).queue();
        }
    }

    public static String addBlank(int number) {
        return String.join("", Collections.nCopies(Math.max(0, number), " "));
    }
}
