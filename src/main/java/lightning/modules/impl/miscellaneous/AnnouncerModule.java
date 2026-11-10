package lightning.modules.impl.miscellaneous;

import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.BreakBlockEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.ConsumeItemEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Announcer", description = "Announces your actions in chat.", category = Module.Category.MISCELLANEOUS)
public class AnnouncerModule extends Module {
    public StringSetting watermark;
    public ModeSetting language;
    public NumberSetting delay;
    public BooleanSetting clientside;
    public BooleanSetting greenText;
    public BooleanSetting distance;
    public BooleanSetting blocksMined;
    public BooleanSetting blocksPlaced;
    public BooleanSetting eating;
    private final ConcurrentLinkedQueue queue;
    private final Timer messageTimer;
    private final Timer distanceTimer;
    private Vec3 lastPos;
    private int mined;
    private int placed;
    private int eaten;

    public AnnouncerModule() {
        this.watermark = new StringSetting("Watermark", "The client name that will be used in the announcer.", "Lightning");
        var tmp0 = new String[24];
        tmp0[0] = "English";
        tmp0[1] = "German";
        tmp0[2] = "French";
        tmp0[3] = "Japanese";
        tmp0[4] = "Finnish";
        tmp0[5] = "Russian";
        tmp0[6] = "Spanish";
        tmp0[7] = "Swedish";
        tmp0[8] = "Turkish";
        tmp0[9] = "Dutch";
        tmp0[10] = "Greek";
        tmp0[11] = "Chinese";
        tmp0[12] = "Italian";
        tmp0[13] = "Norwegian";
        tmp0[14] = "Romanian";
        tmp0[15] = "Czech";
        tmp0[16] = "Portuguese";
        tmp0[17] = "Slovenian";
        tmp0[18] = "Polish";
        tmp0[19] = "Korean";
        tmp0[20] = "Lithuanian";
        tmp0[21] = "Indonesian";
        tmp0[22] = "Hungarian";
        tmp0[23] = "Random";
        this.language = new ModeSetting("Language", "The language that will be used for the announcer.", "English", tmp0);
        this.delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
        this.clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
        this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
        this.distance = new BooleanSetting("Distance", "Announces your distance travelled in chat.", true);
        this.blocksMined = new BooleanSetting("BlocksMined", "Announces blocks mined in chat.", true);
        this.blocksPlaced = new BooleanSetting("BlocksPlaced", "Announces blocks placed in chat.", true);
        this.eating = new BooleanSetting("Eating", "Announces when you eat in chat.", true);
        this.queue = new ConcurrentLinkedQueue();
        this.messageTimer = new Timer();
        this.distanceTimer = new Timer();
    }

