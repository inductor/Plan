package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Processor Class for KillEvent information when the killer is a
 * player.
 * <p>
 * Adds PlayerKill or a Mob kill to the active Session.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class KillProcessor extends PlayerProcessor {

    private final LivingEntity dead;
    private final String weaponName;
    private final long time;

    /**
     * Constructor.
     *
     * @param uuid       UUID of the killer.
     * @param time       Epoch ms the event occurred.
     * @param dead       Dead entity (Mob or Player)
     * @param weaponName Weapon used.
     */
    public KillProcessor(UUID uuid, long time, LivingEntity dead, String weaponName) {
        super(uuid);
        this.time = time;
        this.dead = dead;
        this.weaponName = weaponName;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();

        Plan plugin = Plan.getInstance();

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        if (!cachedSession.isPresent()) {
            return;
        }
        Session session = cachedSession.get();

        if (dead instanceof Player) {
            Player deadPlayer = (Player) dead;
            session.playerKilled(new PlayerKill(deadPlayer.getUniqueId(), weaponName, time));
        } else {
            session.mobKilled();
        }
    }
}
