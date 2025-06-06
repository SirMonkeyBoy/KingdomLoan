package com.github.sirmonkeyboy.loan.Utils;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    public CooldownManager(long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000;
    }

    public boolean isOnCooldown(UUID playerId) {
        Long expiresAt = cooldowns.get(playerId);
        return expiresAt != null && System.currentTimeMillis() < expiresAt;
    }

    public long getRemainingTime(UUID playerId) {
        Long expiresAt = cooldowns.get(playerId);
        if (expiresAt == null) return 0;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public void startCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownMillis);
    }

    public void clearCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }
}