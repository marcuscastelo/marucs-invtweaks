package io.github.marcuscastelo.invtweaks.api;

import io.github.marcuscastelo.invtweaks.behavior.IInvTweaksBehavior;
import net.minecraft.screen.ScreenHandler;

public final class ScreenSpecification {
    public IInvTweaksBehavior getInvTweaksBehavior() {
        return invTweaksBehavior;
    }

    public ScreenInventoriesSpecification getInventoriesSpecification() {
        return inventoriesSpecification;
    }

    protected IInvTweaksBehavior invTweaksBehavior;
    protected ScreenInventoriesSpecification inventoriesSpecification;

    private ScreenSpecification() {

    }

    public static class Builder {
        ScreenSpecification si;
        public Builder() { si = new ScreenSpecification(); }

        public Builder withInventoriesSpecification(ScreenInventoriesSpecification screenInventoriesSpecification) { si.inventoriesSpecification = screenInventoriesSpecification; return this; }
        public Builder withBehavior(IInvTweaksBehavior behavior) { si.invTweaksBehavior = behavior; return this; }
        public ScreenSpecification build() {
            if (si.invTweaksBehavior == null) throw new IllegalStateException("Before building a ScreenInfo, please inform it's behavior");
            if (si.inventoriesSpecification == null) si.inventoriesSpecification = ScreenInventoriesSpecification.DEFAULT;
            return si;
        }
    }
}
