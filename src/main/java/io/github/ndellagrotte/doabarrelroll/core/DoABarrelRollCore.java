package io.github.ndellagrotte.doabarrelroll.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * Empty loading plugin. Exists so the jar carries the FMLCorePlugin manifest attribute,
 * which makes CoreModManager register do_a_barrel_roll.default.mixin.json during coremod
 * discovery instead of at LoaderState.CONSTRUCTING — the mod's mixins target vanilla
 * classes (Entity, EntityLivingBase, EntityPlayer, EntityPlayerSP, EntityRenderer) that
 * are already loaded by the time the late path runs.
 *
 * <p>Keep this class standalone: FML class-loads it (and anything it references) before
 * mixins are applied, so it must not touch mod or Minecraft classes.
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Do a Barrel Roll Core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class DoABarrelRollCore implements IFMLLoadingPlugin {
}
