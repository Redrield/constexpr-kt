package frc.team4069.constexpr.visitor

import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

class AnnotationRemoverVisitor(fv: FieldVisitor) : FieldVisitor(Opcodes.ASM5, fv) {
//    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        return if(descriptor == "frc.team4069.constexpr.ConstExpr") {
//            null
//        } else fv.visitAnnotation(descriptor, visible)
//    }
}