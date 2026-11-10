package lightning.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

/**
 * Purified JarClassScanner: Discovers concrete subtypes of Module and Command at startup.
 * Dead AesCrypto and PayloadClassLoader classes from lightning.core have been removed.
 */
public final class JarClassScanner {
    private JarClassScanner() {}

    public static <T> List<Class<? extends T>> findSubtypesOf(Class<T> baseType, Class<?> anchorClass) {
        List<Class<? extends T>> result = new ArrayList<>();
        ClassLoader loader = anchorClass.getClassLoader();
        URL location = (anchorClass.getProtectionDomain().getCodeSource() != null)
                ? anchorClass.getProtectionDomain().getCodeSource().getLocation()
                : null;
        List<String> classNames = new ArrayList<>();

        if (location != null) {
            try {
                File file;
                try {
                    file = new File(location.toURI());
                } catch (Exception e) {
                    file = new File(location.getPath());
                }
                if (file.isDirectory()) {
                    classNames = scanDirectory(file);
                }
            } catch (Exception ignored) {
            }

            if (classNames.isEmpty()) {
                try (InputStream is = location.openStream();
                     JarInputStream jis = new JarInputStream(is)) {
                    JarEntry entry;
                    while ((entry = jis.getNextJarEntry()) != null) {
                        String name = entry.getName();
                        if (!entry.isDirectory() && name.endsWith(".class") && !name.contains("module-info")) {
                            classNames.add(toClassName(name));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        for (String name : classNames) {
            try {
                Class<?> cls = Class.forName(name, false, loader);
                if (cls != baseType && baseType.isAssignableFrom(cls)) {
                    int mods = cls.getModifiers();
                    if (!Modifier.isAbstract(mods) && !Modifier.isInterface(mods)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends T> typed = (Class<? extends T>) cls;
                        result.add(typed);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    private static List<String> scanDirectory(File dir) {
        Path root = dir.toPath();
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(p -> p.toString().endsWith(".class") && !p.toString().contains("module-info"))
                    .map(p -> toClassName(root.relativize(p).toString().replace(File.separatorChar, '/')))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String toClassName(String entryName) {
        return entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
    }
}
