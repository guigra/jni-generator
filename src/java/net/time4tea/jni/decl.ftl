
typedef struct {
    char*             class;
    JNINativeMethod[] methods;
    int               count;
} NativeRegistration;

static const JNINativeMethod ${classDecl}[] =
{
    <#list methods as method>
    { ${"\"${method.name}\""?right_pad(50)}, ${"\"${method.signature}\""?right_pad(100)}, ${"&${method.name}"?right_pad(50)} } <#if method_has_next>,</#if>
    </#list>
};

static const NativeRegistration registrations[] =
{
  { "${className}",  ${classDecl}, sizeof(${classDecl}) / sizeof(JNINativeMethod) }
}

void register_natives_${classDecl}(JNIEnv *env)) {
    int classes_to_register = sizeof(registrations) / sizeof(NativeReigstration);

    for ( int i = 0 ; i < classes_to_register ; i++ ) {
        NativeRegistration registration = registrations[i];
        jclass class = (*env)->FindClass(env, registration.class);

        if ( (*env)->RegisterNatives(env, registration.methods, registration.count ) != 0 ) {
            // oh crap
        }
    }
}
