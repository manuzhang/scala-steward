package eu.timepit.scalasteward.git

import eu.timepit.scalasteward.MockState
import eu.timepit.scalasteward.MockState.MockEnv
import eu.timepit.scalasteward.github.data.Repo
import eu.timepit.scalasteward.io.{MockFileAlg, MockProcessAlg, MockWorkspaceAlg}
import org.http4s.Uri
import org.scalatest.{FunSuite, Matchers}

class GitAlgTest extends FunSuite with Matchers {
  implicit val fileAlg: MockFileAlg = new MockFileAlg
  implicit val processAlg: MockProcessAlg = new MockProcessAlg
  implicit val workspaceAlg: MockWorkspaceAlg = new MockWorkspaceAlg
  val gitAlg: GitAlg[MockEnv] = GitAlg.create
  val repo = Repo("fthomas", "datapackage")

  test("branchAuthors") {
    val state = gitAlg
      .branchAuthors(repo, Branch("update/cats-1.0.0"), Branch("master"))
      .runS(MockState.empty)
      .value

    state shouldBe MockState.empty.copy(
      commands = Vector(
        List("git", "log", "--pretty=format:'%an'", "master..update/cats-1.0.0")
      )
    )
  }

  test("syncFork") {
    val url = Uri.uri("http://github.com/fthomas/datapackage")
    val defaultBranch = Branch("master")

    val state = gitAlg
      .syncFork(repo, url, defaultBranch)
      .runS(MockState.empty)
      .value

    state shouldBe MockState.empty.copy(
      commands = Vector(
        List("git", "remote", "add", "upstream", "http://github.com/fthomas/datapackage"),
        List("git", "fetch", "upstream"),
        List("git", "checkout", "-B", "master", "--track", "upstream/master"),
        List("git", "merge", "upstream/master"),
        List("git", "push", "--force", "--set-upstream", "origin", "master")
      )
    )
  }
}
