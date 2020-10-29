package jmap

import com.dimafeng.testcontainers.GenericContainer
import jmap.Jmap.Service
import jmap.JmapSpec.JmapServerConfiguration
import org.apache.commons.net.imap.IMAPClient
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.HeaderNames
import zio.test.Assertion._
import zio.test._
import zio.{Task, _}

object Jmap {

  trait Service {
    def mailboxGetBasicAuth(): IO[Throwable, Response[String]]

    def mailboxGetBearerAuth(): IO[Throwable, Response[String]]
  }

  class JmapClient(configuration: JmapServerConfiguration) extends Jmap.Service {
    override def mailboxGetBasicAuth(): IO[Throwable, Response[String]] = AsyncHttpClientZioBackend().flatMap { implicit backend => {
      val request = basicRequest
        .auth.basic("bob", "bob")
        .post(uri"${configuration.url}")
        .contentType("application/json")
        .header(HeaderNames.Accept, "application/json", replaceExisting = true)
        .body(
          """
            | {
            |    "using": [ "urn:ietf:params:jmap:core", "urn:ietf:params:jmap:mail" ],
            |    "methodCalls": [[ "Mailbox/get", { }, "c1" ]]
            | }
            |""".stripMargin)
        .response(asStringAlways)

      backend.send(request)
    }
    }

    override def mailboxGetBearerAuth(): IO[Throwable, Response[String]] = AsyncHttpClientZioBackend().flatMap { implicit backend => {
      val request = basicRequest
        .auth.bearer("bob")
        .post(uri"${configuration.url}")
        .contentType("application/json")
        .header(HeaderNames.Accept, "application/json", replaceExisting = true)
        .body(
          """
            | {
            |    "using": [ "urn:ietf:params:jmap:core", "urn:ietf:params:jmap:mail" ],
            |    "methodCalls": [[ "Mailbox/get", { }, "c1" ]]
            | }
            |""".stripMargin)
        .response(asStringAlways)

      backend.send(request)
    }
    }
  }

  val live: ZLayer[Has[JmapServerConfiguration], Nothing, Has[Service]] = ZLayer.fromService[JmapServerConfiguration, Service](configuration => new JmapClient(configuration))
}

object JmapSpec extends DefaultRunnableSpec {

  val container = GenericContainer("cyrus-jmap").configure(configProvider => {
    configProvider.addExposedPort(143)
    configProvider.addExposedPort(80)
  })

  case class JmapServerConfiguration(imapPort: Int, jmapPort: Int, url: String)

  private val createInboxTask: Task[_] = Task.fromFunction(_ => {
    val imapClient = new IMAPClient()
    imapClient.connect(container.container.getHost, container.container.getMappedPort(143))
    imapClient.login("bob", "bob")
    imapClient.create("INBOX")
  })
  val jmapServerConfig = ZManaged.make(
    Task(container.start) *> createInboxTask *> Task(JmapServerConfiguration(
      container.container.getMappedPort(143),
      container.container.getMappedPort(80),
      s"http://${container.container.getHost}:${container.container.getMappedPort(80)}/jmap/"
    ))
  )(_ => UIO(container.stop))

  val jmapClient = (
    ZLayer.fromManaged(jmapServerConfig) >>> Jmap.live
    ).mapError(TestFailure.die)

  def spec = {
    val mailboxGetBasic = testM[Service, Throwable]("Mailbox/get basic auth witout parameter should list the user mailboxes") {
      for {
        response <- ZIO.fromFunctionM[Service, Throwable, Response[String]](jmapService => jmapService.mailboxGetBasicAuth())
      } yield assert(response.body)(equalTo("response content"))
    }
    val mailboxGetBearer = testM[Service, Throwable]("Mailbox/get bearer auth witout parameter should list the user mailboxes") {
      for {
        response <- ZIO.fromFunctionM[Service, Throwable, Response[String]](jmapService => jmapService.mailboxGetBearerAuth())
      } yield assert(response.body)(equalTo("response content"))
    }
    suite("JmapSpec")(
      mailboxGetBasic,
      mailboxGetBearer
    ).provideLayer(jmapClient.map(_.get))
  }
}
