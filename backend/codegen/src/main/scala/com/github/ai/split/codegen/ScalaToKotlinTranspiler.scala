package com.github.ai.split.codegen

import com.github.ai.split.codegen.ScalaToKotlinTranspiler.{caseClassRegex, fieldRegex, importRegex, packageRegex}
import com.github.ai.split.codegen.model.{KotlinType, ScalaSyntaxError, AppError, Field}
import zio.*

class ScalaToKotlinTranspiler {

  def transpile(input: String): IO[AppError, String] = {
    for {
      packageName <- parsePackageName(input)
      imports <- parseImports(input)
      className <- parseClassName(input)
      fields <- parseFields(input)

      transpiledImports <- transpileImports(imports)
      transpiledFields <- transpileFields(fields)

      result <- formatKotlinClass(
        KotlinType(
          packageName = packageName,
          imports = transpiledImports,
          typeName = className,
          fields = transpiledFields
        )
      )
    } yield result
  }

  private def parsePackageName(input: String): IO[AppError, String] = {
    val packageName = packageRegex.findFirstMatchIn(input)
      .map(m => m.group(1).trim)

    if (packageName.isDefined) ZIO.succeed(packageName.getOrElse(""))
    else ZIO.fail(ScalaSyntaxError(message = "Failed to parse 'package' block"))
  }

  private def parseImports(input: String): IO[AppError, List[String]] = {
    val imports = importRegex.findAllIn(input)
      .map { line => line }
      .toList

    ZIO.succeed(imports)
  }

  private def parseClassName(input: String): IO[AppError, String] = {
    val className = caseClassRegex.findFirstMatchIn(input)
      .map(m => m.group(1).trim)

    if (className.isDefined) ZIO.succeed(className.getOrElse(""))
    else ZIO.fail(ScalaSyntaxError(message = "Failed to parse class name"))
  }

  private def parseFields(input: String): IO[AppError, List[String]] = {
    val allFields = caseClassRegex.findFirstMatchIn(input)
      .map(m => m.group(2).trim)

    if (allFields.isDefined) ZIO.succeed(allFields.getOrElse("").trim.split(",").toList)
    else ZIO.fail(ScalaSyntaxError(message = "Failed to parse 'case class' fields"))
  }

  private def transpileImports(imports: List[String]): IO[AppError, List[String]] = {
    val filteredImports = imports.map(line => transpileImport(line))

    ZIO.collectAll(filteredImports)
      .map(lines =>
        val transpiledImports = lines
          .filter(line => line.isDefined)
          .map(line => line.get)

        List("import kotlinx.serialization.Serializable") ++ transpiledImports
      )
  }

  private def transpileImport(line: String): IO[AppError, Option[String]] = {
    val trimmedLine = line.trim.replaceAll("_", "*")
    if (trimmedLine.isEmpty || trimmedLine.startsWith("import zio")) {
      return ZIO.succeed(None)
    }


    if (trimmedLine.contains("{") || trimmedLine.contains("}")) {
      val bracketStartIdx = trimmedLine.indexOf("{")
      val bracketEndIdx = trimmedLine.lastIndexOf("}")
      if (bracketStartIdx < 0 || bracketEndIdx < 0 || bracketStartIdx >= bracketEndIdx) {
        return ZIO.fail(ScalaSyntaxError(message = s"Unable to transpile '$line'"))
      }

      ZIO.succeed(Some(trimmedLine.substring(0, bracketStartIdx) + "*"))
    } else {
      ZIO.succeed(Some(trimmedLine))
    }
  }

  private def transpileFields(fields: List[String]): IO[AppError, List[Field]] = {
    ZIO.collectAll(fields.map(field => transpileField(field)))
  }

  private def transpileField(field: String): IO[AppError, Field] = {
    val cleanedField = field
      .replaceAll("val", "")
      .replaceAll("var", "")
      .trim

    val nameAndType = fieldRegex.findFirstMatchIn(cleanedField)
      .map(m => (m.group(1), m.group(2)))

    if (nameAndType.isEmpty) {
      return ZIO.fail(ScalaSyntaxError(message = s"Failed to transpile variable '$field'"))
    }

    val (fieldName, fieldType) = nameAndType.get

    if (fieldType.contains("[") && fieldType.contains("]")) {
      val bracketStartIdx = fieldType.indexOf("[")
      val bracketEndIdx = fieldType.lastIndexOf("]")

      if (bracketStartIdx < 0 || bracketEndIdx < 0) {
        return ZIO.fail(ScalaSyntaxError(message = s"Failed to transpile variable '$field'"))
      }

      val genericType = fieldType.substring(0, bracketStartIdx)
      val parameterType = fieldType.substring(bracketStartIdx + 1, bracketEndIdx)
        .replaceAll("\\[", "<")
        .replaceAll("]", ">")

      if (genericType == "Option") {
        ZIO.succeed(
          Field(
            name = fieldName,
            fieldType = s"$parameterType?"
          )
        )
      } else {
        ZIO.succeed(
          Field(
            name = fieldName,
            fieldType = fieldType.replaceAll("\\[", "<").replaceAll("]", ">"))
        )
      }
    } else {
      ZIO.succeed(
        Field(
          name = fieldName,
          fieldType = fieldType
        )
      )
    }
  }

  private def formatKotlinClass(tpe: KotlinType): IO[AppError, String] = {
    val sb = StringBuilder()

    sb.append(s"package ${tpe.packageName}\n")

    if (tpe.imports.nonEmpty) {
      sb.append("\n")
      sb.append(tpe.imports.mkString("\n"))
      sb.append("\n\n")
    } else {
      sb.append("\n")
    }

    sb.append("@Serializable\n")
    sb.append(s"data class ${tpe.typeName}(\n")
    for ((field, index) <- tpe.fields.zipWithIndex) {
      sb.append(s"    val ${field.name}: ${field.fieldType}")
      if (index != tpe.fields.size - 1) {
        sb.append(",")
      }
      sb.append("\n")
    }
    sb.append(")")

    ZIO.succeed(sb.toString())
  }
}

object ScalaToKotlinTranspiler {
  private val importRegex = """(?m)^import.*""".r
  private val packageRegex = """^\s*package\s+(\S+)""".r
  private val caseClassRegex = """case class (\w+)\(\s*([\s\S]*?)\s*\)""".r
  private val fieldRegex = """(\w+):\s+([\w\[\]]+)""".r
}