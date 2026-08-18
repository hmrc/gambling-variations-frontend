import sbt.*
import sbt.Keys.*
import sbt.internal.util.MessageOnlyException

import java.io.File
import scala.sys.process.Process

// Verifies conf/messages files are well-formed via the msgman CLI,
// installing it from GitHub releases if it is not already available.
object Msgman {

  private val repository = "https://github.com/dboresjo/msgman"

  // The version installed when no msgman is found. A msgman that is already installed is used as-is.
  private val installVersion = "1.0.0"

  private val installDir = file(sys.props.getOrElse("user.home", ".")) / ".local" / "bin"
  private val manualInstallHint = s"Install it manually from $repository/releases"

  val msgmanVerify = taskKey[Unit]("Verify conf/messages files are in canonical order via msgman")
  val msgmanFormat = taskKey[Unit]("Rewrite conf/messages files into canonical order, adding missing translations, via msgman")

  // A published release asset, and the path of the msgman binary inside it.
  private case class ReleaseAsset(name: String, binaryPath: String)

  private def releaseAsset(osName: String, osArch: String): Option[ReleaseAsset] =
    (osName.toLowerCase, osArch.toLowerCase) match {
      case (os, arch) if os.contains("linux") && (arch.contains("amd64") || arch.contains("x86_64")) =>
        Some(ReleaseAsset(s"msgman_${installVersion}_amd64.deb", "usr/bin/msgman"))
      case (os, arch) if os.contains("linux") && (arch.contains("aarch64") || arch.contains("arm64")) =>
        Some(ReleaseAsset(s"msgman_${installVersion}_arm64.deb", "usr/bin/msgman"))
      case (os, arch) if os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) =>
        Some(ReleaseAsset(s"msgman_${installVersion}_darwin_arm64_macos15.tar.gz", "msgman"))
      case _ => None
    }

  private def exec(command: Seq[String]): Int = Process(command).!

  // Downloads and extracts a release into installDir and returns the installed binary, or a
  // description of why that was not possible.
  private def download(log: Logger): Either[String, File] = {
    val osName = sys.props("os.name")
    val osArch = sys.props("os.arch")

    releaseAsset(osName, osArch)
      .toRight(s"No msgman release is published for this platform ($osName/$osArch). $manualInstallHint")
      .flatMap { asset =>
        val url = s"$repository/releases/download/v$installVersion/${asset.name}"
        log.info(s"msgman not found, downloading ${asset.name} ...")

        IO.withTemporaryDirectory { tempDir =>
          val archive = tempDir / asset.name
          val extractDir = tempDir / "extract"
          IO.createDirectory(extractDir)

          // --fail is essential here: without it curl writes the HTTP error body to the output file
          // and still exits 0, which would show up later as a confusing extraction failure.
          val curl =
            Seq("curl", "--fail", "--silent", "--show-error", "--location", "--output", archive.getAbsolutePath, url)

          val extract =
            if (asset.name.endsWith(".deb")) Seq("dpkg-deb", "-x", archive.getAbsolutePath, extractDir.getAbsolutePath)
            else Seq("tar", "-xzf", archive.getAbsolutePath, "-C", extractDir.getAbsolutePath)

          val binary = installDir / "msgman"

          if (exec(curl) != 0) Left(s"Failed to download msgman from $url. $manualInstallHint")
          else if (exec(extract) != 0) Left(s"Failed to extract ${asset.name}. $manualInstallHint")
          else {
            IO.createDirectory(installDir)
            IO.copyFile(extractDir / asset.binaryPath, binary)
            if (!binary.setExecutable(true))
              Left(s"Failed to make ${binary.getAbsolutePath} executable. $manualInstallHint")
            else {
              log.info(s"msgman installed to ${binary.getAbsolutePath}")
              Right(binary)
            }
          }
        }
      }
  }

  // Looks up msgman on PATH without spawning a process, so that a missing binary does not have to
  // be detected by catching the exception from a failed process start.
  private def onPath: Option[File] =
    sys.env
      .getOrElse("PATH", "")
      .split(File.pathSeparatorChar)
      .iterator
      .map(dir => file(dir) / "msgman")
      .find(_.canExecute)

  // A msgman that is already installed is preferred, whether it is on PATH or was left in
  // installDir by an earlier build on a machine where that directory is not on PATH. Only when
  // there is none does the build install one.
  private def resolve(log: Logger): Either[String, File] =
    onPath.orElse(Some(installDir / "msgman").filter(_.canExecute)) match {
      case Some(binary) => Right(binary)
      case None         => download(log)
    }

  // Runs msgman with the given arguments against the messages files under baseDir. The step is
  // skipped with a warning, rather than failing, when no msgman could be found or installed, so that
  // an unsupported platform or an unreachable GitHub does not stop anyone from building.
  private def runMsgman(arguments: Seq[String], baseDir: File, log: Logger, failureMessage: String): Unit =
    resolve(log) match {
      case Left(reason) =>
        log.warn(s"Skipping msgman ${arguments.mkString(" ")}. $reason")

      case Right(binary) =>
        if (Process(binary.getAbsolutePath +: arguments, baseDir).! != 0)
          throw new MessageOnlyException(failureMessage)
    }

  val settings: Seq[Def.Setting[?]] = Seq(
    msgmanVerify := runMsgman(
      Seq("verify"),
      baseDirectory.value,
      streams.value.log,
      "msgman verify failed: conf/messages files are malformed"
    ),
    // --fix also adds any missing translation as a placeholder, so that a formatted tree is one that
    // msgmanVerify accepts, and compiling is not blocked by a translation that nobody has written yet.
    msgmanFormat := runMsgman(
      Seq("format", "--fix"),
      baseDirectory.value,
      streams.value.log,
      "msgman format failed: conf/messages files could not be rewritten"
    ),
    Compile / compile := (Compile / compile).dependsOn(msgmanVerify).value
  )
}
