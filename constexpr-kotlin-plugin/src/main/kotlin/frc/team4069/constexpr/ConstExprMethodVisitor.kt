package frc.team4069.constexpr

import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class ConstExprMethodVisitor(val targetFieldNames: List<String>, val messageCollector: MessageCollector, val internalMv: MethodVisitor) :
    MethodVisitor(Opcodes.ASM5, internalMv) {
    data class InvokeDescriptor(
        val opcode: Int,
        val owner: String,
        val name: String,
        val descriptor: String,
        val isInterface: Boolean
    )

    private val unappliedStaticInvokes = mutableListOf<InvokeDescriptor>()

    /**
     * When called, checks if the field insn being visited is operating on a constexpr field
     * If so, pops the last INVOKESTATIC, evaluates it in place and adds the result before generating this instruction.
     *
     * Otherwise, makes sure that no previous INVOKESTATIC was mistakenly caught, and allows the code to generate if so.
     */
    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        if (opcode == Opcodes.PUTSTATIC) {
            messageCollector.report(CompilerMessageSeverity.WARNING, "Visiting field insn for field $name of $owner")
            if (name != null && targetFieldNames.contains(name)) {
                messageCollector.report(CompilerMessageSeverity.WARNING, "Field $name is targeted for inlining")
                val (_, owner, name, descriptor, _) = unappliedStaticInvokes.pop()
                val ownerClass = Class.forName(owner.replace("/", "."))
                val method = ownerClass.getDeclaredMethod(name)
                val result = method.invoke(null)
                InstructionAdapter(this).apply {
                    val descriptorReturn = descriptor.replace("()", "")
                    when {
                        descriptorReturn == "J" -> lconst(result as Long)
                        descriptorReturn.startsWith("L") -> {
                            aconst(result)
                        }
                        else -> {
                        }
                    }
                }
            } else {
                if(unappliedStaticInvokes.isNotEmpty()) {
                    messageCollector.report(CompilerMessageSeverity.WARNING, "Candidate for inlining not annotated. Allowing normal codegen")
                    val (opcode, owner, name, descriptor, isInterface) = unappliedStaticInvokes.pop()
                    internalMv.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }
        }
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    /**
     * Catches potential
     */
    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {

        if (opcode == Opcodes.INVOKESTATIC && owner != null && name != null && descriptor != null && descriptor.contains(
                "()"
            )
        ) {
            messageCollector.report(CompilerMessageSeverity.WARNING, "Not applying method $name as it is a candidate for inlining")
            unappliedStaticInvokes.add(InvokeDescriptor(opcode, owner, name, descriptor, isInterface))
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }
}