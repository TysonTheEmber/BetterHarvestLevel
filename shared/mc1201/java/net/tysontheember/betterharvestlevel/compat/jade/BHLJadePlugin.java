package net.tysontheember.betterharvestlevel.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("")
public class BHLJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // BHL's ItemStack.isCorrectToolForDrops mixin makes Jade's native
        // harvest tool display reflect BHL's tier system automatically.
    }
}
