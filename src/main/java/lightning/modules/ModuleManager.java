package lightning.modules;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.core.JarClassScanner;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.api.Frame;
import lightning.gui.impl.ModuleButton;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.settings.Setting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.IMinecraft;
import lightning.utils.input.KeyboardUtils;
import lombok.Generated;

public class ModuleManager implements IMinecraft {
    private final List modules;
    private final Map moduleClasses;

    public ModuleManager() {
        this.modules = new ArrayList();
        this.moduleClasses = new Reference2ReferenceOpenHashMap();
        try {
            LightningClient.EVENT_HANDLER.subscribe(this);
            Object var1 = JarClassScanner.findSubtypesOf(Module.class, LightningClient.class).iterator();
            while (var1.hasNext()) {
                Object var2 = ((Class) var1.next());
                registerIfAnnotated(var2);
            }
            var1 = ModuleManager.class;
            this.modules.sort(Comparator.comparing(Module::getName));
        } catch (Throwable ex4) {
            var1 = ex4;
            throw var1;
        }
    }

    private void registerIfAnnotated(Class arg0) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (arg0.getAnnotation(RegisterModule.class) == null) return;
        Object var2 = ((Module) arg0.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
        register(var2);
    }

    public void register(Module arg0) {
        if (arg0 == null) {
            return;
            try {
            }
            Object var2 = arg0.getClass().getDeclaredFields();
            int var3 = var2.length;
            int var4 = 0;
            while (var4 < var3) {
                Object var5 = var2[var4];
                if (!(Setting.class.isAssignableFrom(var5.getType()))) {
                    break label_6;
                }
                if (!(var5.canAccess(arg0))) {
                    var5.setAccessible(true);
                }
                var tmp0 = ((Setting) var5.get(arg0));
                Object var6 = tmp0;
                if (tmp0 == null) {
                    break label_6;
                }
                if (!((arg0 instanceof HUDModule))) break label_5;
                Object var9 = ((HUDModule) arg0);
                if (HUDModule.isMusicAddonLoaded()) break label_5;
                if ((var6 == var9.musicHudCategory) || (var6 == var9.musicHudPosition)) break label_6;
                var tmp1 = var6.getVisibility();
                Object var8 = tmp1;
                if (!((tmp1 instanceof CategorySetting.Visibility))) break label_5;
                var tmp2 = ((CategorySetting.Visibility) var8);
                Object var7 = tmp2;
                if (tmp2.getValue() != var9.musicHudCategory) break label_5;
                break label_6;
                label_5:
                arg0.getSettings().add(var6);
                label_6:
                var4++;
            }
        } catch (IllegalAccessException ex8) {
            var2 = ex8;
            LightningClient.LOGGER.error(("Failed to register settings for module " + arg0.getName()), var2);
        }
        arg0.getSettings().add(arg0.chatNotify);
        arg0.getSettings().add(arg0.drawn);
        if (!(arg0.isPersistent()) && !(arg0 instanceof AutoShopModule)) {
            arg0.getSettings().add(arg0.bind);
        }
        if (this.modules.contains(arg0)) break label_13;
        this.modules.add(arg0);
        this.moduleClasses.put(arg0.getClass(), arg0);
        this.modules.sort(Comparator.comparing(Module::getName));
        if (LightningClient.CLICK_GUI == null) break label_13;
        var2 = LightningClient.CLICK_GUI.getFrames().iterator();
        while (var2.hasNext()) {
            var3 = ((Frame) var2.next());
            if (var3.getCategory() != arg0.getCategory()) break label_11;
            if ((arg0 instanceof PingBypassModule)) {
                break label_11;
            }
            var3.getButtons().add(new ModuleButton(arg0, var3, var3.getHeight()));
        }
        label_13:
    }

    public void unregister(Module arg0) {
        if (arg0 == null) return;
        if (arg0.isToggled()) {
            arg0.setToggled(false);
        }
        this.modules.remove(arg0);
        this.moduleClasses.remove(arg0.getClass());
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (mc.gui != null && mc.gui.screen() != null) return;
        this.modules.stream().filter((arg1) -> {
    if (arg1.getBind() != arg0.getKey()) break label_0;
    return arg1.bind.getMode().equals("Bind");
}).forEach((arg0) -> arg0.setToggled(!(arg0.isToggled())));
        Object var2 = ((AutoShopModule) getModule(AutoShopModule.class));
        if (var2 != null) {
            var2.handleKey(arg0.getKey());
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if (mc.gui != null && mc.gui.screen() != null) return;
        this.modules.stream().filter((arg1) -> {
    if (arg1.getBind() != (-(arg0.getButton()) - 1)) break label_0;
    return arg1.bind.getMode().equals("Bind");
}).forEach((arg0) -> arg0.setToggled(!(arg0.isToggled())));
        Object var2 = ((AutoShopModule) getModule(AutoShopModule.class));
        if (var2 != null) {
            var2.handleKey((-(arg0.getButton()) - 1));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.gui != null && mc.gui.screen() != null) return;
        Object var2 = this.modules.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            Object var5 = var3.bind.getMode();
            if (var5.equals("Bind")) break label_1;
            if (!(var3.getBind())) {
                break label_1;
            }
            int var6 = KeyboardUtils.isBindDown(var3.getBind());
            var tmp0 = (var5.equals("Hold") == var6 ? 1 : 0);
            int var4 = tmp0;
            int var7 = tmp0;
            if (var3.isToggled() == var4) {
                break label_1;
            }
            var3.setToggled(var4);
        }
    }

    public List getModules(Module.Category arg0) {
        return this.modules.stream().filter((arg1) -> arg1.getCategory() == arg0).toList();
    }

    public Module getModule(String arg0) {
        return ((Module) this.modules.stream().filter((arg1) -> arg1.getName().equalsIgnoreCase(arg0)).findFirst().orElse(null));
    }

    public Module getModule(Class arg0) {
        return ((Module) this.moduleClasses.get(arg0));
    }

    @Generated
    public List getModules() {
        return this.modules;
    }

    @Generated
    public Map getModuleClasses() {
        return this.moduleClasses;
    }

}
