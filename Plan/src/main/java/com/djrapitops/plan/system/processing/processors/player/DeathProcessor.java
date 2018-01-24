package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;

import java.util.Optional;
import java.util.UUID;

/**
 * Updates death count of the current session.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class DeathProcessor extends PlayerProcessor {

    /**
     * Constructor.
     *
     * @param uuid UUID of the dead player.
     */
    public DeathProcessor(UUID uuid) {
        super(uuid);
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(Session::died);
    }
}
