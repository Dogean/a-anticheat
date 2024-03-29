package xyz.elevated.frequency.data.impl;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.elevated.frequency.data.PlayerData;
import xyz.elevated.frequency.observable.Observable;
import xyz.elevated.frequency.util.EvictingList;
import xyz.elevated.frequency.util.MathUtil;

import java.util.List;

@Getter
public final class ActionManager {
    private final PlayerData playerData;

    public ActionManager(PlayerData playerData) {
        this.playerData = playerData;
    }
    private final EvictingList<Integer> clicks = new EvictingList<>(10);

    /*
    We're using observables so we don't reset variables all the time which hogs performance
     */
    private final Observable<Boolean> placing = new Observable<>(false);
    private final Observable<Boolean> attacking = new Observable<>(false);
    private final Observable<Boolean> swinging = new Observable<>(false);
    private final Observable<Boolean> digging = new Observable<>(false);
    private final Observable<Boolean> delayed = new Observable<>(false);
    private final Observable<Boolean> teleported = new Observable<>(false);
    private final Observable<Boolean> steer = new Observable<>(false);
    private final Observable<Boolean> packetDigging = new Observable<>(false);

    private int lastAttack = 0, lastDig = 0, lastFlying = 0,
            lastDelayedFlying = 0, lastTeleport = 0, movements = 0, lastPlace = 0;

    public boolean isFlying() {
        if (lastDelayedFlying == 0 && lastFlying == 0 && movements >= 7) {
            return true;
        }
        return false;
    }
    public void onArmAnimation() {
        this.swinging.set(true);

        click: {
            if (digging.get() || movements > 5) break click;

            clicks.add(movements);
        }

        if (clicks.size() > 5) {
            final double cps = MathUtil.getCps(clicks);
            final double rate = cps * movements;

            playerData.getCps().set(cps);
            playerData.getRate().set(rate);
        }

        movements = 0;
    }

    public void onAttack() {
        this.attacking.set(true);

        this.lastAttack = playerData.getTicks().get();
    }

    public void onPlace() {
        this.placing.set(true);

        this.lastPlace = playerData.getTicks().get();
    }

    public void onDig() {
        this.packetDigging.set(true);
        
        this.lastDig = playerData.getTicks().get();
    }

    public void onFlying() {
        final int now = playerData.getTicks().get();
        final int attack = now - lastAttack;

        final boolean delayed = now - lastFlying > 2;
        final boolean digging = now - lastDig < 15 || packetDigging.get();
        final boolean lagging = now - lastDelayedFlying < 2;
        final boolean teleporting = now - lastTeleport < 2;
        final boolean recent = attack < 200;

        this.placing.set(false);
        this.attacking.set(false);
        this.swinging.set(false);
        this.attacking.set(false);
        this.steer.set(false);
        this.packetDigging.set(false);

        this.digging.set(digging);
        this.delayed.set(lagging);
        this.teleported.set(teleporting);

        this.lastDelayedFlying = delayed ? now : lastDelayedFlying;
        this.lastFlying = now;

        playerData.getTarget().set(recent ? playerData.getTarget().get() : null);
        playerData.getTicks().set(now + 1);

        movements++;
    }

    public void onSteerVehicle() {
        this.steer.set(true);
    }

    public void onTeleport() {
        this.lastTeleport = playerData.getTicks().get();
    }

    public void onBukkitDig() {
        this.lastDig = playerData.getTicks().get();
    }

    public Observable<Boolean> getDelayed() {
        return delayed;
    }

    public Observable<Boolean> getTeleported() {
        return teleported;
    }

    public int getLastAttack() {
        return lastAttack;
    }

    public Observable<Boolean> getAttacking() {
        return attacking;
    }

    public int getLastPlace() {
        return lastPlace;
    }

    public Observable<Boolean> getDigging() {
        return digging;
    }

    public Observable<Boolean> getPlacing() {
        return placing;
    }

    public Observable<Boolean> getSteer() {
        return steer;
    }

    public Observable<Boolean> getSwinging() {
        return swinging;
    }
}
