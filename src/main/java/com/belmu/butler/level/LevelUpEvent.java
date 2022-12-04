package com.belmu.butler.level;

import com.belmu.butler.util.EmbedTemplates;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LevelUpEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            Member member = event.getMember();
            User user = member.getUser();
            if (!user.isBot()) {
                if(LevelUtils.hasPassedALevel(user)) {
                    double newLvl = LevelUtils.getLevel(user) + 1D;

                    LevelUtils.setLevel(user, newLvl);
                    event.getChannel().sendMessageEmbeds(EmbedTemplates.levelUp(member, newLvl)).queueAfter(2, TimeUnit.SECONDS);
                }
            }
        } catch(NullPointerException npe) { npe.printStackTrace(); }
    }
}
