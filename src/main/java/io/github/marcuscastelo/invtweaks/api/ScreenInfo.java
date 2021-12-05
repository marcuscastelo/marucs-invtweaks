package io.github.marcuscastelo.invtweaks.api;

import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;
import net.minecraft.screen.ScreenHandler;

public final class ScreenInfo {
    private static final int DEFAULT_PLAYER_MAIN_INV_SIZE = 27;
    private static final int DEFAULT_PLAYER_HOTBAR_SIZE = 9;
    private static final boolean DEFAULT_HAS_PLAYER_MAIN_INV = true;
    private static final boolean DEFAULT_HAS_PLAYER_HOTBAR = true;
    private static final boolean DEFAULT_HAS_EXTERNAL_CONTAINER = true;

    protected final Class<? extends ScreenHandler> handlerClass;
    protected boolean bhasPlayerMainInv;

    public Class<? extends ScreenHandler> getHandlerClass() {
        return handlerClass;
    }

    public boolean hasPlayerMainInv() {
        return bhasPlayerMainInv;
    }

    public boolean hasPlayerHotbar() {
        return bhasPlayerHotbar;
    }

    public boolean hasExternalContainer() {
        return bhasExternalContainer;
    }

    public int getPlayerMainInvSize() {
        return playerMainInvSize;
    }

    public int getPlayerHotbarSize() {
        return playerHotbarSize;
    }

    public int getPlayerInvTotalSize() {
        return playerInvTotalSize;
    }

    public IInvTweaksBehavior getInvTweaksBehavior() {
        return invTweaksBehavior;
    }

    protected boolean bhasPlayerHotbar;
    protected boolean bhasExternalContainer;
    protected int playerMainInvSize, playerHotbarSize, playerInvTotalSize;



    protected IInvTweaksBehavior invTweaksBehavior;


    private ScreenInfo(Class<? extends ScreenHandler> handlerClass) {
        this.handlerClass = handlerClass;
        this.playerMainInvSize = DEFAULT_PLAYER_MAIN_INV_SIZE;
        this.playerHotbarSize = DEFAULT_PLAYER_HOTBAR_SIZE;
        this.bhasPlayerMainInv = DEFAULT_HAS_PLAYER_MAIN_INV;
        this.bhasPlayerHotbar = DEFAULT_HAS_PLAYER_HOTBAR;
        this.bhasExternalContainer = DEFAULT_HAS_EXTERNAL_CONTAINER;
        this.invTweaksBehavior = null;
        this.playerInvTotalSize = -1;
    }

    public static class Builder {
        ScreenInfo si;
        public Builder(Class<? extends ScreenHandler> screenHandlerClass) { si = new ScreenInfo(screenHandlerClass); }
        public Builder withPlayerMainInv(boolean bl) { si.bhasPlayerMainInv = bl; return this; }
        public Builder withPlayerHotbar(boolean bl) { si.bhasPlayerHotbar = bl; return this; }
        public Builder withExternalContainer(boolean bl) { si.bhasExternalContainer = bl; return this; }
        public Builder setPlayerMainInvSize(int size) { si.playerMainInvSize = size; return this; }
        public Builder setPlayerHotbarSize(int size) { si.playerHotbarSize = size; return this; }
        public Builder setPlayerInvTotalSize(int size) { si.playerInvTotalSize = size; return this; }
        public Builder setBehavior(IInvTweaksBehavior behavior) { si.invTweaksBehavior = behavior; return this; }
        public ScreenInfo build() {
            if (si.invTweaksBehavior == null) throw new IllegalStateException("Before building a ScreenInfo, please inform it's behavior");
            if (si.playerInvTotalSize == -1) si.playerInvTotalSize = si.playerMainInvSize + si.playerHotbarSize;
            return si;
        }
    }
}
