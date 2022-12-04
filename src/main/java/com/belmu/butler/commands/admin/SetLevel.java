package com.belmu.butler.commands.admin;

import com.belmu.butler.level.LevelUtils;
import com.belmu.butler.util.CooldownMessages;
import com.belmu.butler.util.EmbedTemplates;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class SetLevel extends ListenerAdapter {

    public String cmdName = "setlevel";
    public String cmdDescription = "Sets a user's level to the specified value";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.USER, "user", "The targeted user", true),
            new OptionData(OptionType.INTEGER, "value", "The level you want the user to acquire", true)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        Member member = event.getMember();

        if(cmd.equals(cmdName)) {
            if(member.getId().equals("160421207399858176")) {
                Member target = event.getOption("user").getAsMember();
                User user = target.getUser();

                if(user.isBot()) {
                    CooldownMessages.a(event, EmbedTemplates.robots(member));
                    return;
                }

                double level = event.getOption("value").getAsDouble();
                LevelUtils.setLevel(user, level);
                LevelUtils.setXp(user, LevelUtils.calcXpForLevel(level));

                event.reply(":white_check_mark: Successfully set " + target.getAsMention() + "'s Level to `" + level + "`").queue();
                return;
            }
            CooldownMessages.a(event, EmbedTemplates.perms(member));
        }
    }
}
