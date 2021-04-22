package io.github.marcuscastelo.invtweaks.api;

import io.github.marcuscastelo.invtweaks.client.behavior.IInvTweaksBehavior;
import net.minecraft.screen.ScreenHandler;

public final class ScreenSpecification {
    protected final Class<? extends ScreenHandler> handlerClass;
    public Class<? extends ScreenHandler> getHandlerClass() {
        return handlerClass;
    }

    public IInvTweaksBehavior getInvTweaksBehavior() {
        return invTweaksBehavior;
    }

    protected IInvTweaksBehavior invTweaksBehavior;
    protected ScreenInventoriesSpecification inventoriesSpecification;

    private ScreenSpecification(Class<? extends ScreenHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    public static class Builder {
        ScreenSpecification si;
        public Builder(Class<? extends ScreenHandler> screenHandlerClass) { si = new ScreenSpecification(screenHandlerClass); }

        public Builder withInventoriesSpecification(ScreenInventoriesSpecification screenInventoriesSpecification) { si.inventoriesSpecification = screenInventoriesSpecification; return this; }
        public Builder withBehavior(IInvTweaksBehavior behavior) { si.invTweaksBehavior = behavior; return this; }
        public ScreenSpecification build() {
            if (si.invTweaksBehavior == null) throw new IllegalStateException("Before building a ScreenInfo, please inform it's behavior");
            if (si.inventoriesSpecification == null) si.inventoriesSpecification = ScreenInventoriesSpecification.DEFAULT;
            return si;
        }
    }
}
