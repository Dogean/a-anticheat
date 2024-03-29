package xyz.elevated.frequency.check.impl.fly;

import org.bukkit.Location;
import xyz.elevated.frequency.FrequencyPlugin;
import xyz.elevated.frequency.check.CheckData;
import xyz.elevated.frequency.check.type.PositionCheck;
import xyz.elevated.frequency.data.PlayerData;
import xyz.elevated.frequency.exempt.type.ExemptType;
import xyz.elevated.frequency.update.PositionUpdate;

@CheckData(name = "Fly (B)")
public final class FlyB extends PositionCheck {

    private double lastDeltaY = 0.0d, buffer = 0.0d;
    private int ticks = 0;

    public FlyB(final PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void process(final PositionUpdate positionUpdate) {
        if (!FrequencyPlugin.getFrequencyConfig().getBoolean("checks.fly.enable") && !FrequencyPlugin.getFrequencyConfig()
                .getBoolean("checks.fly.modules.v_fly.check_protocol1")) {
            return;
        }
        final Location from = positionUpdate.getFrom();
        final Location to = positionUpdate.getTo();

        final double deltaY = to.getY() - from.getY();
        final double estimation = (lastDeltaY - 0.08) * 0.9800000190734863;

        final boolean resetting = Math.abs(deltaY) + 0.0980000019 < 0.05;
        final boolean exempt = this.isExempt(ExemptType.TELEPORTING, ExemptType.VELOCITY);

        final boolean touchingAir = playerData.getPositionManager().getTouchingAir().get();

        if (exempt || resetting) return;

        if (touchingAir) {
            ++ticks;

            if (ticks > 5 && Math.abs(estimation - deltaY) > 0.01) {
                buffer += 1.5;

                if (buffer > 5) {
                    fail("tried to H-Fly, motY=(" + Math.abs(estimation - deltaY) + "), deltaY=(" + deltaY + "), estimation=(" + estimation + "), ticks=(" +
                            ticks + "), buffer=(" + buffer + ")");
                    lagback(FrequencyPlugin.getFrequencyConfig().getBoolean("checks.fly.lagback"));
                }
            } else {
                buffer = Math.max(0, buffer - 1.25);
            }
        } else {
            ticks = 0;
        }

        this.lastDeltaY = deltaY;
    }
}
