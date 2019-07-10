package frc.team4069.constexpr

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class ConstExprClassBuilder(val delegateBuilder: ClassBuilder, val messageCollector: MessageCollector) : DelegatingClassBuilder() {
    override fun getDelegate() = delegateBuilder

    val targetFieldNames = mutableListOf<String>()

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        val descriptor = origin.descriptor as? PropertyDescriptor ?: return super.newField(origin, access, name, desc, signature, value)

        if(!descriptor.annotations.hasAnnotation(FqName(ANNOTATION_NAME))) return super.newField(origin, access, name, desc, signature, value)

        // Fields should be static final
        val flags = Opcodes.ACC_STATIC or Opcodes.ACC_FINAL
        if((access and flags) != flags || value != null) return super.newField(origin, access, name, desc, signature, value)

        targetFieldNames.add(name)

       return super.newField(origin, access, name, desc, signature, value)
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        messageCollector.report(CompilerMessageSeverity.WARNING, "Visiting method decl of method $name")
        if(name == "<clinit>") {
            messageCollector.report(CompilerMessageSeverity.WARNING, "All recognized constexpr fields: $targetFieldNames")
            val original = super.newMethod(origin, access, name, desc, signature, exceptions)
            // clinit is the last thing generated in objects, therefore targetFieldNames will be filled by now
            return ConstExprMethodVisitor(targetFieldNames, messageCollector, original)
        }
        return super.newMethod(origin, access, name, desc, signature, exceptions)
    }

    companion object {
        const val ANNOTATION_NAME = "frc.team4069.constexpr.ConstExpr"
    }
}
