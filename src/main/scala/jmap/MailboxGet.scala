package jmap

import zio.json._

final case class AccountId(id: String) extends AnyVal
object AccountId {
  implicit val decoder: JsonCodec[AccountId] = JsonCodec[AccountId](JsonEncoder[String].contramap[AccountId](_.id), JsonDecoder[String].map(AccountId(_)))
}
final case class State(id: String) extends AnyVal
object State {
  implicit val decoder: JsonDecoder[State] = JsonDecoder[String].map(State(_))
}
final case class MailboxId(id: String) extends AnyVal
object MailboxId {
  implicit val decoder: JsonDecoder[MailboxId] = JsonDecoder[String].map(MailboxId(_))
}
final case class MailboxName(id: String) extends AnyVal
object MailboxName {
  implicit val decoder: JsonDecoder[MailboxName] = JsonDecoder[String].map(MailboxName(_))
}
final case class Role(id: String) extends AnyVal
object Role {
  implicit val decoder: JsonDecoder[Role] = JsonDecoder[String].map(Role(_))
}
final case class SortOrder(id: Int) extends AnyVal
object SortOrder {
  implicit val decoder: JsonDecoder[SortOrder] = JsonDecoder[Int].map(SortOrder(_))
}
final case class TotalEmails(id: Int) extends AnyVal
object TotalEmails {
  implicit val decoder: JsonDecoder[TotalEmails] = JsonDecoder[Int].map(TotalEmails(_))
}
final case class UnreadEmails(id: Int) extends AnyVal
object UnreadEmails {
  implicit val decoder: JsonDecoder[UnreadEmails] = JsonDecoder[Int].map(UnreadEmails(_))
}
final case class TotalThreads(id: Int) extends AnyVal
object TotalThreads {
  implicit val decoder: JsonDecoder[TotalThreads] = JsonDecoder[Int].map(TotalThreads(_))
}
final case class UnreadThreads(id: Int) extends AnyVal
object UnreadThreads {
  implicit val decoder: JsonDecoder[UnreadThreads] = JsonDecoder[Int].map(UnreadThreads(_))
}
final case class IsSubscribed(id: Boolean) extends AnyVal
object IsSubscribed {
  implicit val decoder: JsonDecoder[IsSubscribed] = JsonDecoder[Boolean].map(IsSubscribed(_))
}

final case class MayReadItems(value: Boolean) extends AnyVal
object MayReadItems {
  implicit val decoder: JsonDecoder[MayReadItems] = JsonDecoder[Boolean].map(MayReadItems(_))
}
case class MayAddItems(value: Boolean) extends AnyVal
object MayAddItems {
  implicit val decoder: JsonDecoder[MayAddItems] = JsonDecoder[Boolean].map(MayAddItems(_))
}
case class MayRemoveItems(value: Boolean) extends AnyVal
object MayRemoveItems {
  implicit val decoder: JsonDecoder[MayRemoveItems] = JsonDecoder[Boolean].map(MayRemoveItems(_))
}
case class MaySetSeen(value: Boolean) extends AnyVal
object MaySetSeen {
  implicit val decoder: JsonDecoder[MaySetSeen] = JsonDecoder[Boolean].map(MaySetSeen(_))
}
case class MaySetKeywords(value: Boolean) extends AnyVal
object MaySetKeywords {
  implicit val decoder: JsonDecoder[MaySetKeywords] = JsonDecoder[Boolean].map(MaySetKeywords(_))
}
case class MayCreateChild(value: Boolean) extends AnyVal
object MayCreateChild {
  implicit val decoder: JsonDecoder[MayCreateChild] = JsonDecoder[Boolean].map(MayCreateChild(_))
}
case class MayRename(value: Boolean) extends AnyVal
object MayRename {
  implicit val decoder: JsonDecoder[MayRename] = JsonDecoder[Boolean].map(MayRename(_))
}
case class MayDelete(value: Boolean) extends AnyVal
object MayDelete {
  implicit val decoder: JsonDecoder[MayDelete] = JsonDecoder[Boolean].map(MayDelete(_))
}
case class MaySubmit(value: Boolean) extends AnyVal
object MaySubmit {
  implicit val decoder: JsonDecoder[MaySubmit] = JsonDecoder[Boolean].map(MaySubmit(_))
}

final case class MailboxRights(mayReadItems: MayReadItems,
                         mayAddItems: MayAddItems,
                         mayRemoveItems: MayRemoveItems,
                         maySetSeen: MaySetSeen,
                         maySetKeywords: MaySetKeywords,
                         mayCreateChild: MayCreateChild,
                         mayRename: MayRename,
                         mayDelete: MayDelete,
                         maySubmit: MaySubmit)

object MailboxRights {
  implicit val decoder: JsonDecoder[MailboxRights] = DeriveJsonDecoder.gen[MailboxRights]
}


/*case class MailboxNamespace(value: String) extends AnyVal
object MailboxNamespace {
  implicit val decoder: JsonDecoder[MailboxNamespace] = JsonDecoder[String].map(MailboxNamespace(_))
}*/

case class Username(value: String) extends AnyVal
object Username {
  implicit val decoder: JsonDecoder[Username] = JsonDecoder[String].map(Username(_))
}
case class Right(value: String) extends AnyVal
object Right {
  implicit val decoder: JsonDecoder[Right] = JsonDecoder[String].map(Right(_))
}
case class Rights(rights: Map[Username, List[Right]])
object Rights {
  implicit val mapDecoder: JsonDecoder[Map[Username, List[Right]]] = JsonDecoder[Map[String, List[Right]]]
    .map(map => map.map{case (k,v) =>(Username(k),v) })
  implicit val decoder: JsonDecoder[Rights] = DeriveJsonDecoder.gen[Rights]

}

case class Mailbox(id: MailboxId,
                   name: MailboxName,
                   parentId: Option[MailboxId],
                   role: Option[Role],
                   sortOrder: SortOrder,
                   totalEmails: TotalEmails,
                   unreadEmails: UnreadEmails,
                   totalThreads: TotalThreads,
                   unreadThreads: UnreadThreads,
                   myRights: MailboxRights,
                   isSubscribed: IsSubscribed,
//                   namespace: MailboxNamespace,
//                   rights: Rights,
//                   quotas: Quotas
                  )
case class UnparsedMailboxId(value: String) extends AnyVal
object UnparsedMailboxId {
  implicit val decoder: JsonDecoder[UnparsedMailboxId] = JsonDecoder[String].map(UnparsedMailboxId(_))
}

object Mailbox {
  implicit val decoder: JsonDecoder[Mailbox] = DeriveJsonDecoder.gen[Mailbox]
}

case class MailboxGetResponse(accountId: AccountId,
                              state: State,
                              list: List[Mailbox],
                              notFound: List[UnparsedMailboxId])

object MailboxGetResponse {
  implicit val decoder: JsonDecoder[MailboxGetResponse] = DeriveJsonDecoder.gen[MailboxGetResponse]
}

case class SessionState(value: String)
object SessionState {
  implicit val decoder: JsonDecoder[SessionState] = JsonDecoder[String].map(SessionState(_))
}

case class MethodName(value: String)
object MethodName {
  implicit val decoder: JsonDecoder[MethodName] = JsonDecoder[String].map(MethodName(_))
  val mailboxGet = MethodName("Mailbox/get")
  val mailboxQuery = MethodName("Mailbox/query")
  val error = MethodName("error")
}

case class ClientId(value: String)
object ClientId {
  implicit val decoder: JsonDecoder[ClientId] = JsonDecoder[String].map(ClientId(_))
}

