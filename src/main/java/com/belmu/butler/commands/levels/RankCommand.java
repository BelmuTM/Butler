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
import java.util.Objects;

public class RankCommand extends ListenerAdapter {

    public String cmdName = "rank";
    public String cmdDescription = "Displays someone's rank card";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.USER, "user", "The targeted user", false)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        assert member != null;

        if(event.getName().equals(cmdName)) {
            event.deferReply().queue();

            Member target;
            try {
                target = Objects.requireNonNull(event.getOption("user")).getAsMember();
            } catch(NullPointerException npe) {
                target = member;
            }
            assert target != null;
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
                globalRank = "#" + Levels.getRank(user, new ArrayList(Levels.globalRanking.keySet()));
            } catch(NullPointerException ignored) {}

            String serverRank = "N/A";
            try {
                serverRank = "#" + Levels.getRank(user, new ArrayList(Levels.getGuildSortedRanking(Objects.requireNonNull(event.getGuild())).keySet()));
            } catch(NullPointerException ignored) {}

            int level     = Levels.getLevel(user);
            int nextLevel = level + 1;

            Double xpDouble = Levels.getXp(user);
            int xp = xpDouble.intValue();

            double currentXp = Levels.getXp(user) - Levels.calculateXp(level);
            double xpForNext = Levels.calculateXp(nextLevel) - Levels.calculateXp(level);

            double percentage = currentXp * 100 / xpForNext;
            double tenth      = percentage * 0.1;

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

            rank.addField("Progress", "`[" + level + "]` " + bar.toString().trim() + " `[" + nextLevel + "]` " + "**" +
                    (int) percentage + "%**" + " *(" + (int) currentXp + "/" + (int) xpForNext + ")*", false);

            event.getHook().sendMessageEmbeds(rank.build()).queue();
        }
    }
}
