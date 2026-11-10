package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "TextESP", description = "Renders text ESP on the world.", category = Module.Category.VISUALS)
public class TextESPModule extends Module {
    public BooleanSetting items;
    public ModeSetting itemListMode;
    public WhitelistSetting itemWhitelist;
    public BooleanSetting pearls;
    public BooleanSetting chorus;
    public NumberSetting scale;
    public ColorSetting color;
    public BooleanSetting glow;
    private final ArrayList chorusList;

    public TextESPModule() {
        this.items = new BooleanSetting("Items", "Renders text ESP on item entities.", true);
        var tmp0 = new String[3];
        tmp0[0] = "WhiteList";
        tmp0[1] = "BlackList";
        tmp0[2] = "All";
        this.itemListMode = new ModeSetting("Mode", "WhiteList = only listed items. BlackList = items NOT in list. All = render on every item.", new BooleanSetting.Visibility(this.items, true), "WhiteList", tmp0);
        this.itemWhitelist = new WhitelistSetting("Whitelist", "Items the WhiteList/BlackList mode compares against.", new BooleanSetting.Visibility(this.items, true), WhitelistSetting.Type.ITEMS);
        this.pearls = new BooleanSetting("Pearls", "Renders text ESP on pearl entities.", true);
        this.chorus = new BooleanSetting("Chorus", "Renders text ESP on chorus sounds.", true);
        this.scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 30, 10, 100);
        this.color = new ColorSetting("Color", "The color that will be used for the text ESP rendering.", new ColorSetting.Color(Color.WHITE, false, false));
        this.glow = new BooleanSetting("Glow", "Renders a glow shader effect around text ESP.", false);
        this.chorusList = new ArrayList();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (getNull()) return;
        Object var2 = this.chorusList;
        Object var3 = var2;
        // synchronized (var2)
        Object var6 = arg0.getPacket();
        if (!((var6 instanceof ClientboundSoundPacket))) break label_3;
        var tmp0 = ((ClientboundSoundPacket) var6);
        Object var4 = tmp0;
        var tmp1 = ((SoundEvent) tmp0.getSound().value());
        Object var5 = tmp1;
        if (tmp1 != SoundEvents.CHORUS_FRUIT_TELEPORT) {
            if (var5 != SoundEvents.ENDERMAN_TELEPORT) break label_3;
        }
        this.chorusList.add(new TextESPModule.Chorus(this, this, mc.getSoundManager().getSoundEvent(var5.location()).getSubtitle().getString(), new Vec3(var4.getX(), var4.getY(), var4.getZ())));
        label_3:
        break label_7;
        Object var7 = null;
        throw var7;
        label_7:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (getNull()) return;
        Object var2 = mc.level.entitiesForRendering().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Entity) var2.next());
            if (!(Renderer3D.isFrustumVisible(var3.getBoundingBox()))) {
                break label_1;
            }
            if (!((var3 instanceof ItemEntity))) break label_5;
            Object var6 = ((ItemEntity) var3);
            if ((!(this.items.getValue())) || (!(itemAllowed(var6.getItem().getItem())))) break label_5;
            Object var5 = EntityUtils.getRenderPos(var6, arg0.getTickDelta());
            if (var6.getItem().getCount() > 1) {
            } else {
            }
            Object var7 = ((" x" + var6.getItem().getCount()) + "");
            Renderer3D.renderScaledText(arg0.getMatrices(), var7, var5.x, var5.y, var5.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
            break label_1;
            label_5:
            if (!((var3 instanceof ThrownEnderpearl))) break label_1;
            var tmp0 = ((ThrownEnderpearl) var3);
            Object var4 = tmp0;
            if (tmp0.getOwner() == null) break label_1;
            if (!(this.pearls.getValue())) {
                break label_1;
            }
            var5 = EntityUtils.getRenderPos(var4, arg0.getTickDelta());
            Renderer3D.renderScaledText(arg0.getMatrices(), var4.getOwner().getName().getString(), var5.x, var5.y, var5.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
        }
        if (!(this.chorus.getValue())) break label_14;
        var2 = this.chorusList;
        var3 = var2;
        // synchronized (var2)
        this.chorusList.removeIf((arg0) -> (System.currentTimeMillis() - arg0.time) >= 1500L);
        var4 = this.chorusList.iterator();
        while (var4.hasNext()) {
            var5 = ((TextESPModule.Chorus) var4.next());
            Renderer3D.renderScaledText(arg0.getMatrices(), var5.subtitle, var5.pos.x, var5.pos.y, var5.pos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
        }
        break label_14;
        Object var8 = var6.getName().getString();
        throw var8;
        label_14:
    }

    private boolean itemAllowed(Item arg0) {
        int var2 = this.itemWhitelist.isWhitelistContains(arg0);
        Object var3 = this.itemListMode.getValue();
        int var4 = -1;
        switch (var3.hashCode()) {
            case -958939875 -> { break label_1; }
            case 349164615 -> { break label_0; }
            default -> { break label_2; }
        }
        label_0:
        if (!(var3.equals("WhiteList"))) break label_2;
        var4 = 0;
        break label_2;
        label_1:
        if (!(var3.equals("BlackList"))) break label_2;
        var4 = 1;
        label_2:
        switch (var4) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            default -> { break label_6; }
        }
        label_3:
        break label_7;
        label_4:
        if (!var2) {
            break label_7;
        }
        break label_7;
        label_6:
        label_7:
        return true;
    }


    class Chorus {
        private final String subtitle;
        private final Vec3 pos;
        private final long time;

        public Chorus(TextESPModule arg0, TextESPModule arg1, String arg2, Vec3 arg3) {
            Objects.requireNonNull(arg0);
            Objects.requireNonNull(arg1);
            this.subtitle = arg2;
            this.pos = arg3;
            this.time = System.currentTimeMillis();
        }

    }
}