    public void onEnable() {
        this.queue.clear();
        this.messageTimer.reset();
        this.distanceTimer.reset();
        this.lastPos = null;
        this.mined = 0;
        this.placed = 0;
        this.eaten = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (this.messageTimer.hasTimeElapsed((this.delay.getValue().intValue() * 1000)) && !(this.queue.isEmpty())) {
            Object var2 = ((String) this.queue.poll());
            if (this.clientside.getValue()) {
                LightningClient.CHAT_MANAGER.message(var2);
            } else {
                mc.player.connection.sendChat(((this.greenText.getValue() ? "> " : "") + var2));
            }
            this.messageTimer.reset();
        }
        if (this.lastPos == null) {
            this.lastPos = new Vec3(mc.player.xo, mc.player.yo, mc.player.zo);
        }
        var2 = ((Math.abs((this.lastPos.x - mc.player.xo)) + Math.abs((this.lastPos.y - mc.player.yo))) + Math.abs((this.lastPos.z - mc.player.zo)));
        if (this.distance.getValue() && var2 > 1.0 && this.distanceTimer.hasTimeElapsed(10000) && this.queue.size() <= 5) {
            this.queue.add(getDistanceMessage((MathUtils.round(var2, 1))));
            this.lastPos = new Vec3(mc.player.xo, mc.player.yo, mc.player.zo);
            this.distanceTimer.reset();
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakBlockEvent arg0) {
        if (getNull()) return;
        this.mined = (this.mined + 1);
        if (this.blocksMined.getValue() && ((double) this.mined) >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(getMineMessage((this.mined)));
            this.mined = 0;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (getNull()) return;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundUseItemOnPacket))) break label_1;
        var tmp0 = ((ServerboundUseItemOnPacket) var3);
        Object var2 = tmp0;
        if (!((mc.player.getItemInHand(tmp0.getHand()).getItem() instanceof BlockItem))) break label_1;
        this.placed = (this.placed + 1);
        if (((!(this.blocksPlaced.getValue())) || (((double) this.placed) < MathUtils.random(6.0, 1.0))) || (this.queue.size() > 5)) break label_1;
        this.queue.add(getPlaceMessage((this.placed)));
        this.placed = 0;
        label_1:
    }

    @SubscribeEvent
    public void onConsumeItem(ConsumeItemEvent arg0) {
        if ((getNull()) || (arg0.getStack().get(DataComponents.FOOD) == null)) {
            return;
        }
        this.eaten = (this.eaten + 1);
        if (this.eating.getValue() && ((double) this.eaten) >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(getEatMessage((this.eaten + " " + arg0.getStack().getHoverName().getString())));
            this.eaten = 0;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent arg0) {
        if (getNull()) return;
        this.messageTimer.reset();
    }

    private String getDistanceMessage(String arg0) {
        var tmp0 = new String[23];
        tmp0[0] = ("I just flew " + arg0 + " meters thanks to " + this.watermark.getValue() + "!");
        tmp0[1] = ("Ich bin gerade " + arg0 + " Meter weit geflogen, dank " + this.watermark.getValue() + "!");
        tmp0[2] = ("Je viens de voler " + arg0 + " m\u00e8tres gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!");
        tmp0[3] = (this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + arg0 + "\u30e1\u30fc\u30c8\u30eb\u98db\u3093\u3060\u3088\uff01");
        tmp0[4] = ("Lensin juuri " + arg0 + " metri\u00e4 " + this.watermark.getValue() + " ansiosta!");
        tmp0[5] = ("\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u043f\u0440\u043e\u043b\u0435\u0442\u0435\u043b " + arg0 + " \u043c\u0435\u0442\u0440\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!");
        tmp0[6] = ("Acabo de volar " + arg0 + " metros gracias a " + this.watermark.getValue() + "!");
        tmp0[7] = ("Jag fl\u00f6g just " + arg0 + " meter tack vare " + this.watermark.getValue() + "!");
        tmp0[8] = (this.watermark.getValue() + " sayesinde " + arg0 + " metre u\u00e7tum!");
        tmp0[9] = ("Ik heb net " + arg0 + " meter gevlogen dankzij " + this.watermark.getValue() + "!");
        tmp0[10] = ("\u039c\u03cc\u03bb\u03b9\u03c2 \u03c0\u03ad\u03c4\u03b1\u03be\u03b1 " + arg0 + " \u03bc\u03ad\u03c4\u03c1\u03b1 \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!");
        tmp0[11] = ("\u6211\u521a\u521a\u98de\u4e86" + arg0 + "\u7c73\uff0c\u591a\u4e8f\u4e86" + this.watermark.getValue() + "!");
        tmp0[12] = ("Ho appena volato per " + arg0 + " metri grazie ad " + this.watermark.getValue() + "!");
        tmp0[13] = ("Jeg fl\u00f8y nettopp " + arg0 + " meter takket v\u00e6re " + this.watermark.getValue() + "!");
        tmp0[14] = ("Tocmai am zburat " + arg0 + " de metri datorit\u0103 lui " + this.watermark.getValue() + "!");
        tmp0[15] = ("D\u00edky " + this.watermark.getValue() + " jsem pr\u00e1v\u011b ulet\u011bl " + arg0 + " metr\u016f!");
        tmp0[16] = ("Acabei de voar " + arg0 + " metros gra\u00e7as ao " + this.watermark.getValue() + "!");
        tmp0[17] = ("Z " + this.watermark.getValue() + " sem pravkar preletel " + arg0 + " metrov!");
        tmp0[18] = ("W\u0142a\u015bnie przelecia\u0142em " + arg0 + " metr\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!");
        tmp0[19] = (this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 " + arg0 + "\ub97c \ube44\ud589\ud588\uc2b5\ub2c8\ub2e4!");
        tmp0[20] = (this.watermark.getValue() + " d\u0117ka k\u0105 tik nuskridau " + arg0 + " metrus!");
        tmp0[21] = ("Saya baru saja terbang sejauh " + arg0 + " meter berkat " + this.watermark.getValue() + "!");
        tmp0[22] = (arg0 + " m\u00e9tert rep\u00fcltem az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!");
        Object var2 = tmp0;
        return var2[getLanguageIndex()];
    }

    private String getMineMessage(String arg0) {
        var tmp0 = new String[23];
        tmp0[0] = ("I just mined " + arg0 + " blocks thanks to " + this.watermark.getValue() + "!");
        tmp0[1] = ("Ich habe gerade " + arg0 + " Bl\u00f6cke abgebaut, dank " + this.watermark.getValue() + "!");
        tmp0[2] = ("Je viens d'extraire " + arg0 + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!");
        tmp0[3] = (this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + arg0 + "\u30d6\u30ed\u30c3\u30af\u63a1\u6398\u3057\u305f\u3068\u3053\u308d\u3067\u3059\uff01");
        tmp0[4] = ("Louhin juuri " + arg0 + " lohkoa " + this.watermark.getValue() + " ansiosta!");
        tmp0[5] = ("\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0434\u043e\u0431\u044b\u043b " + arg0 + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!");
        tmp0[6] = ("\u00a1Acabo de minar " + arg0 + " bloques gracias a " + this.watermark.getValue() + "!");
        tmp0[7] = ("Jag har precis tagit fram " + arg0 + " block tack vare " + this.watermark.getValue() + "!");
        tmp0[8] = (this.watermark.getValue() + " sayesinde az \u00f6nce " + arg0 + " blok kazd\u0131m!");
        tmp0[9] = ("Ik heb net " + arg0 + " blokken gedolven dankzij " + this.watermark.getValue() + "!");
        tmp0[10] = ("\u039c\u03cc\u03bb\u03b9\u03c2 \u03b5\u03be\u03cc\u03c1\u03c5\u03be\u03b1 " + arg0 + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!");
        tmp0[11] = ("\u6211\u521a\u521a\u5f00\u91c7\u4e86" + arg0 + "\u4e2a\u533a\u5757\uff0c\u611f\u8c22" + this.watermark.getValue() + "!");
        tmp0[12] = ("Ho appena estratto " + arg0 + " blocchi grazie ad " + this.watermark.getValue() + "!");
        tmp0[13] = ("Jeg har nettopp utvunnet " + arg0 + " blokker takket v\u00e6re " + this.watermark.getValue() + "!");
        tmp0[14] = ("Tocmai am minat " + arg0 + " de blocuri datorit\u0103 lui " + this.watermark.getValue() + "!");
        tmp0[15] = ("Pr\u00e1v\u011b jsem vyt\u011b\u017eil " + arg0 + " blok\u016f d\u00edky " + this.watermark.getValue() + "!");
        tmp0[16] = ("Acabei de extrair " + arg0 + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!");
        tmp0[17] = ("Zahvaljujo\u010d " + this.watermark.getValue() + " sem pravkar izkopal " + arg0 + " blokov!");
        tmp0[18] = ("W\u0142a\u015bnie wydoby\u0142em " + arg0 + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!");
        tmp0[19] = ("\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + arg0 + " \ube14\ub85d\uc744 \ucc44\uad74\ud588\uc2b5\ub2c8\ub2e4!");
        tmp0[20] = (this.watermark.getValue() + " d\u0117ka k\u0105 tik i\u0161kasiau " + arg0 + " blokus!");
        tmp0[21] = ("Saya baru saja menambang " + arg0 + " blok berkat " + this.watermark.getValue() + "!");
        tmp0[22] = ("Most b\u00e1ny\u00e1sztam " + arg0 + " blokkot az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!");
        Object var2 = tmp0;
        return var2[getLanguageIndex()];
    }

    private String getPlaceMessage(String arg0) {
        var tmp0 = new String[23];
        tmp0[0] = ("I just placed " + arg0 + " blocks thanks to " + this.watermark.getValue() + "!");
        tmp0[1] = ("Ich habe gerade " + arg0 + " Bl\u00f6cke dank " + this.watermark.getValue() + " platziert!");
        tmp0[2] = ("Je viens de placer " + arg0 + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!");
        tmp0[3] = (this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + arg0 + "\u500b\u306e\u30d6\u30ed\u30c3\u30af\u3092\u7f6e\u3044\u305f\u3068\u3053\u308d\u3060!");
        tmp0[4] = ("Sijoitin juuri " + arg0 + " lohkoa " + this.watermark.getValue() + " ansiosta!");
        tmp0[5] = ("\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0440\u0430\u0437\u043c\u0435\u0441\u0442\u0438\u043b " + arg0 + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!");
        tmp0[6] = ("\u00a1Acabo de colocar " + arg0 + " bloques gracias a " + this.watermark.getValue() + "!");
        tmp0[7] = ("Jag har precis placerat " + arg0 + " block tack vare " + this.watermark.getValue() + "!");
        tmp0[8] = ("Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + arg0 + " blok yerle\u015ftirdim!");
        tmp0[9] = ("Ik heb net " + arg0 + " blokken geplaatst dankzij " + this.watermark.getValue() + "!");
        tmp0[10] = ("\u039c\u03cc\u03bb\u03b9\u03c2 \u03c4\u03bf\u03c0\u03bf\u03b8\u03ad\u03c4\u03b7\u03c3\u03b1 " + arg0 + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!");
        tmp0[11] = ("\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "\uff0c\u6211\u521a\u521a\u653e\u4e86 " + arg0 + " \u5757!");
        tmp0[12] = ("Ho appena piazzato " + arg0 + " blocchi grazie a " + this.watermark.getValue() + "!");
        tmp0[13] = ("Jeg har nettopp plassert " + arg0 + " blokker takket v\u00e6re " + this.watermark.getValue() + "!");
        tmp0[14] = ("Tocmai am plasat " + arg0 + " blocuri datorit\u0103 lui " + this.watermark.getValue() + "!");
        tmp0[15] = ("Pr\u00e1v\u011b jsem um\u00edstil " + arg0 + " blok\u016f d\u00edky " + this.watermark.getValue() + "!");
        tmp0[16] = ("Acabei de colocar " + arg0 + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!");
        tmp0[17] = ("Pravkar sem postavil " + arg0 + " blokov zahvaljujo\u010d " + this.watermark.getValue() + "!");
        tmp0[18] = ("W\u0142a\u015bnie umie\u015bci\u0142em " + arg0 + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!");
        tmp0[19] = (this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 XXX \ube14\ub85d\uc744 \ubc30\uce58\ud588\uc2b5\ub2c8\ub2e4!");
        tmp0[20] = ("K\u0105 tik \u012fd\u0117jau " + arg0 + " blok\u0173 d\u0117ka " + this.watermark.getValue() + "!");
        tmp0[21] = ("Saya baru saja menempatkan blok " + arg0 + " berkat " + this.watermark.getValue() + "!");
        tmp0[22] = ("Most helyeztem el " + arg0 + " blokkot a " + this.watermark.getValue() + "-nek k\u00f6sz\u00f6nhet\u0151en!");
        Object var2 = tmp0;
        return var2[getLanguageIndex()];
    }

    private String getEatMessage(String arg0) {
        var tmp0 = new String[22];
        tmp0[0] = ("I just ate " + arg0 + " thanks to " + this.watermark.getValue() + "!");
        tmp0[1] = ("Ich habe gerade " + arg0 + " gegessen, dank " + this.watermark.getValue() + "!");
        tmp0[2] = ("Je viens de manger " + arg0 + " gr\u00e2ce \u00e0 " + this.watermark.getValue() + " !");
        tmp0[3] = (this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + arg0 + "\u3092\u98df\u3079\u305f\u3088!");
        tmp0[4] = ("S\u00f6in juuri " + arg0 + " kiitos " + this.watermark.getValue() + "!");
        tmp0[5] = ("\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0441\u044a\u0435\u043b " + arg0 + " \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!");
        tmp0[6] = ("\u00a1Acabo de comer " + arg0 + " gracias a " + this.watermark.getValue() + "!");
        tmp0[7] = ("Jag \u00e5t just " + arg0 + " tack vare " + this.watermark.getValue() + "!");
        tmp0[8] = ("Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + arg0 + " yedim!");
        tmp0[9] = ("Ik heb net " + arg0 + " gegeten dankzij " + this.watermark.getValue() + "!");
        tmp0[10] = ("\u039c\u03cc\u03bb\u03b9\u03c2 \u03ad\u03c6\u03b1\u03b3\u03b1 " + arg0 + " \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!");
        tmp0[11] = ("\u6211\u521a\u5403\u4e86 " + arg0 + "\uff0c\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "!");
        tmp0[12] = ("Ho appena mangiato " + arg0 + " grazie a " + this.watermark.getValue() + "!");
        tmp0[13] = ("Jeg spiste nettopp " + arg0 + " takket v\u00e6re " + this.watermark.getValue() + "!");
        tmp0[14] = ("Pr\u00e1v\u011b jsem sn\u011bdl " + arg0 + " d\u00edky " + this.watermark.getValue() + "!");
        tmp0[15] = ("Acabei de comer " + arg0 + " gra\u00e7as a " + this.watermark.getValue() + "!");
        tmp0[16] = ("Pravkar sem pojedel " + arg0 + " zaradi " + this.watermark.getValue() + "!");
        tmp0[17] = ("W\u0142a\u015bnie zjad\u0142em " + arg0 + " dzi\u0119ki " + this.watermark.getValue() + "!");
        tmp0[18] = ("\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + arg0 + "\ub97c \uba39\uc5c8\uc5b4\uc694!");
        tmp0[19] = ("K\u0105 tik suvalgiau " + arg0 + " d\u0117l " + this.watermark.getValue() + "!");
        tmp0[20] = ("Saya baru saja makan " + arg0 + " berkat " + this.watermark.getValue() + "!");
        tmp0[21] = ("Most ettem " + arg0 + "-t, h\u00e1la " + this.watermark.getValue() + "-nek!");
        Object var2 = tmp0;
        return var2[getLanguageIndex()];
    }

    private int getLanguageIndex() {
        Object var1 = this.language.getValue();
        int var2 = -1;
        switch (var1.hashCode()) {
            case -2041773788 -> { break label_18; }
            case -1898802383 -> { break label_17; }
            case -1883983667 -> { break label_10; }
            case -1854418717 -> { break label_22; }
            case -1764554162 -> { break label_12; }
            case -1654282081 -> { break label_21; }
            case -1550031926 -> { break label_20; }
            case -1541319955 -> { break label_16; }
            case -1463714219 -> { break label_15; }
            case -1298070587 -> { break label_19; }
            case -1074763917 -> { break label_4; }
            case -688086063 -> { break label_2; }
            case -517823520 -> { break label_11; }
            case -347177772 -> { break label_5; }
            case -176239783 -> { break label_13; }
            case -143377541 -> { break label_6; }
            case 65610643 -> { break label_14; }
            case 66399624 -> { break label_8; }
            case 69066464 -> { break label_9; }
            case 699082148 -> { break label_7; }
            case 811777979 -> { break label_3; }
            case 2112439738 -> { break label_1; }
            case 2129449382 -> { break label_0; }
            default -> { break label_23; }
        }
        label_0:
        if (!(var1.equals("German"))) break label_23;
        var2 = 0;
        break label_23;
        label_1:
        if (!(var1.equals("French"))) break label_23;
        var2 = 1;
        break label_23;
        label_2:
        if (!(var1.equals("Japanese"))) break label_23;
        var2 = 2;
        break label_23;
        label_3:
        if (!(var1.equals("Finnish"))) break label_23;
        var2 = 3;
        break label_23;
        label_4:
        if (!(var1.equals("Russian"))) break label_23;
        var2 = 4;
        break label_23;
        label_5:
        if (!(var1.equals("Spanish"))) break label_23;
        var2 = 5;
        break label_23;
        label_6:
        if (!(var1.equals("Swedish"))) break label_23;
        var2 = 6;
        break label_23;
        label_7:
        if (!(var1.equals("Turkish"))) break label_23;
        var2 = 7;
        break label_23;
        label_8:
        if (!(var1.equals("Dutch"))) break label_23;
        var2 = 8;
        break label_23;
        label_9:
        if (!(var1.equals("Greek"))) break label_23;
        var2 = 9;
        break label_23;
        label_10:
        if (!(var1.equals("Chinese"))) break label_23;
        var2 = 10;
        break label_23;
        label_11:
        if (!(var1.equals("Italian"))) break label_23;
        var2 = 11;
        break label_23;
        label_12:
        if (!(var1.equals("Norwegian"))) break label_23;
        var2 = 12;
        break label_23;
        label_13:
        if (!(var1.equals("Romanian"))) break label_23;
        var2 = 13;
        break label_23;
        label_14:
        if (!(var1.equals("Czech"))) break label_23;
        var2 = 14;
        break label_23;
        label_15:
        if (!(var1.equals("Portuguese"))) break label_23;
        var2 = 15;
        break label_23;
        label_16:
        if (!(var1.equals("Slovenian"))) break label_23;
        var2 = 16;
        break label_23;
        label_17:
        if (!(var1.equals("Polish"))) break label_23;
        var2 = 17;
        break label_23;
        label_18:
        if (!(var1.equals("Korean"))) break label_23;
        var2 = 18;
        break label_23;
        label_19:
        if (!(var1.equals("Lithuanian"))) break label_23;
        var2 = 19;
        break label_23;
        label_20:
        if (!(var1.equals("Indonesian"))) break label_23;
        var2 = 20;
        break label_23;
        label_21:
        if (!(var1.equals("Hungarian"))) break label_23;
        var2 = 21;
        break label_23;
        label_22:
        if (!(var1.equals("Random"))) break label_23;
        var2 = 22;
        label_23:
        switch (var2) {
            case 0 -> { break label_24; }
            case 1 -> { break label_25; }
            case 2 -> { break label_26; }
            case 3 -> { break label_27; }
            case 4 -> { break label_28; }
            case 5 -> { break label_29; }
            case 6 -> { break label_30; }
            case 7 -> { break label_31; }
            case 8 -> { break label_32; }
            case 9 -> { break label_33; }
            case 10 -> { break label_34; }
            case 11 -> { break label_35; }
            case 12 -> { break label_36; }
            case 13 -> { break label_37; }
            case 14 -> { break label_38; }
            case 15 -> { break label_39; }
            case 16 -> { break label_40; }
            case 17 -> { break label_41; }
            case 18 -> { break label_42; }
            case 19 -> { break label_43; }
            case 20 -> { break label_44; }
            case 21 -> { break label_45; }
            case 22 -> { break label_46; }
            default -> { break label_47; }
        }
        label_24:
        break label_48;
        label_25:
        break label_48;
        label_26:
        break label_48;
        label_27:
        break label_48;
        label_28:
        break label_48;
        label_29:
        break label_48;
        label_30:
        break label_48;
        label_31:
        break label_48;
        label_32:
        break label_48;
        label_33:
        break label_48;
        label_34:
        break label_48;
        label_35:
        break label_48;
        label_36:
        break label_48;
        label_37:
        break label_48;
        label_38:
        break label_48;
        label_39:
        break label_48;
        label_40:
        break label_48;
        label_41:
        break label_48;
        label_42:
        break label_48;
        label_43:
        break label_48;
        label_44:
        break label_48;
        label_45:
        break label_48;
        label_46:
        break label_48;
        label_47:
        label_48:
        return 0;
    }

}
