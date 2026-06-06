package herr0w.aotomod.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class DiscordInteractionListener extends ListenerAdapter {
    private final DiscordBotManager botManager;

    public DiscordInteractionListener(DiscordBotManager botManager) {
        this.botManager = botManager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        botManager.handleButton(event);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        botManager.handleModal(event);
    }
}
