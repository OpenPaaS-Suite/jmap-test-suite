package jmap

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class MailboxQueryResponse(accountId: AccountId,
//                                queryState: QueryState,
//                                canCalculateChanges: CanCalculateChanges,
                                ids: Seq[MailboxId],
//                                position: Position,
//                                limit: Option[Limit]
                               )

object MailboxQueryResponse {
  implicit val decoder: JsonDecoder[MailboxQueryResponse] = DeriveJsonDecoder.gen[MailboxQueryResponse]
}
