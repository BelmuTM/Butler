package com.belmu.butler.commands.admin;

import com.belmu.butler.level.Levels;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SetLevelCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");

        if(args[0].equals("setlevel")) {
            if(event.getMember().getId().equals("160421207399858176")) {
                User user = event.getMessage().getMentions().getUsers().get(0);

                if(user.isBot()) {
                    event.getChannel().sendMessage("Error: user is a bot");
                    return;
                }

                int level = Integer.parseInt(args[2]);
                Levels.setLevel(user, level);

                event.getChannel().sendMessage(":white_check_mark: Successfully set " + user.getAsTag() + "'s Level to `" + level + "`").queue();
            }
        }
    }
}
