package com.nilemnod.mangareader.core.reader

data class WebGpuShader(val file: String, val name: String)

object WebGpuShaderCodec {

    private val FILE = Regex("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.wgsl""")

    fun serialize(shaders: List<WebGpuShader>): String =
        shaders.joinToString("\n") { "${it.file}\t${it.name}" }

    fun deserialize(text: String): List<WebGpuShader> = text.lineSequence()
        .mapNotNull { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@mapNotNull null
            val file = line.substring(0, tab)
            if (FILE.matches(file)) WebGpuShader(file, line.substring(tab + 1)) else null
        }
        .distinctBy { it.file }
        .toList()
}
