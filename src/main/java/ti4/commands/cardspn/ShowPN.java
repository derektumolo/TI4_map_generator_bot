package ti4.commands.cardspn;

import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ti4.commands.CommandHelper;
import ti4.commands.GameStateSubcommand;
import ti4.helpers.Constants;
import ti4.helpers.PromissoryNoteHelper;
import ti4.image.Mapper;
import ti4.map.Game;
import ti4.map.Player;
import ti4.message.MessageHelper;

class ShowPN extends GameStateSubcommand {

    public ShowPN() {
        super(Constants.SHOW_PN, "Show Promissory Note to player", true, true);
        addOptions(new OptionData(OptionType.INTEGER, Constants.PROMISSORY_NOTE_ID, "Promissory Note ID which is found between ()").setRequired(true));
        addOptions(new OptionData(OptionType.STRING, Constants.TARGET_FACTION_OR_COLOR, "Faction or Color").setRequired(true).setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.FACTION_COLOR, "Source faction or color (default is you)").setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.LONG_PN_DISPLAY, "Long promissory display, \"y\" or \"yes\" to enable"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Player player = getPlayer();
        int index = event.getOption(Constants.PROMISSORY_NOTE_ID).getAsInt();
        String pnID = null;
        for (Map.Entry<String, Integer> so : player.getPromissoryNotes().entrySet()) {
            if (so.getValue().equals(index)) {
                pnID = so.getKey();
            }
        }

        if (pnID == null) {
            MessageHelper.sendMessageToEventChannel(event, "No such promissory note ID found, please retry.");
            return;
        }

        OptionMapping longPNOption = event.getOption(Constants.LONG_PN_DISPLAY);
        boolean longPNDisplay = false;
        if (longPNOption != null) {
            longPNDisplay = "y".equalsIgnoreCase(longPNOption.getAsString()) || "yes".equalsIgnoreCase(longPNOption.getAsString());
        }

        Game game = getGame();
        Player targetPlayer = CommandHelper.getOtherPlayerFromEvent(game, event);
        if (targetPlayer == null) {
            MessageHelper.replyToMessage(event, "Unable to determine who the target player is.");
            return;
        }

        MessageEmbed pnEmbed = Mapper.getPromissoryNote(pnID).getRepresentationEmbed(!longPNDisplay, false, false);
        player.setPromissoryNote(pnID);


        if (game.isFowMode())
        {
            MessageHelper.sendMessageToEventChannel(event, "Promissory note has been shown.");
        }
        else
        {
            MessageHelper.sendMessageToEventChannel(event, player.getRepresentation()
                + " has shown a promissory note to " + targetPlayer.getRepresentation() + ".");
        }
        String message = player.getRepresentation(false, false) + " has shown you a promissory note:";
        PromissoryNoteHelper.sendPromissoryNoteInfo(game, player, longPNDisplay);
        MessageHelper.sendMessageEmbedsToCardsInfoThread(targetPlayer, message, List.of(pnEmbed));
    }
}
