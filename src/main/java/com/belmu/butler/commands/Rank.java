package com.belmu.butler.commands;

import com.belmu.butler.level.LevelUtils;
import com.belmu.butler.util.CooldownMessages;
import com.belmu.butler.util.EmbedTemplates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;

public class Rank extends ListenerAdapter {

    public String cmdName = "rank";
    public String cmdDescription = "Gives you a user's levelling statistics";

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
                CooldownMessages.a(event, EmbedTemplates.robots(member));
                return;
            }

            String globalRank = "N/A";
            try {
                globalRank = "#" + LevelUtils.getRank(user, new ArrayList(LevelUtils.sortedRanking.keySet()));
            } catch(NullPointerException ignored) {}

            String serverRank = "N/A";
            try {
                serverRank = "#" + LevelUtils.getRank(user, new ArrayList(LevelUtils.getGuildSortedRanking(event.getGuild()).keySet()));
            } catch(NullPointerException ignored) {}

            int lvl     = LevelUtils.getLevel(user).intValue();
            int nextLvl = lvl + 1;
            String level = String.valueOf(lvl);
            String nextLevel = String.valueOf(lvl + 1);

            int xp = LevelUtils.getXp(user).intValue();

            EmbedBuilder r = new EmbedBuilder();

            r.setColor(target.getColor());
            r.setTitle(":bar_chart: **" + target.getEffectiveName() + "**'s Rank");

            r.addField(":small_orange_diamond: Global", globalRank, true);
            r.addField(":small_orange_diamond: Server", serverRank, true);
            r.addField(":small_orange_diamond: Level", level, true);
            r.addField(":small_orange_diamond: Total EXP", String.valueOf(xp), true);

            r.setThumbnail(target.getUser().getAvatarUrl());

            r.setFooter("Requested by " + member.getUser().getAsTag(), member.getUser().getAvatarUrl());
            r.setTimestamp(Instant.now());

            Double currentXp = lvl == 0 ? 0D : ((LevelUtils.calcXpForLevel(lvl)) - (LevelUtils.getXp(user))) - (((LevelUtils.calcXpForLevel(lvl)) - (LevelUtils.getXp(user))) * 2);
            Double xpForNext = (LevelUtils.calcXpForLevel(nextLvl)) - (LevelUtils.calcXpForLevel(lvl));

            String actualXpFormat = String.valueOf(currentXp.intValue());
            String xpForNextFormat = String.valueOf(xpForNext.intValue());

            Double percentage = calculatePercentage(currentXp, xpForNext);
            double tenth = percentage * 0.1;

            StringBuilder b = new StringBuilder();
            for (double a = 0; a < 10; a++) {
                b.append(" ").append(a <= tenth ? ":green_square:" : ":white_large_square:").append(" ");
            }

            r.addField("Progress", "`" + level + "` " + b.toString().trim() + " `" + nextLevel + "` " + "**" + percentage.intValue() + "%**" + " *(" + actualXpFormat + "/" + xpForNextFormat + ")*", false);

            event.getHook().sendMessageEmbeds(r.build()).queue();
        }
    }

    public double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }
}
