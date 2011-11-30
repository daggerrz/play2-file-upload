package play.plugins.fileupload

import org.specs2.mutable._
import play.plugins.fileupload.FileUploadContent

object FileUploadContentSpec extends Specification {

  object FileUploadRequest extends Request[AnyContent] {
    def uri = "foo"
    def method = "GET"
    def queryString = Map.empty
    def body: AnyContent = AnyContentUnknown(Array())

    def username = Some("peter")
    def path = "/"

    def headers = new Headers {
      private val h = Map("Content-Type" -> "multipart/form-data, boundary=------------------------------c2a02946888a")
      def getAll(key: String) = h.get(key).map(Seq(_)).getOrElse(Seq.empty)
    }
    def cookies = new Cookies {
      def get(name: String) = None
    }
  }

  "FileUploadParser" should {
    "parse boundary correctly" in {
      FileUploadContent.parseBoundary(Some("boundary=foo")) must_== "foo"
    }

    "parse correctly" in {
      val inputData = new java.io.FileInputStream("play/src/test/scala/play/api/mvc/file-upload-data.txt")
      val parser = FileUploadContent.parser(FileUploadRequest)
      import play.api.libs.iteratee._
      Stream.continually(inputData.read).takeWhile(-1 !=).map(_.toByte).grouped(18).map(_.toArray).foreach { bytes =>
        parser.feed(El(bytes))
      }
      parser.feed(EOF)
      val res = parser.run.value.get
      res.files.toTraversable.size must_== 2
    }
  }
}