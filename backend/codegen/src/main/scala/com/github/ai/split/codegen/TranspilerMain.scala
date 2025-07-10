package com.github.ai.split.codegen

import com.github.ai.split.codegen.model.{AppError, IOError, InvalidArgumentsError}
import zio.*

import java.io.File
import java.io.PrintWriter
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object TranspilerMain extends ZIOAppDefault {

  def run: ZIO[ZIOAppArgs, Any, ExitCode] = {
    val program = for {
      arguments <- ZIOAppArgs.getArgs.flatMap(args => parseArguments(args.toList))
      scalaFiles <- findScalaFiles(arguments.sourceDir)
      transpiledFiles <- transpile(scalaFiles, arguments.destinationDir)
    } yield ExitCode.success

    program
      .catchAll { error =>
        Console.printLineError(s"Error: $error") *> ZIO.succeed(ExitCode.failure)
      }
  }

  case class Arguments(sourceDir: String, destinationDir: String)

  private def parseArguments(args: List[String]): IO[AppError, Arguments] = {
    args match {
      case sourceDir :: destinationDir :: Nil =>
        ZIO.succeed(Arguments(sourceDir, destinationDir))
      case _ =>
        ZIO.fail(InvalidArgumentsError(message = s"Failed to parse arguments"))
    }
  }

  private def findScalaFiles(
    rootPath: String
  ): IO[AppError, List[FilePath]] = {
    ZIO.attempt {
        val scalaFiles = ListBuffer[File]()
        val queue = mutable.Queue[File]()

        queue.addOne(File(rootPath))

        while (queue.nonEmpty) {
          val dir = queue.remove(0)
          if (dir.isDirectory) {
            val childFiles = dir.listFiles().toList

            for (childFile <- childFiles) {
              if (childFile.isDirectory) {
                queue.addOne(childFile)
              } else if (childFile.getName.endsWith(".scala")) {
                scalaFiles.addOne(childFile)
              }
            }
          }
        }

        scalaFiles.toList
          .map(file => FilePath(root = rootPath, path = file.getPath.stripPrefix(rootPath)))
      }
      .mapError(error => IOError(exception = error))
  }

  private def transpile(
    files: List[FilePath],
    outputDir: String
  ): IO[AppError, List[FilePath]] = {
    val transpiler = ScalaToKotlinTranspiler()

    val outputFiles = files.map { file =>
      for {
        scalaContent <- readFile(file)
        kotlinCode <- transpiler.transpile(scalaContent)
        destinationFile <- createDestinationFile(inputFile = file, outputDir = outputDir)

        _ <- Console.printLine(s"Write file: ${destinationFile.toJavaFile().getPath}").mapError(IOError(_))

        result <- writeFile(file = destinationFile, content = kotlinCode)
      } yield result
    }

    ZIO.collectAll(outputFiles)
  }

  private def createDestinationFile(
    inputFile: FilePath,
    outputDir: String
  ): IO[AppError, FilePath] = {
    ZIO.succeed(
      FilePath(
        root = outputDir,
        path = inputFile.path.replace(".scala", ".kt")
      )
    )
  }

  private def readFile(file: FilePath): IO[AppError, String] = {
    ZIO.attempt {
        val source = Source.fromFile(file.toJavaFile())
        try source.mkString
        finally source.close()
      }
      .mapError(IOError)
  }

  private def writeFile(
    file: FilePath,
    content: String
  ): IO[AppError, FilePath] = {
    ZIO.attempt {
        val parent = file.toJavaFile().getParentFile
        if (!parent.exists()) {
          parent.mkdirs()
        }

        val pw = new PrintWriter(file.toJavaFile())
        try {
          pw.write(content)
          pw.flush()
        }
        finally pw.close()
      }
      .map(_ => file)
      .mapError(IOError)
  }
}

case class FilePath(root: String, path: String) {
  def toJavaFile(): File = File(root, path)
}