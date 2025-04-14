import org.gradle.api.provider.Provider
import org.gradle.internal.cc.base.logger
import org.gradle.plugin.use.PluginDependency

object Utils {

    fun Provider<PluginDependency>.id(): String = this.get().pluginId

    val inCI: Boolean
        get() = System.getenv("CI") == true.toString()

    val onLinux: Boolean
        get() = os().contains("linux", ignoreCase = true)

    val onMac: Boolean
        get() = os().contains("mac", ignoreCase = true)

    val onWindows: Boolean
        get() = os().contains("windows", ignoreCase = true)

    private fun os(): String = System.getProperty("os.name")

    fun normally(todo: () -> Unit): Normally = Normally(todo)

    class Normally(private val normallyBlock: () -> Unit) {
        infix fun except(condition: () -> Boolean): Conditionally = Conditionally(normallyBlock, condition)
    }

    class Conditionally(private val normallyBlock: () -> Unit, private val condition: () -> Boolean) {
        infix fun where(exceptionalBlock: () -> Unit): ConditionallyWithCause = with(condition()) {
            if (this) exceptionalBlock() else normallyBlock()
            ConditionallyWithCause(this)
        }
    }

    class ConditionallyWithCause(private val condition: Boolean) {
        infix fun cause(reason: String) = if (condition) logger.quiet(reason) else Unit
    }
}
