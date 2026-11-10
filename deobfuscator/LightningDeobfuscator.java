import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.commons.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * Custom Automated ASM Deobfuscation, Bytecode Patch Purification, Dead Loader/Gate Eradication,
 * and Semantic Identifier Remapping Tool for LightningClient (LightningUser_no_key.jar).
 *
 * Execute via:
 *   /usr/bin/java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED \
 *                 --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED \
 *                 --add-exports java.base/jdk.internal.org.objectweb.asm.commons=ALL-UNNAMED \
 *                 LightningDeobfuscator.java <input.jar> <output.jar>
 */
public class LightningDeobfuscator {

    // Class-level semantic remapping table
    public static final Map<String, String> CLASS_MAP = new LinkedHashMap<>();

    // Method-level semantic remapping table: Key = "owner.nameDesc", Value = "newName"
    public static final Map<String, String> METHOD_MAP = new LinkedHashMap<>();

    // Dead encrypted loader, license gate, and broken 96-byte stub classes to eradicate
    public static final Set<String> ERADICATED_CLASSES = new HashSet<>(Arrays.asList(
        "lightning/core/AesCrypto.class",
        "lightning/core/PayloadClassLoader.class",
        "lightning/auth/AuthGate.class",
        "lightning/auth/AuthGate$1.class",
        "lightning/auth/AuthGate$2.class",
        "lightning/auth/AuthGate$3.class",
        "lightning/auth/AuthGate$4.class",
        "lightning/auth/AuthGate$AuthResult.class",
        "lightning/modules/impl/combat/FightLogModule.class"
    ));

