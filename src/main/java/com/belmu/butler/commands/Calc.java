package com.belmu.butler.commands;

import com.belmu.butler.level.LevelUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class Calc extends ListenerAdapter {

    public String cmdName = "calc";
    public String cmdDescription = "Calculates the amount of xp required to reach a desired level";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.INTEGER, "level", "The desired level", true)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        Member member = event.getMember();
        User user = event.getUser();

        if(cmd.equals(cmdName)) {
            event.deferReply().queue();

            Double level  = event.getOption("level").getAsDouble();
            Double xpFor  = LevelUtils.calcXpForLevel(level);
            Double xpFrom = LevelUtils.calcXpForLevel(level - 1);
            Double xpGap  = xpFor - xpFrom;

            EmbedBuilder c = new EmbedBuilder();

            c.setColor(member.getColor());

            c.addField(
                    ":man_teacher: **XP calculator**",
                    ":small_orange_diamond: Needed for lvl **" + level.intValue() + "** ⟶ `" + xpFor.intValue() + "`\n"
                     + ":small_orange_diamond: Needed from lvl **" + (level.intValue() - 1) + "** to lvl **" + level.intValue() + "** ⟶ `" + xpGap.intValue() + "`",
                    true);

            c.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());
            c.setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(c.build()).queue();
        }
    }
}
