package jmap

import jmap.Jmap.{JmapServerConfiguration, Service}
import sttp.client._
import zio.json._
import zio.test.Assertion._
import zio.test._
import zio.{test => _, _}

object JmapSpec extends DefaultRunnableSpec {

  private def init: RIO[JMAPServer, _] = RIO.fromFunction(server => server.init())

  val jmapClient: ZLayer[JMAPServer, TestFailure[Nothing], Has[Service]] = {
    val jmapServerConfig: ZManaged[JMAPServer, Throwable, JmapServerConfiguration] = ZManaged.make(ZIO.fromFunction[JMAPServer, Unit](server => server.container.start)
        *> init
        *> ZIO.fromFunction[JMAPServer, JmapServerConfiguration](server => JmapServerConfiguration(
            server.container.container.getMappedPort(143),
            server.container.container.getMappedPort(80),
            server.container.container.getMappedPort(25),
            s"http://${server.container.container.getHost}:${server.container.container.getMappedPort(80)}/jmap",
            server
          ))
      )(_ => ZIO.fromFunction[JMAPServer, Unit](server => server.container.stop))

    (ZLayer.fromManaged(jmapServerConfig) >>> Jmap.live).mapError(TestFailure.die)
  }

  def spec = {
    val mailboxGet = testM[Service, Throwable]("Mailbox/get without parameter should list the user mailboxes") {
      for {
        response <- ZIO.fromFunctionM[Service, Throwable, Response[String]](jmapService => jmapService.mailboxGet())
        jmapResponse <- response.body.fromJson[JmapResponse] match {
          case Left(s) => ZIO.fail(new RuntimeException(s"response was: $response\n  but got error: " + s))
          case util.Right(value) => ZIO.apply(value)
        }
      } yield {
        assert(jmapResponse.methodResponses(0).asInstanceOf[MailboxGet].arguments.list.map(_.role))(contains(Some(Role("inbox"))))
      }
    }

    def createSuite(jmapServer: JMAPServer) = {
      suite(s"JmapSpec ${jmapServer.name}")(
        mailboxGet.provideLayer(jmapClient.map(_.get)).provide(jmapServer)
      )
    }

    suite("jmap")(
      createSuite(Cyrus),
      createSuite(James)
    )
  }
}