    static {
        // Rename legacy entrypoint lightning/Night -> lightning/LightningClient
        CLASS_MAP.put("lightning/Night", "lightning/LightningClient");

        // Semantic method renames across LightningSkin
        METHOD_MAP.put("lightning/gui/LightningSkin.bq(FFFFII)V", "bufferQuadGradient");
        METHOD_MAP.put("lightning/gui/LightningSkin.bqc(FFFFLjava/awt/Color;)V", "bufferQuadSolid");
        METHOD_MAP.put("lightning/gui/LightningSkin.rr(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFZZZZLjava/awt/Color;Ljava/awt/Color;)V", "drawRoundedRect");
        METHOD_MAP.put("lightning/gui/LightningSkin.rrAll(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFLjava/awt/Color;)V", "drawRoundedRectAll");
        METHOD_MAP.put("lightning/gui/LightningSkin.fq(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "fillQuad");
        METHOD_MAP.put("lightning/gui/LightningSkin.ol(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawOutline");
        METHOD_MAP.put("lightning/gui/LightningSkin.dfTop(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawFrameTop");
        METHOD_MAP.put("lightning/gui/LightningSkin.dfBottom(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawFrameBottom");
        METHOD_MAP.put("lightning/gui/LightningSkin.nop(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawNoOp");

        // Semantic method renames across RefSkin
        METHOD_MAP.put("lightning/gui/RefSkin.fq(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "fillToggleQuad");
        METHOD_MAP.put("lightning/gui/RefSkin.ol(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawToggleOutline");

        // Semantic method renames across ColorGui
        METHOD_MAP.put("lightning/gui/ColorGui.r(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFFFLjava/awt/Color;)V", "drawRoundedBox");
        METHOD_MAP.put("lightning/gui/ColorGui.rd(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFFFLjava/awt/Color;)V", "drawRoundedBoxDual");

        // Semantic method renames across HeaderFx
        METHOD_MAP.put("lightning/gui/HeaderFx.c(IIII)Ljava/awt/Color;", "createRgbaColor");
        METHOD_MAP.put("lightning/gui/HeaderFx.nop(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFLjava/awt/Color;)V", "drawNoOp");

        // Semantic method renames across DashboardScreen
        METHOD_MAP.put("lightning/gui/DashboardScreen.in(DDIIII)Z", "isMouseWithinBounds");
        METHOD_MAP.put("lightning/gui/DashboardScreen.rr(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFFFFLjava/awt/Color;)V", "drawRoundedRect");
        METHOD_MAP.put("lightning/gui/DashboardScreen.tw(Ljava/lang/String;)I", "getTextWidth");
    }

    /**
     * Purifies all binary-patched NOP sleds (249 NOP instructions across 10 methods),
     * opaque predicates, and dead stack operations.
     */
    public static int purifyBytecodePatches(ClassNode cn) {
        int removedNops = 0;

        for (MethodNode mn : cn.methods) {
            // 1. Purify BooleanButton.render opaque predicate:
            //    aload 14; pop; iconst_1; nop; nop; nop; nop; ifeq L651
            if (cn.name.equals("lightning/gui/impl/BooleanButton") && mn.name.equals("render")) {
                AbstractInsnNode cur = mn.instructions.getFirst();
                while (cur != null) {
                    if (cur instanceof VarInsnNode vin && vin.getOpcode() == Opcodes.ALOAD && vin.var == 14
                            && cur.getNext() != null && cur.getNext().getOpcode() == Opcodes.POP
                            && cur.getNext().getNext() != null && cur.getNext().getNext().getOpcode() == Opcodes.ICONST_1) {
                        // Find the trailing IFEQ
                        AbstractInsnNode scan = cur.getNext().getNext().getNext();
                        while (scan != null && scan.getOpcode() == Opcodes.NOP) {
                            scan = scan.getNext();
                        }
                        if (scan instanceof JumpInsnNode jin && jin.getOpcode() == Opcodes.IFEQ) {
                            AbstractInsnNode del = cur;
                            while (del != scan) {
                                AbstractInsnNode next = del.getNext();
                                if (del.getOpcode() == Opcodes.NOP) removedNops++;
                                mn.instructions.remove(del);
                                del = next;
                            }
                            mn.instructions.remove(scan);
                            break;
                        }
                    }
                    cur = cur.getNext();
                }
            }

            // 2. Purify LoadingText.drawBackground dead alpha calculation + 103 NOP sled
            if (cn.name.equals("lightning/utils/graphics/LoadingText") && mn.name.equals("drawBackground")) {
                AbstractInsnNode cur = mn.instructions.getFirst();
                while (cur != null) {
                    if (cur instanceof LdcInsnNode ldc && ldc.cst instanceof Float f && Math.abs(f - 70.0f) < 0.01f
                            && cur.getNext() instanceof VarInsnNode vin && vin.getOpcode() == Opcodes.FLOAD && vin.var == 3) {
                        AbstractInsnNode scan = cur;
                        while (scan != null && !(scan instanceof VarInsnNode store && store.getOpcode() == Opcodes.ISTORE && store.var == 5)) {
                            scan = scan.getNext();
                        }
                        if (scan != null) {
                            AbstractInsnNode del = cur;
                            while (del != null) {
                                AbstractInsnNode next = del.getNext();
                                mn.instructions.remove(del);
                                if (del == scan) break;
                                del = next;
                            }
                            break;
                        }
                    }
                    cur = cur.getNext();
                }
            }

            // 3. Strip all remaining NOP instructions across all methods
            AbstractInsnNode insn = mn.instructions.getFirst();
            while (insn != null) {
                AbstractInsnNode next = insn.getNext();
                if (insn.getOpcode() == Opcodes.NOP) {
                    mn.instructions.remove(insn);
                    removedNops++;
                }
                insn = next;
            }
        }

        // 4. Purify NightUserManager: remove dead 30-second background polling thread
        //    that repeatedly invokes an empty poll() { return; } method
        if (cn.name.equals("lightning/managers/NightUserManager")) {
            cn.fields.removeIf(f -> f.name.equals("executor") || f.name.equals("http")
                    || f.name.equals("BASE_URL") || f.name.equals("FALLBACK_BASE_URL")
                    || f.name.equals("GSON") || f.name.equals("POLL_INTERVAL_SECONDS"));
            cn.methods.removeIf(m -> m.name.equals("poll") || m.name.equals("lambda$tryPoll$0")
                    || m.name.equals("lambda$new$0") || m.name.equals("<clinit>"));
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("<init>")) {
                    mn.instructions.clear();
                    mn.visitVarInsn(Opcodes.ALOAD, 0);
                    mn.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                    mn.visitVarInsn(Opcodes.ALOAD, 0);
                    mn.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/concurrent/ConcurrentHashMap",
                            "newKeySet", "()Ljava/util/concurrent/ConcurrentHashMap$KeySetView;", false);
                    mn.visitFieldInsn(Opcodes.PUTFIELD, cn.name, "onlineUsernames", "Ljava/util/Set;");
                    mn.visitInsn(Opcodes.RETURN);
                    mn.maxStack = 2;
                    mn.maxLocals = 1;
                }
            }
        }

