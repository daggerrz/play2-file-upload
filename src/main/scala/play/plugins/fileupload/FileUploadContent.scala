package play.plugins.fileupload

import collection.TraversableOnce
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.{ FileItemStream, RequestContext, FileUpload }
import java.io._
import play.api.mvc.BodyParser
import play.api.libs.iteratee.Iteratee

object FileUploadPlugin

/**
 * Contains the items (files and fields) associated with the upload. Please observe that
 * traversing the items might destroy them as the temporary files might be wiped.
 */
case class FileUploadContent(items: TraversableOnce[FileItemStream])

object FileUploadContent {

  import play.api.http.HeaderNames._

  private val repo = new java.io.File("var/uploads")
  repo.mkdirs()
  lazy val fileUpload = new FileUpload(new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repo))

  private[fileupload] def parseBoundary(contentTypeHeader: Option[String]) = {
    contentTypeHeader.flatMap(_.split(";").flatMap(_.split("=").map(_.trim) match {
      case Array("boundary", b) => Some(b);
      case _ => None
    }).headOption) match {
      case Some(b) => b.getBytes("UTF-8")
      case _ => throw new RuntimeException("Invalid multi-part request. No boundary defined.")
    }
  }

  def parser: BodyParser[FileUploadContent] = BodyParser { requestHeader =>

    // This is kind of ridiculous, but we're first writing
    // the entire file body to a file, then we're piping this to the
    // commons library (which might also write several files).
    val tempFile = File.createTempFile("_part_upload", ".tmp", repo)
    tempFile.deleteOnExit()

    val fileOut = new FileOutputStream(tempFile)

    val body = Iteratee.fold[Array[Byte], Int](0) {
      case (state, newBytes) =>
        fileOut.write(newBytes)
        state + newBytes.length
    }

    body.mapDone { content =>
      fileOut.close()
      val fileIn = new FileInputStream(tempFile)

      val iterator = fileUpload.getItemIterator(new RequestContext {
        def getCharacterEncoding = "UTF-8"

        def getContentLength = requestHeader.headers.get(CONTENT_LENGTH).map(_.toInt).getOrElse(0)

        def getContentType = requestHeader.headers.get(CONTENT_TYPE).orNull

        def getInputStream = fileIn
      })

      val itemIterator = new Iterator[FileItemStream] {
        def hasNext = {
          // Clean up our temp file when we know commons has parsed our entire stream
          if (!iterator.hasNext) {
            try {
              fileIn.close()
              tempFile.delete()
            } finally {
              // Noop
            }
            false
          } else true
        }
        def next() = iterator.next()
      }
      Right(FileUploadContent(itemIterator))
    }
  }
}