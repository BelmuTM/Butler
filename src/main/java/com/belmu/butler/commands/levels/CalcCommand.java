package com.belmu.butler.commands.levels;

import com.belmu.butler.level.Levels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class CalcCommand extends ListenerAdapter {

    public String cmdName = "calc";
    public String cmdDescription = "Calculates the amount of EXP required to reach a desired level";

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
            Double xpFor  = Levels.calcXpForLevel(level);
            Double xpFrom = Levels.calcXpForLevel(level - 1);
            Double xpGap  = xpFor - xpFrom;

            EmbedBuilder calc = new EmbedBuilder();

            calc.setColor(member.getColor());

            calc.addField(
                    ":man_teacher: **XP calculator**",
                    ":small_orange_diamond: Needed for lvl **" + level.intValue() + "** ⟶ `" + xpFor.intValue() + "`\n"
                     + ":small_orange_diamond: Needed from lvl **" + (level.intValue() - 1) + "** to lvl **" + level.intValue() + "** ⟶ `" + xpGap.intValue() + "`",
                    true);

            calc.setFooter("Requested by " + member.getEffectiveName(), user.getAvatarUrl());
            calc.setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(calc.build()).queue();
        }
    }
}
