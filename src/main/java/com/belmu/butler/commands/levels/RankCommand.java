package com.belmu.butler.commands.levels;

import com.belmu.butler.Butler;
import com.belmu.butler.level.Levels;
import com.belmu.butler.utility.CooldownMessages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;

public class RankCommand extends ListenerAdapter {

    public String cmdName = "rank";
    public String cmdDescription = "Displays someone's rank card";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.USER, "user", "The targeted user", false)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        Member member = event.getMember();

        if(cmd.equals(cmdName)) {
            event.deferReply().queue();

            Member target;
            try {
                target = event.getOption("user").getAsMember();
            } catch(NullPointerException npe) {
                target = member;
            }
            User user = target.getUser();

            if(user.isBot()) {
                EmbedBuilder bot = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setTitle(":robot: **Error.**")
                        .setDescription(member.getAsMention() + " The user your entered is a bot.");
                CooldownMessages.send(event, bot.build());
                return;
            }

            String globalRank = "N/A";
            try {
                globalRank = "#" + Levels.getRank(user, new ArrayList(Levels.sortedRanking.keySet()));
            } catch(NullPointerException ignored) {}

            String serverRank = "N/A";
            try {
                serverRank = "#" + Levels.getRank(user, new ArrayList(Levels.getGuildSortedRanking(event.getGuild()).keySet()));
            } catch(NullPointerException ignored) {}

            int lvl     = Levels.getLevel(user).intValue();
            int nextLvl = lvl + 1;
            String level = String.valueOf(lvl);
            String nextLevel = String.valueOf(lvl + 1);

            Double xpDouble = Levels.getXp(user);
            int xp = xpDouble.intValue();

            Double currentXp = (Levels.calcXpForLevel(lvl) - Levels.getXp(user)) - (Levels.calcXpForLevel(lvl) - xpDouble) * 2;
            Double xpForNext = Levels.calcXpForLevel(nextLvl) - Levels.calcXpForLevel(lvl);

            Double percentage = calculatePercentage(currentXp, xpForNext);
            double tenth = percentage * 0.1;

            StringBuilder bar = new StringBuilder();
            for (double i = 0; i < 10; i++) {
                bar.append(i < tenth ? ":green_square:" : ":white_large_square:").append(" ");
            }

            final EmbedBuilder rank = new EmbedBuilder();

            rank.setColor(target.getColor());
            rank.setTitle(":bar_chart: **" + target.getEffectiveName() + "**'s Rank");

            rank.addField(":small_orange_diamond: Global", globalRank, true);
            rank.addField(":small_orange_diamond: Server", serverRank, true);
            rank.addField(":small_orange_diamond: Total EXP", String.valueOf(xp), true);

            rank.setThumbnail(user.getAvatarUrl());
            rank.setFooter("Requested by " + member.getEffectiveName(), member.getUser().getAvatarUrl());
            rank.setTimestamp(Instant.now());

            rank.addField("Progress", "`[" + level + "]` " + bar.toString().trim() + " `[" + nextLevel + "]` " + "**" + percentage.intValue() + "%**" + " *(" + currentXp.intValue() + "/" + xpForNext.intValue() + ")*", false);

            event.getHook().sendMessageEmbeds(rank.build()).queue();
        }
    }

    public double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }
}
