
object TestApp {
//    @ConstExpr
    val x: Long = System.currentTimeMillis()
    val k = y(100)

    @JvmStatic fun y(z: Int): String {
        return ":ha:"
    }
}