package ch.frily.yubot.container;

import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

public class RulesContainer extends Container {

    public RulesContainer() {
        this.addComponent(TextDisplay.of("## Regelwerk"));
        this.addComponent(TextDisplay.of("-# *Vom 16. Januar 2026 - Stand 11. Juni 2026*"));
    }
}
