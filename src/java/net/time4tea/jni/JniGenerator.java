package net.time4tea.jni;

import com.google.common.base.Joiner;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class JniGenerator {


    public static void main(String[] args) throws IOException, Templates.TemplateProcessingException {
        ClassReader classReader = new ClassReader(new FileInputStream("PlannerBookedProgrammeImpl.class"));
        F f = new F();
        classReader.accept(f, ClassReader.SKIP_DEBUG);


        Templates templates = new Templates(JniGenerator.class);

        Map<String, Object> model = newHashMap();

        model.put("classDecl", "planner_booked_programme_impl");
        model.put("className", "PlannerBookedProgrammeImpl.class");
        model.put("methods", f.methods);

        String process = templates.process("decl.ftl", model);
        System.out.println(process);
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

        public final List<NativeMethod> methods = newArrayList();

        public F() {
            super(Opcodes.ASM4);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

            if ((access & Opcodes.ACC_NATIVE) != 0) {
                methods.add( new NativeMethod(
                        name, desc
                ));
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
