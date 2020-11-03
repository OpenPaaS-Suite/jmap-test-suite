package jmap

import sttp.client.{RequestT, Response}
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.HeaderNames
import zio.{Has, IO, ZLayer}

object Jmap {

  case class JmapServerConfiguration(imapPort: Int, jmapPort: Int, smtpPort: Int, url: String, `type`: JMAPServer) {
    // TODO use session to get the right account Id automatically
    val accountId = `type` match {
      case James => "f8614bd254a46c94ce6c6ef8131421826a8a7bb39c9e9cfc7b706b7d2aa178b1"
      case Cyrus => "bob"
    }
  }

  trait Service {
    def mailboxGet(): IO[Throwable, Response[String]]
  }

  class JmapClient(configuration: JmapServerConfiguration) extends Service {

    def jamesRequest(request: RequestT[Identity, String, Nothing]): RequestT[Identity, String, Nothing] = {
      val BOB_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJib2JAam1hcC50ZXN0In0.q6Ut65YDwa6im2nl2bPTc-VkhMrxPv7ewZ-ms-J23jwt2FJosm2MP-QmcW2w6j6N7G9jO3G-t_y1VYyAP2gtgSHDKuF1oft9tlggn7wrdUyRvLKmTQ3KtqswXhzM_93E2UOPdsyf5GJqfUQy-wDYcY250xKaXp5sCOtaXo9PzlSH-INotYNZesXJhM-I08AB9kxYsGguHBnPBxzoJ1yzyy0hDJqpbK8T-Vd1K7y0WnlFyfh5PJlaWMvi4HM61SsfjqmAI8XzhsF1GB6_tE9AWoc4fMyS6X3T528S3sq4dSNu98u3jV6RpyC4x-py9F4DW6Zz5u7jcAKRdnJZviz5zw"
      val ALICE_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZUBqbWFwLnRlc3QifQ.fA96ITSU1CsdQV001K0DlBoMLc-aKubDHTQg7Xipo5TABWWGlgFNrV53Bh_GuUc2d25Hhkg2Rh0KNifLXNdIXg5g52f0jjr2Vs3qqe3uNZ9EnRJ4hyrkvfHHLr0YEmlTRR1HitA_hMSdlUyKizyzVxP6lta_NHqFJOK-bvd8UDK_LXiKGQ72rs7lNxJBKaId8XfL_OQHTdynKEuLhlwC8xukm23pWjvLO0RC2yj7yiO5j3k-bls-DDw6fjRsws4MKKqkfc_H5IyIBomUErRKvb3j3qYGaDC4l4VcIw6EfrpIo5jWdZoaspccq_j2eThJNFg780GRi10USvo1BEfR5g"

      request.auth.bearer(BOB_JWT)
        .header(HeaderNames.Accept, "application/json; jmapVersion=rfc-8621", replaceExisting = true)
        .contentType("application/json")
    }

    def cyrusRequest(request: RequestT[Identity, String, Nothing]): RequestT[Identity, String, Nothing] = {
      request.auth.basic("bob", "bob")
        .header(HeaderNames.Accept, "application/json", replaceExisting = true)
        .contentType("application/json")
    }

    def sendRequest(request: RequestT[Identity, String, Nothing]) =
      AsyncHttpClientZioBackend().flatMap { implicit delegatedBackend =>
        // needed because when accessing /jmap Cyrus sends a redirect to /jmap/ and we need to keep the Authorization header and so not stripping this sensitive header
        val backend = new FollowRedirectsBackend(delegatedBackend, sensitiveHeaders = Set.empty)
        configuration.`type` match {
          case Cyrus => backend.send(cyrusRequest(request))
          case James => backend.send(jamesRequest(request))
        }
      }

    override def mailboxGet(): IO[Throwable, Response[String]] = {
      val request: RequestT[Identity, String, Nothing] = basicRequest
        .post(uri"${configuration.url}")
        .body(
          s"""
            | {
            |    "using": [ "urn:ietf:params:jmap:core", "urn:ietf:params:jmap:mail" ],
            |    "methodCalls": [[ "Mailbox/get", { "accountId":"${configuration.accountId}"}, "c1" ]]
            | }
            |""".stripMargin)
        .response(asStringAlways)
      sendRequest(request)
    }
  }

  val live: ZLayer[Has[JmapServerConfiguration], Nothing, Has[Service]] = ZLayer.fromService[JmapServerConfiguration, Service](configuration => new JmapClient(configuration))
}