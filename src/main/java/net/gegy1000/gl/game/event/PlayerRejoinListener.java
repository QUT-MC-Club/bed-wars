package net.gegy1000.gl.game.event;

import net.gegy1000.gl.game.Game;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerRejoinListener {
    EventType<PlayerRejoinListener> EVENT = EventType.create(PlayerRejoinListener.class, listeners -> {
        return (game, player) -> {
            for (PlayerRejoinListener listener : listeners) {
                listener.onRejoin(game, player);
            }
        };
    });

    void onRejoin(Game game, ServerPlayerEntity player);
}