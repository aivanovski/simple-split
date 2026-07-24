package com.github.ai.split.openapi

import org.yaml.snakeyaml.{DumperOptions, Yaml}

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}

object GenerateOpenApi {
  def main(args: Array[String]): Unit = {
    val output = args.headOption
      .map(Path.of(_))
      .getOrElse(Path.of("openapi.yaml"))

    val options = DumperOptions()
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    options.setPrettyFlow(true)
    options.setSplitLines(false)

    val yaml = Yaml(options)
    val openApiData = yaml.load[Any](ApiEndpoints.openApi.toJson)
    val contents = yaml.dump(openApiData)

    Option(output.getParent).foreach(Files.createDirectories(_))
    Files.writeString(output, contents, UTF_8)
  }
}
