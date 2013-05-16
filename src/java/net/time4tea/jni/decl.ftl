
typedef struct {
    char*             class;
    JNINativeMethod[] methods;
    int               count;
} NativeRegistration;


<#list classes as class>
static const JNINativeMethod ${class.decl}[] =
{
    <#list class.methods as method>
    { ${"\"${method.name}\""?right_pad(50)}, ${"\"${method.signature}\""?right_pad(100)}, ${"&${method.name}"?right_pad(50)} } <#if method_has_next>,</#if>
    </#list>
};

</#list>

static const NativeRegistration registrations[] =
{
  <#list classes as class>
  { "${class.internalName}",  ${class.decl}, sizeof(${class.decl}) / sizeof(JNINativeMethod) } <#if class_has_next>,</#if>
  </#list>
};

void register_natives_xxx(JNIEnv *env)) {
    int classes_to_register = sizeof(registrations) / sizeof(NativeReigstration);

    for ( int i = 0 ; i < classes_to_register ; i++ ) {
        NativeRegistration registration = registrations[i];
        jclass class = (*env)->FindClass(env, registration.class);

        if ( (*env)->RegisterNatives(env, registration.methods, registration.count ) != 0 ) {
            // oh crap
        }
    }
}
