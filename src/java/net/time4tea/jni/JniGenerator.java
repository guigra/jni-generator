package net.time4tea.jni;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class JniGenerator {

    public static void main(String[] args) throws IOException, Templates.TemplateProcessingException {

        File input = new File("xxx");

        String outputFilename = "foobar.c";

        FileWriter fileWriter = new FileWriter(new File(outputFilename));

        List<File> filepaths = collectClassesFrom(input);

        final NativeClassParser parser = new NativeClassParser();

        Templates templates = new Templates(JniGenerator.class);

        Map<String, Object> model = newHashMap();



        model.put("filename", outputFilename);
        model.put("date", new Date());

        model.put("classes", newArrayList(
                Iterables.filter(
                Lists.transform(filepaths, new Function<File, NativeClass>() {
                    @Override
                    public NativeClass apply(File filename) {
                        try {
                            return parser.parse(filename);
                        } catch (IOException e) {
                            throw new RuntimeException("dammit",e);
                        }
                    }
                }), new Predicate<NativeClass>() {
                    @Override
                    public boolean apply(NativeClass o) {
                        return o.hasNativeMethods();
                    }
                })
        ));

        templates.process("decl.ftl", model, fileWriter);
    }

    private static List<File> collectClassesFrom(File input) {

        if ( input.isFile()) {
            return newArrayList(input);
        }

        List<File> files = newArrayList();
        addClassFilesIn(files, input);
        return files;
    }

    private static void addClassFilesIn(List<File> files, File input) {
        File[] directory = input.listFiles();
        if ( directory == null ) {
            return;
        }

        for (File file : directory) {
            if ( file.isDirectory() ) {
                addClassFilesIn(files, file);
            }
            else if ( file.getName().endsWith(".class")) {
                files.add(file);
            }
        }
    }

    public static class NativeClassParser {
        public NativeClass parse(File file) throws IOException {
            ClassReader classReader = new ClassReader(new FileInputStream(file));
            F f = new F();
            classReader.accept(f, ClassReader.EXPAND_FRAMES);
            return new NativeClass(
                    f.className,
                    file.getName(),
                    file.getName().toLowerCase().replaceAll("\\.", "_"),
                    f.methods
            );
        }
    }

    public static class NativeClass {

        public final String className;
        public final String internalName;
        public final String decl;
        public final List<NativeMethod> methods;

        public NativeClass(String className, String internalName, String decl, List<NativeMethod> methods) {
            this.className = className;
            this.internalName = internalName;
            this.decl = decl;
            this.methods = methods;
        }

        public String getClassName() {
            return className;
        }

        public String getInternalName() {
            return internalName;
        }

        public String getDecl() {
            return decl;
        }

        public List<NativeMethod> getMethods() {
            return methods;
        }

        public boolean hasNativeMethods() {
            return ! methods.isEmpty();
        }
    }

    public static class NativeMethod {

        public final String name;
        public final String signature;

        public NativeMethod(String name, String signature) {
            this.name = name;
            this.signature = signature;
        }

        // used from freemarker
        public String getName() {
            return name;
        }

        // used from freemarker
        public String getSignature() {
            return signature;
        }
    }

    public static class F extends ClassVisitor {

        private String className;
        public final List<NativeMethod> methods = newArrayList();

        public F() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name.replaceAll("/", ".");
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

            if ((access & Opcodes.ACC_NATIVE) != 0) {
                methods.add(new NativeMethod(
                        name, desc
                ));
            }

            return null;
        }
    }
}
