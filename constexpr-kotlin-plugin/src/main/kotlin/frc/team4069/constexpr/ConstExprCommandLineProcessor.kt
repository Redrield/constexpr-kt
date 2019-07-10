package frc.team4069.constexpr

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_ENABLED = CompilerConfigurationKey<Boolean>("enabled")

@AutoService(CommandLineProcessor::class)
class ConstExprCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "constexpr"
    override val pluginOptions = listOf(
        CliOption("enabled", "<true|false>", "Whether plugin is enabled")
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) = when(option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> error("Unexpected config option ${option.optionName}")
    }
}