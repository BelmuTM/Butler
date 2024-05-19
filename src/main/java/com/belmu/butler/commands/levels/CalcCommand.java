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
import java.util.Objects;

public class CalcCommand extends ListenerAdapter {

    public String cmdName = "calc";
    public String cmdDescription = "Calculates the amount of EXP required to reach a desired level";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.INTEGER, "level", "The desired level", true)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        User   user   = event.getUser();

        if(event.getName().equals(cmdName)) {
            event.deferReply().queue();

            int    level  = Objects.requireNonNull(event.getOption("level")).getAsInt();
            Double xpFor  = Levels.calculateXp(level);
            Double xpFrom = Levels.calculateXp(level - 1);
            int    xpGap  = (int) (xpFor - xpFrom);

            EmbedBuilder calc = new EmbedBuilder();

            assert member != null;
            calc.setColor(member.getColor());

            calc.addField(
                    ":man_teacher: **XP calculator**",
                    ":small_orange_diamond: Needed for lvl **" + level + "** ⟶ `" + xpFor.intValue() + "`\n"
                     + ":small_orange_diamond: Needed from lvl **" + (level - 1) + "** to lvl **" + level + "** ⟶ `" + xpGap + "`",
                    true);

            calc.setFooter("Requested by " + member.getEffectiveName(), user.getAvatarUrl());
            calc.setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(calc.build()).queue();
        }
    }
}
