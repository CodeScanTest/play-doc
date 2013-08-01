package play.doc

import org.specs2.mutable.Specification
import java.io.File
import org.apache.commons.io.IOUtils
import java.util.jar.JarFile

object FileRepositorySpec extends Specification {

  def fileFromClasspath(name: String) = new File(Thread.currentThread.getContextClassLoader.getResource(name).toURI)
  def loadFileFromRepo(repo: FileRepository, path: String) = repo.loadFile(path)(IOUtils.toString)
  def handleFileFromRepo(repo: FileRepository, path: String) = repo.handleFile(path) { handle =>
    val result = (handle.name, handle.size, IOUtils.toString(handle.is))
    handle.close()
    result
  }

  "FilesystemRepository" should {
    val repo = new FilesystemRepository(fileFromClasspath("file-placeholder").getParentFile)
    def loadFile(path: String) = loadFileFromRepo(repo, path)
    def handleFile(path: String) = handleFileFromRepo(repo, path)
    import repo.findFileWithName

    "load a file" in {
      loadFile("example/docs/Foo.md") must beSome("Some markdown")
    }

    "return none when file not found" in {
      loadFile("example/NotFound.md") must beNone
    }

    "return none when file is a directory" in {
      loadFile("example/docs") must beNone
    }

    "handle a file" in {
      handleFile("example/docs/Foo.md") must beSome(("Foo.md", 13, "Some markdown"))
    }

    "handle a missing file" in {
      handleFile("example/NotFound.md") must beNone
    }

    "find a file with a name" in {
      findFileWithName("Foo.md") must beSome("example/docs/Foo.md")
    }

    "return none when a file with a name is not found" in {
      findFileWithName("NotFound.md") must beNone
    }

    "return none when a file with a name is a directory" in {
      findFileWithName("docs") must beNone
    }
  }

  "JarRepository" should {
    def withJarRepo[T](block: JarRepository => T): T = {
      val repo = new JarRepository(new JarFile(fileFromClasspath("example-jar-repo.jar")))
      try {
        block(repo)
      } finally {
        repo.close()
      }
    }

    def loadFile(path: String) = withJarRepo(loadFileFromRepo(_, path))
    def handleFile(path: String) = withJarRepo(handleFileFromRepo(_, path))
    def findFileWithName(name: String) = withJarRepo(_.findFileWithName(name))

    "load a file" in {
      loadFile("example/docs/Foo.md") must beSome("Some markdown")
    }

    "return none when file not found" in {
      loadFile("example/NotFound.md") must beNone
    }

    "return none when file is a directory" in {
      loadFile("example/docs") must beNone
    }

    "handle a file" in {
      handleFile("example/docs/Foo.md") must beSome(("Foo.md", 13, "Some markdown"))
    }

    "handle a missing file" in {
      handleFile("example/NotFound.md") must beNone
    }

    "find a file with a name" in {
      findFileWithName("Foo.md") must beSome("example/docs/Foo.md")
    }

    "return none when a file with a name is not found" in {
      findFileWithName("NotFound.md") must beNone
    }

    "return none when a file with a name is a directory" in {
      findFileWithName("docs") must beNone
    }
  }
}
