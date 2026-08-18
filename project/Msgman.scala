import sbt.*
import sbt.Keys.*
import sbt.internal.util.MessageOnlyException

import java.io.File
import scala.sys.process.Process

// Verifies conf/messages files are in canonical order via the msgman CLI (https://github.com/dboresjo/msgman),
// downloading it from GitHub releases into a local cache if it isn't already installed.
object Msgman {

  private val msgmanRepository = "https://github.com/dboresjo/msgman"
  private val msgmanVersion = "1.0.0"

  val verify = taskKey[Unit]("Verify conf/messages files are in canonical order via msgman")

  // Resolves the platform-specific release asset for the pinned msgmanVersion, and the path
  // to the msgman binary inside it once downloaded and extracted.
  private def releaseAsset: Option[(String, String)] =
    (sys.props("os.name").toLowerCase, sys.props("os.arch").toLowerCase) match {
      case (os, arch) if os.contains("linux") && (arch.contains("amd64") || arch.contains("x86_64")) =>
        Some((s"msgman_${msgmanVersion}_amd64.deb", "usr/bin/msgman"))
      case (os, arch) if os.contains("linux") && (arch.contains("aarch64") || arch.contains("arm64")) =>
        Some((s"msgman_${msgmanVersion}_arm64.deb", "usr/bin/msgman"))
      case (os, arch) if os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) =>
        Some((s"msgman_${msgmanVersion}_darwin_arm64_macos15.tar.gz", "msgman"))
      case _ => None
    }

  // Downloads and extracts msgman into installDir, without requiring root, returning the path to
  // the installed binary. Returns None (having warned) if the platform has no published release
  // asset, or the download/extraction fails.
  private def download(installDir: File, log: Logger): Option[File] =
    releaseAsset match {
      case None =>
        log.warn(
          s"No msgman release available for this platform (${sys.props("os.name")}/${sys.props("os.arch")}). " +
            s"Install it manually from ${msgmanRepository}/releases"
        )
        None

      case Some((assetName, entryPath)) =>
        log.info(s"msgman not found, downloading $assetName ...")
        val downloadUrl = s"${msgmanRepository}/releases/download/v$msgmanVersion/$assetName"

        IO.withTemporaryDirectory { tempDir =>
          val downloadedFile = tempDir / assetName

          if (Process(Seq("curl", "-sL", "-o", downloadedFile.getAbsolutePath, downloadUrl)).! != 0) {
            log.warn(s"Failed to download msgman from $downloadUrl. Install it manually from ${msgmanRepository}/releases")
            None
          } else {
            val extractDir = tempDir / "extract"
            IO.createDirectory(extractDir)
            val extractExit =
              if (assetName.endsWith(".deb"))
                Process(Seq("dpkg-deb", "-x", downloadedFile.getAbsolutePath, extractDir.getAbsolutePath)).!
              else
                Process(Seq("tar", "-xzf", downloadedFile.getAbsolutePath, "-C", extractDir.getAbsolutePath)).!

            if (extractExit != 0) {
              log.warn(s"Failed to extract msgman package. Install it manually from ${msgmanRepository}/releases")
              None
            } else {
              IO.createDirectory(installDir)
              val target = installDir / "msgman"
              IO.copyFile(extractDir / entryPath, target)
              target.setExecutable(true)
              log.info(s"msgman installed to ${target.getAbsolutePath}")
              Some(target)
            }
          }
        }
    }

  val settings: Seq[Def.Setting[?]] = Seq(
    verify := {
      val log = streams.value.log
      val installDir = new File(sys.props("user.home"), ".local/bin")
      val installedBinary = installDir / "msgman"

      def run(command: String): Int = Process(Seq(command, "verify"), baseDirectory.value).!

      val exitCode: Option[Int] =
        try Some(run("msgman"))
        catch {
          case _: java.io.IOException =>
            if (installedBinary.canExecute) Some(run(installedBinary.getAbsolutePath))
            else download(installDir, log).map(binary => run(binary.getAbsolutePath))
        }

      exitCode.foreach { code =>
        if (code != 0) throw new MessageOnlyException("msgman verify failed: conf/messages files are malformed")
      }
    },
    Compile / compile := (Compile / compile).dependsOn(verify).value
  )
}