        return removedNops;
    }

    public static void deobfuscateJar(Path inputJar, Path outputJar) throws Exception {
        Remapper remapper = new Remapper() {
            @Override
            public String map(String internalName) {
                return CLASS_MAP.getOrDefault(internalName, internalName);
            }

            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                String key = owner + "." + name + descriptor;
                return METHOD_MAP.getOrDefault(key, name);
            }
        };

        int totalNopsRemoved = 0;
        int totalClassesProcessed = 0;
        int normalizedVersionCount = 0;

        try (JarFile jar = new JarFile(inputJar.toFile());
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(outputJar))) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (ERADICATED_CLASSES.contains(name)) {
                    continue;
                }

                byte[] rawBytes;
                try (InputStream is = jar.getInputStream(entry)) {
                    rawBytes = is.readAllBytes();
                }

                if (name.endsWith(".class")) {
                    totalClassesProcessed++;
                    int origMajor = ((rawBytes[6] & 0xFF) << 8) | (rawBytes[7] & 0xFF);
                    if (origMajor != 69) {
                        normalizedVersionCount++;
                    }
                    // Downgrade header temporarily to 61 (Java 17) for JDK 17 internal ASM reader
                    rawBytes[6] = 0;
                    rawBytes[7] = 61;

                    ClassReader cr = new ClassReader(rawBytes);
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, 0);

                    totalNopsRemoved += purifyBytecodePatches(cn);

                    ClassNode remappedNode = new ClassNode();
                    ClassRemapper classRemapper = new ClassRemapper(remappedNode, remapper);
                    cn.accept(classRemapper);

                    if (CLASS_MAP.containsKey(cn.name)) {
                        String simpleName = remappedNode.name.substring(remappedNode.name.lastIndexOf('/') + 1);
                        remappedNode.sourceFile = simpleName + ".java";
                    }

                    ClassWriter cw = new ClassWriter(0);
                    remappedNode.accept(cw);
                    byte[] outBytes = cw.toByteArray();
                    // Normalize output class version to 69 (Java 25 / Minecraft 26.2)
                    outBytes[6] = 0;
                    outBytes[7] = 69;

                    String newEntryName = remappedNode.name + ".class";
                    JarEntry outEntry = new JarEntry(newEntryName);
                    jos.putNextEntry(outEntry);
                    jos.write(outBytes);
                    jos.closeEntry();
                } else if (name.equals("fabric.mod.json")) {
                    String content = new String(rawBytes, StandardCharsets.UTF_8);
                    content = content.replace("\"lightning.Night\"", "\"lightning.LightningClient\"");
                    JarEntry outEntry = new JarEntry(name);
                    jos.putNextEntry(outEntry);
                    jos.write(content.getBytes(StandardCharsets.UTF_8));
                    jos.closeEntry();
                } else {
                    JarEntry outEntry = new JarEntry(name);
                    jos.putNextEntry(outEntry);
                    jos.write(rawBytes);
                    jos.closeEntry();
                }
            }
        }

        System.out.println("Processed " + totalClassesProcessed + " classes.");
        System.out.println("Eradicated " + ERADICATED_CLASSES.size() + " dead loader/auth/stub classes.");
        System.out.println("Removed " + totalNopsRemoved + " binary-patched NOP instructions & dead predicates.");
        System.out.println("Normalized " + normalizedVersionCount + " mixed-version classes (Java 8/21 -> Java 25).");
        System.out.println("Applied " + CLASS_MAP.size() + " class renames and " + METHOD_MAP.size() + " semantic method renames.");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: LightningDeobfuscator <input.jar> <output.jar>");
            return;
        }
        Path in = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        deobfuscateJar(in, out);
        System.out.println("Successfully deobfuscated and purified " + in + " -> " + out);
    }
}
