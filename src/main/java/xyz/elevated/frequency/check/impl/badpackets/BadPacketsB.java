package xyz.elevated.frequency.check.impl.badpackets;

import xyz.elevated.frequency.FrequencyPlugin;
import xyz.elevated.frequency.check.CheckData;
import xyz.elevated.frequency.check.type.PostCheck;
import xyz.elevated.frequency.data.PlayerData;
import xyz.elevated.frequency.wrapper.impl.client.WrappedPlayInArmAnimation;

@CheckData(name = "BadPackets (B)")
public final class BadPacketsB extends PostCheck {

    public BadPacketsB(final PlayerData playerData) {
        super(playerData, WrappedPlayInArmAnimation.class);
    }

    @Override
    public void process(final Object object) {
        if (!FrequencyPlugin.getFrequencyConfig().getBoolean("checks.badpackets.protocol.check_protocol1") &&
                !FrequencyPlugin.getFrequencyConfig().getBoolean("checks.badpackets.enable")) {
            return;
        }
        final boolean post = this.isPost(object);

        if (post) fail();
    }
}
