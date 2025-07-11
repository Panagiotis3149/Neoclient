package neo.script;

import neo.Neo;
import neo.util.Utils;
import net.minecraft.launchwrapper.Launch;

import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;

public class Script {
    public String name;
    public Class d;
    public Object b;
    public String scriptName;
    public String codeStr;
    public boolean error = false;
    public int extraLines;
    public ScriptEvents event;

    public Script(String name) {
        this.name = name;
        this.scriptName = "sc_" + name.replace(" ", "").replace(")", "_").replace("(", "_") + "_" + Utils.generateRandomString(5);
    }

    public float[] getFloat(final String s, final Object... array) {
        if (this.d == null || this.b == null) {
            return null;
        }
        Method method = null;
        for (final Method method2 : this.d.getDeclaredMethods()) {
            if (method2.getName().equalsIgnoreCase(s) && method2.getParameterCount() == array.length && method2.getReturnType().equals(float[].class)) {
                method = method2;
                break;
            }
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                final Object invoke = method.invoke(this.b, array);
                if (invoke instanceof float[]) {
                    return (float[])invoke;
                }
            }
            catch (IllegalAccessException ex) {}
            catch (InvocationTargetException ex2) {}
        }
        return null;
    }

    public boolean run() {
        try {
            if (this.scriptName == null || this.codeStr == null) {
                return false;
            }
            final File file = new File(Neo.scriptManager.tempDir);
            if (!file.exists() || !file.isDirectory()) {
                file.mkdir();
            }
            if (Neo.scriptManager.compiler == null) {
                return false;
            }
            final Diagnostic bp = new Diagnostic();
            final StandardJavaFileManager standardFileManager = Neo.scriptManager.compiler.getStandardFileManager(bp, null, null);
            final ArrayList<String> list = new ArrayList<>();
            list.add("-d");
            list.add(Neo.scriptManager.tempDir);
            list.add("-XDuseUnsharedTable");
            if (!(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
                list.add("-classpath");
                String s = Neo.scriptManager.b;
                try {
                    s = URLDecoder.decode(s, "UTF-8");
                }
                catch (UnsupportedOperationException ex2) {}
                list.add(s);
            }
            boolean success = Neo.scriptManager.compiler.getTask(null, standardFileManager, bp, list, null, Collections.singletonList(new ClassObject(this.scriptName, this.codeStr, this.extraLines))).call();
            if (!success) {
                this.error = true;
                return false;
            }
            final URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, Launch.classLoader);
            this.d = urlClassLoader.loadClass(this.scriptName);
            this.b = this.d.newInstance();
            urlClassLoader.close();
            return true;
        }
        catch (Exception ex) {
            this.error = true;
            return !error;
        }
    }

    public int getBoolean(final String s, final Object... array) {
        if (this.d == null || this.b == null) {
            return -1;
        }
        Method method = null;
        for (final Method method2 : this.d.getDeclaredMethods()) {
            if (method2.getName().equalsIgnoreCase(s) && method2.getParameterCount() == array.length && method2.getReturnType().equals(Boolean.TYPE)) {
                method = method2;
                break;
            }
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                final Object invoke = method.invoke(this.b, array);
                if (invoke instanceof Boolean) {
                    return ((boolean)invoke) ? 1 : 0;
                }
            }
            catch (Exception e) {
                printRunTimeError(e, s);
            }
        }
        return -1;
    }

    public void delete() {
        this.d = null;
        this.b = null;
        final File file = new File(Neo.scriptManager.tempDir + File.separator + this.scriptName + ".class");
        if (file.exists()) {
            file.delete();
        }
    }

    public void createScript(final String s) {
        extraLines = 0;
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> iterator = Neo.scriptManager.imports.iterator();
        while (iterator.hasNext()) {
            extraLines++;
            sb.append("import ").append(iterator.next()).append(";\n");
        }
        sb.append("import neo.script.classes.*;\n");
        sb.append("import neo.script.packets.clientbound.*;\n");
        sb.append("import neo.script.packets.serverbound.*;\n");
        String name = Utils.extractFileName(this.name);
        this.codeStr = sb + "public class " + this.scriptName + " extends " + ScriptDefaults.class.getName() + " {public static final " + ScriptDefaults.modules.class.getName().replace("$", ".") + " modules = new " + ScriptDefaults.modules.class.getName().replace("$", ".") + "(\"" + name + "\");public static final String scriptName = \"" + name + "\";\n" + s + "\n}";
        extraLines += 4;
    }

    public boolean invokeMethod(final String s, final Object... array) {
        if (this.d == null || this.b == null) {
            return false;
        }
        Method method = null;
        for (final Method method2 : this.d.getDeclaredMethods()) {
            if (method2.getName().equalsIgnoreCase(s) && method2.getParameterCount() == array.length && method2.getReturnType().equals(Void.TYPE)) {
                method = method2;
                break;
            }
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                method.invoke(this.b, array);
                return true;
            }
            catch (Exception e) {
                printRunTimeError(e, s);
            }
        }
        return false;
    }

    private int getLine(Exception e, String name) {
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().equals(name)) {
                return element.getLineNumber() - extraLines;
            }
        }
        return 0;
    }

    private void printRunTimeError(Exception e, String methodName) {
        Utils.sendDebugMessage("§cRuntime ewwor during script §b" + Utils.extractFileName(this.name));
        Utils.sendDebugMessage(" §7err: §c" + e.getCause().getClass().getSimpleName());
        Utils.sendDebugMessage(" §7line: §c" + getLine((Exception) e.getCause(), this.scriptName));
        Utils.sendDebugMessage(" §7src: §c" + methodName);
    }
}
