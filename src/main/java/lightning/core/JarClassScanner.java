package lightning.core;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class JarClassScanner {
    public static List findSubtypesOf(Class arg0, Class arg1) {
        Object var2 = new ArrayList();
        Object var3 = arg1.getClassLoader();
        if (arg1.getProtectionDomain().getCodeSource() != null) {
            break label_1;
        }
        label_1:
        Object var4 = null;
        Object var5 = new ArrayList();
        if (var4 == null) break label_27;
        try {
            Object var6 = new File(var4.toURI());
        } catch (Throwable ex4) {
            Object var7 = ex4;
            var6 = new File(var4.getPath());
        }
        if (var6.isDirectory()) {
            var5 = scanDirectory(var6);
        }
        } catch (Throwable ex7) {
            var6 = ex7;
        }
        if (!(var5.isEmpty())) break label_27;
        try {
            var7 = var4.openStream();
            try {
                Object var8 = new JarInputStream(var7);
                try {
                    label_11:
                    var tmp0 = var8.getNextJarEntry();
                    var6 = tmp0;
                    if (tmp0 != null) {
                        Object var9 = var6.getName();
                        if (!(var6.isDirectory()) && var9.endsWith(".class") && !(var9.contains("module-info"))) {
                            var5.add(toClassName(var9));
                        }
                        break label_11;
                    }
                    var8.close();
                } catch (Throwable ex14) {
                    var9 = ex14;
                    try {
                        var8.close();
                    } catch (Throwable ex17) {
                        Object var10 = ex17;
                        var9.addSuppressed(var10);
                    }
                    throw var9;
                }
                if (var7 == null) break label_25;
                var7.close();
            } catch (Throwable ex20) {
                var8 = ex20;
                if (var7 == null) break label_24;
                try {
                    var7.close();
                } catch (Throwable ex23) {
                    var9 = ex23;
                    var8.addSuppressed(var9);
                }
                label_24:
                throw var8;
            }
            label_25:
        } catch (Throwable ex26) {
            var7 = ex26;
        }
        label_27:
        var6 = var5.iterator();
        while (var6.hasNext()) {
            var7 = ((String) var6.next());
            try {
                var8 = Class.forName(var7, false, var3);
            } catch (Throwable ex31) {
                var9 = ex31;
                break label_28;
            }
            if ((var8 == arg0) || (!(arg0.isAssignableFrom(var8)))) break label_33;
            var9 = var8.getModifiers();
            if ((Modifier.isAbstract(var9)) || (Modifier.isInterface(var9))) break label_33;
            var2.add(var8);
            label_33:
        }
        return var2;
    }

    private static List scanJar(File arg0) {
        Object var1 = new ArrayList();
        try {
            Object var2 = new JarFile(arg0);
            try {
                Object var3 = var2.entries();
                while (var3.hasMoreElements()) {
                    Object var4 = ((JarEntry) var3.nextElement());
                    Object var5 = var4.getName();
                    if ((var4.isDirectory()) || (!(var5.endsWith(".class")))) break label_2;
                    if (var5.contains("module-info")) {
                        break label_2;
                    }
                    var1.add(toClassName(var5));
                }
                var2.close();
            } catch (Throwable ex5) {
                var3 = ex5;
                try {
                    var2.close();
                } catch (Throwable ex8) {
                    var4 = ex8;
                    var3.addSuppressed(var4);
                }
                throw var3;
            }
        } catch (Exception ex11) {
            var2 = ex11;
        }
        return var1;
    }

    private static List scanDirectory(File arg0) {
        Object var1 = arg0.toPath();
        try {
            Object var2 = Files.walk(var1, new FileVisitOption[0]);
            try {
                Object var3 = var2.filter((arg0) -> {
    if (!(arg0.toString().endsWith(".class"))) break label_0;
    return !(arg0.toString().contains("module-info"));
}).map((arg1) -> toClassName(arg0.relativize(arg1).toString().replace(File.separatorChar, 47))).toList();
                if (var2 != null) {
                    var2.close();
                }
                return var3;
                try {
                } catch (Throwable ex4) {
                    var3 = ex4;
                    if (var2 == null) break label_8;
                    try {
                        var2.close();
                    } catch (Throwable ex7) {
                        Object var4 = ex7;
                        var3.addSuppressed(var4);
                    }
                    label_8:
                    throw var3;
                }
            } catch (Exception ex9) {
                var2 = ex9;
                return List.of();
            }
        }
    }

    private static String toClassName(String arg0) {
        return arg0.substring(0, (arg0.length() - ".class".length())).replace(47, 46);
    }

}
