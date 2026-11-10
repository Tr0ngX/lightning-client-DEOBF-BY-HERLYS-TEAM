package lightning.modules.impl.miscellaneous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChangePitchEvent;
import lightning.events.impl.ChangeYawEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.TickEvent;
import lightning.events.impl.ToggleModuleEvent;
import lightning.events.impl.UnfilteredKeyInputEvent;
import lightning.events.impl.UnfilteredMouseInputEvent;
import lightning.mixins.accessors.KeyboardHandlerAccessor;
import lightning.mixins.accessors.MouseAccessor;
import lightning.modules.Module;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.modules.impl.movement.HoleSnapModule;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.MovementUtils;
import lombok.Generated;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EURoboticsModule extends Module {
    public ModeSetting side;
    public StringSetting port;
    private String target;
    private final EURoboticsModule.Client client;
    private final EURoboticsModule.Server server;

    public EURoboticsModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Client";
        tmp0[1] = "Server";
        this.side = new ModeSetting("Side", "The side that this Minecraft instance is on.", "Client", tmp0);
        this.port = new StringSetting("Port", "The port that will be used for communication.", "4311");
        this.target = "";
        this.client = new EURoboticsModule.Client();
        this.server = new EURoboticsModule.Server();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("update;" + mc.player.getName().getString()));
        } catch (IOException ex5) {
            Object var2 = ex5;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onToggleModule(ToggleModuleEvent arg0) {
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("module;" + arg0.getModule().getName() + ";" + arg0.getModule().isToggled()));
        } catch (IOException ex3) {
            Object var2 = ex3;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onUnfilteredKeyInput(UnfilteredKeyInputEvent arg0) {
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("key;" + arg0.getKey() + ";" + arg0.getScancode() + ";" + arg0.getAction() + ";" + arg0.getModifiers()));
        } catch (IOException ex3) {
            Object var2 = ex3;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onUnfilteredMouseInput(UnfilteredMouseInputEvent arg0) {
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("mouse;" + arg0.getButton() + ";" + arg0.getAction() + ";" + arg0.getMods()));
        } catch (IOException ex3) {
            Object var2 = ex3;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onChangeYaw(ChangeYawEvent arg0) {
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("yaw;" + arg0.getYaw()));
        } catch (IOException ex3) {
            Object var2 = ex3;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onChangePitch(ChangePitchEvent arg0) {
        if (!(this.side.getValue().equalsIgnoreCase("Server")) && this.client.getPrimarySocket() != null) {
            if (!(this.client.getPrimarySocket().isConnected())) {
            }
            return;
            try {
            }
            this.client.sendMessage(("pitch;" + arg0.getPitch()));
        } catch (IOException ex3) {
            Object var2 = ex3;
            LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var2);
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if ((getNull()) || (mc.player.fallDistance >= 5.0)) {
            return;
        }
        if (this.side.getValue().equalsIgnoreCase("Client")) return;
        Object var2 = getPlayer();
        if (var2 == null) return;
        if (mc.player.distanceToSqr(var2.position()) <= 0.25) return;
        if (mc.player.distanceToSqr(var2.position()) <= Mth.square(1.5)) {
            if ((((mc.options.keyUp.isDown()) || (mc.options.keyDown.isDown())) || (mc.options.keyLeft.isDown())) || (mc.options.keyRight.isDown())) {
                return;
            }
        }
        if (((HoleSnapModule) LightningClient.MODULE_MANAGER.getModule(HoleSnapModule.class)).isToggled()) return;
        if (((HitboxDesyncModule) LightningClient.MODULE_MANAGER.getModule(HitboxDesyncModule.class)).isToggled()) return;
        MovementUtils.moveTowards(arg0, Vec3.upFromBottomCenterOf(var2.blockPosition(), 0.0), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
    }

    @SubscribeEvent
    public void onEnable() {
        if (!(this.side.getValue().equalsIgnoreCase("Client"))) break label_6;
        try {
            if (mc.player == null) break label_1;
            if (mc.level == null) {
                label_1:
                if (this.client.getPrimarySocket() != null) {
                    this.client.stopConnection();
                }
                return;
                try {
                }
                this.client.startConnection("127.0.0.1", Integer.parseInt(this.port.getValue()));
            } catch (IOException ex5) {
                Object var1 = ex5;
                LightningClient.LOGGER.error("An exception has been thrown by clientside EU Robotics!", var1);
                LightningClient.CHAT_MANAGER.error("Failed to establish a connection to the EURobotics server.");
                setToggled(false);
                break label_12;
                try {
                    label_6:
                    if (mc.player != null) {
                        if (mc.level == null) {
                        }
                        if (this.server.getServerSocket() != null) {
                            this.server.stopConnection();
                        }
                        return;
                        try {
                        }
                        new Thread(() -> {
    try {
        this.server.startConnection(Integer.parseInt(this.port.getValue()));
    } catch (IOException ex2) {
        Object var1 = ex2;
        throw new RuntimeException(var1);
    }
}).start();
                    } catch (IOException ex11) {
                        var1 = ex11;
                        LightningClient.LOGGER.error("An exception has been thrown by serverside EU Robotics!", var1);
                        LightningClient.CHAT_MANAGER.error("Failed to start the EURobotics server.");
                        setToggled(false);
                    }
                }
                label_12:
            }
        }
    }

    public boolean shouldStep() {
        if (!(isToggled())) return false;
        Object var1 = getPlayer();
        if (var1 == null) return false;
        if (mc.player.distanceToSqr(var1.position()) <= 0.25) return false;
        if (mc.player.distanceToSqr(var1.position()) <= Mth.square(1.5)) {
            if (!(mc.options.keyUp.isDown()) && !(mc.options.keyDown.isDown()) && !(mc.options.keyLeft.isDown()) && !(mc.options.keyRight.isDown())) {
            }
        } else {
        }
        return false;
    }

    private Player getPlayer() {
        if (this.target.isEmpty()) return null;
        Object var1 = mc.level.players().iterator();
        label_1:
        if (var1.hasNext()) {
            Object var2 = ((Player) var1.next());
            if (var2 == mc.player) break label_1;
            if (!(var2.getName().getString().equals(this.target))) {
                break label_1;
            }
            return var2;
        }
        return null;
    }

    @Generated
    public void setTarget(String arg0) {
        this.target = arg0;
    }


    public class Server {
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private BufferedReader in;

        public void startConnection(int arg0) throws IOException {
            this.serverSocket = new ServerSocket(arg0);
            this.clientSocket = this.serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            label_0:
            var tmp0 = this.in.readLine();
            Object var2 = tmp0;
            if (tmp0 == null) break label_8;
            Object var4 = var2.split(";");
            if ((!(var4[0].equalsIgnoreCase("module"))) || (var4.length != 3)) break label_1;
            var tmp1 = LightningClient.MODULE_MANAGER.getModule(var4[1]);
            Object var3 = tmp1;
            if (tmp1 == null) break label_1;
            LightningClient.TASK_MANAGER.submit(() -> arg0.setToggled(Boolean.parseBoolean(arg1[2])));
            label_1:
            if (var4[0].equalsIgnoreCase("update") && var4.length == 2) {
                ((EURoboticsModule) LightningClient.MODULE_MANAGER.getModule(EURoboticsModule.class)).setTarget(var4[1]);
            }
            if (var4[0].equalsIgnoreCase("key") && var4.length == 5 && IMinecraft.mc.keyboardHandler != null) {
                int var5 = Integer.parseInt(var4[1]);
                int var6 = Integer.parseInt(var4[2]);
                int var7 = Integer.parseInt(var4[3]);
                int var8 = Integer.parseInt(var4[4]);
                LightningClient.TASK_MANAGER.submit(() -> ((KeyboardHandlerAccessor) IMinecraft.mc.keyboardHandler).invokeKeyPress(IMinecraft.mc.getWindow().handle(), arg0, new KeyEvent(arg1, arg2, arg3)));
            }
            if (var4[0].equalsIgnoreCase("mouse") && var4.length == 4 && IMinecraft.mc.mouseHandler != null) {
                var5 = Integer.parseInt(var4[1]);
                var6 = Integer.parseInt(var4[2]);
                var7 = Integer.parseInt(var4[3]);
                LightningClient.TASK_MANAGER.submit(() -> ((MouseAccessor) IMinecraft.mc.mouseHandler).invokeOnButton(IMinecraft.mc.getWindow().handle(), new MouseButtonInfo(arg0, arg1), arg2));
            }
            if (var4[0].equalsIgnoreCase("yaw") && var4.length == 2 && IMinecraft.mc.player != null) {
                IMinecraft.mc.player.setYRot(Float.parseFloat(var4[1]));
            }
            if (var4[0].equalsIgnoreCase("pitch") && var4.length == 2 && IMinecraft.mc.player != null) {
                IMinecraft.mc.player.setXRot(Float.parseFloat(var4[1]));
            }
            if (((EURoboticsModule) LightningClient.MODULE_MANAGER.getModule(EURoboticsModule.class)).isToggled()) {
                break label_0;
            }
            stopConnection();
            break label_8;
            label_8:
        }

        public void stopConnection() throws IOException {
            this.in.close();
            this.clientSocket.close();
            this.serverSocket.close();
        }

        @Generated
        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }

        @Generated
        public Socket getClientSocket() {
            return this.clientSocket;
        }

        @Generated
        public BufferedReader getIn() {
            return this.in;
        }

    }

    public class Client {
        private Socket primarySocket;
        private PrintWriter primaryOut;
        private Socket secondarySocket;
        private PrintWriter secondaryOut;
        private Socket tertiarySocket;
        private PrintWriter tertiaryOut;

        public void startConnection(String arg0, int arg1) throws IOException {
            this.primarySocket = new Socket(arg0, arg1);
            this.primaryOut = new PrintWriter(this.primarySocket.getOutputStream(), true);
            this.secondarySocket = new Socket(arg0, (arg1 + 1));
            this.secondaryOut = new PrintWriter(this.secondarySocket.getOutputStream(), true);
            this.tertiarySocket = new Socket(arg0, (arg1 + 2));
            this.tertiaryOut = new PrintWriter(this.tertiarySocket.getOutputStream(), true);
        }

        public void stopConnection() throws IOException {
            this.primaryOut.close();
            this.primarySocket.close();
        }

        public void sendMessage(String arg0) throws IOException {
            this.primaryOut.println(arg0);
            this.secondaryOut.println(arg0);
            this.tertiaryOut.println(arg0);
        }

        @Generated
        public Socket getPrimarySocket() {
            return this.primarySocket;
        }

        @Generated
        public PrintWriter getPrimaryOut() {
            return this.primaryOut;
        }

        @Generated
        public Socket getSecondarySocket() {
            return this.secondarySocket;
        }

        @Generated
        public PrintWriter getSecondaryOut() {
            return this.secondaryOut;
        }

        @Generated
        public Socket getTertiarySocket() {
            return this.tertiarySocket;
        }

        @Generated
        public PrintWriter getTertiaryOut() {
            return this.tertiaryOut;
        }

    }
}
