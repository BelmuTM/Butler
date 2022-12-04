package com.belmu.butler.util;

import com.belmu.butler.Butler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.text.DecimalFormat;

public class EmbedTemplates {

    public static MessageEmbed perms(Member author) {
        EmbedBuilder perms = new EmbedBuilder();

        perms.setColor(Butler.darkGray);
        perms.setTitle(":gear: **Missing permissions.**");
        perms.setDescription(author.getAsMention() + " Looks like you do not have the permissions to do that.");

        return perms.build();
    }

    public static MessageEmbed limit(Member author) {
        EmbedBuilder li = new EmbedBuilder();

        li.setColor(Butler.darkGray);
        li.setTitle(":page_facing_up: **Limit Exceeded!**");
        li.setDescription(author.getAsMention() + " You can only delete between 1 & 100 messages.");

        return li.build();
    }

    public static MessageEmbed robots(Member author) {
        EmbedBuilder h = new EmbedBuilder();

        h.setColor(Butler.darkGray);
        h.setTitle(":robot: **Error.**");
        h.setDescription(author.getAsMention() + " Bots can't level up.");

        return h.build();
    }

    public static MessageEmbed levelUp(Member member, double lvl) {
        DecimalFormat formatter = new DecimalFormat("#");
        String level = formatter.format(lvl);

        EmbedBuilder up = new EmbedBuilder();

        up.setColor(member.getColor());
        up.setTitle(":green_circle: **Level Up!**");
        up.setDescription(member.getAsMention() + " Achieved Level " + level);
        up.setThumbnail(member.getUser().getAvatarUrl());

        return up.build();
    }

    public static MessageEmbed missing(Member author, String action) {
        EmbedBuilder h = new EmbedBuilder();

        h.setColor(Butler.darkGray);
        h.setTitle(":lock: **Missing Permissions.**");
        h.setDescription(author.getAsMention() + " I do not have the permissions to " + action + ".");

        return h.build();
    }
}
