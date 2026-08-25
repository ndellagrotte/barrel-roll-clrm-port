package io.github.ndellagrotte.doabarrelroll.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loading plugin that registers the mod's mixin config early enough to matter.
 *
 * <p>The jar carries the {@code FMLCorePlugin} manifest attribute so FML discovers this class during
 * coremod discovery rather than at {@code LoaderState.CONSTRUCTING} — by which point the vanilla
 * targets (Entity, EntityLivingBase, EntityPlayer, EntityPlayerSP, EntityRenderer) are already
 * loaded and can no longer be transformed.
 *
 * <p>Registration goes through MixinBooter's {@link IEarlyMixinLoader}, which is honoured on both
 * supported loaders: on MinecraftForge by MixinBooter itself, and on Cleanroom by
 * {@code CleanMixHooks.loadMixinBooterEarlyMixins} (Cleanroom bundles MixinBooter as a built-in mod,
 * so the {@code required-after:mixinbooter} dependency resolves there too). This is the single
 * registration path — there is deliberately no {@code MixinConfigs} manifest attribute.
 *
 * <p>Every {@link IFMLLoadingPlugin} method is implemented explicitly. Cleanroom declares them as
 * {@code default} methods, but vanilla Forge declares them abstract, so an empty body compiles only
 * against Cleanroom and breaks the Forge path. Do not remove these stubs.
 *
 * <p>Keep this class standalone: FML class-loads it (and anything it references) before mixins are
 * applied, so it must not touch mod or Minecraft classes.
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Do a Barrel Roll Core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class DoABarrelRollCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("do_a_barrel_roll.default.mixin.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
