package commands;

import bot.NASABot;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Objects;

public class SetPostChannel extends NASACommand {

    public SetPostChannel() {
        this.name = "setPostChannel";
        this.help = "Sets the Post Channel for the server.";
        this.arguments = "<#channelMention>";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        this.insertCommand(commandEvent);

        if (!commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && !Objects.equals(commandEvent.getGuild().getOwner(), commandEvent.getMember())) {
            commandEvent.replyError("Only server administrators or the server owner may use this command.");
            return;
        }

        if (NASABot.dbClient.getPostChannelForServer(commandEvent.getGuild().getId()) != null) {
            try {
                TextChannel textChannel = commandEvent.getGuild().getTextChannelById(NASABot.dbClient.getPostChannelForServer(commandEvent.getGuild().getId()));
                commandEvent.replyError(String.format("This server is already using %s as the Post Channel. Please clear " +
                        "it before setting a new one with the removePostChannel command.", Objects.requireNonNull(textChannel).getAsMention()));
                return;
            } catch (Exception e) {
                commandEvent.replyError("There is already a Post Channel set for this server, but the bot does not have permission to view it. " +
                        "Please clear the current Post Channel before setting a new one with the removePostChannel command.");
                return;
            }

        }

        if (commandEvent.getMessage().getMentions().getChannels().size() > 0) {
            GuildChannel textChannel = commandEvent.getMessage().getMentions().getChannels().get(0);
            if (!textChannel.getType().equals(ChannelType.TEXT)) {
                commandEvent.replyError(String.format("No channels were mentioned, or the bot does not have permission to view the " +
                        "mentioned channel. Please check your permission settings or command formatting: %s", this.getArgumentsString()));
                return;
            }
            if (NASABot.dbClient.createPostChannel(commandEvent.getGuild().getId(), textChannel.getId())) {
                int postChannelId = NASABot.dbClient.getPostChannelId(commandEvent.getGuild().getId());
                if (postChannelId != -1) {
                    if (NASABot.dbClient.createPostChannelConfiguration(postChannelId)) {
                        commandEvent.reply(String.format("%s has been set as the Post Channel for this server.", textChannel.getAsMention()));
                    } else {
                        commandEvent.reply("Unable to set Post Channel Configuration. Please contact the bot owner or join the NASABot Discord channel to report this error.");
                    }
                } else {
                    commandEvent.reply("Unable to get Post Channel ID. Please contact the bot owner or join the NASABot Discord channel to report this error.");
                }
            }
        } else {
            commandEvent.replyError(String.format("No channels were mentioned, or the bot does not have permission to view the " +
                    "mentioned channel. Please check your permission settings or command formatting: %s", this.getArgumentsString()));
        }
    }
}
