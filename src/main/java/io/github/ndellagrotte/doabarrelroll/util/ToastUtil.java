package io.github.ndellagrotte.doabarrelroll.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.util.text.TextComponentTranslation;

public class ToastUtil {
    public static void toasty(String key) {
        Minecraft.getMinecraft().getToastGui().add(new SystemToast(
                SystemToast.Type.NARRATOR_TOGGLE,
                new TextComponentTranslation("toast.do_a_barrel_roll"),
                new TextComponentTranslation("toast.do_a_barrel_roll." + key)
        ));
    }
}
