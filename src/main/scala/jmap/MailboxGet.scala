package jmap

import zio.json._

final case class AccountId(id: String) extends AnyVal
object AccountId {
  implicit val decoder: JsonCodec[AccountId] = JsonCodec[AccountId](JsonEncoder[String].contramap[AccountId](_.id), JsonDecoder[String].map(AccountId(_)))
}
final case class State(id: String) extends AnyVal

final case class MailboxId(id: String) extends AnyVal
final case class MailboxName(id: String) extends AnyVal
final case class Role(id: String) extends AnyVal
final case class SortOrder(id: Int) extends AnyVal
final case class TotalEmails(id: Int) extends AnyVal
final case class UnreadEmails(id: Int) extends AnyVal
final case class TotalThreads(id: Int) extends AnyVal
final case class UnreadThreads(id: Int) extends AnyVal
final case class IsSubscribed(id: Boolean) extends AnyVal


case class MayReadItems(value: Boolean) extends AnyVal
case class MayAddItems(value: Boolean) extends AnyVal
case class MayRemoveItems(value: Boolean) extends AnyVal
case class MaySetSeen(value: Boolean) extends AnyVal
case class MaySetKeywords(value: Boolean) extends AnyVal
case class MayCreateChild(value: Boolean) extends AnyVal
case class MayRename(value: Boolean) extends AnyVal
case class MayDelete(value: Boolean) extends AnyVal
case class MaySubmit(value: Boolean) extends AnyVal

case class MailboxRights(mayReadItems: MayReadItems,
                         mayAddItems: MayAddItems,
                         mayRemoveItems: MayRemoveItems,
                         maySetSeen: MaySetSeen,
                         maySetKeywords: MaySetKeywords,
                         mayCreateChild: MayCreateChild,
                         mayRename: MayRename,
                         mayDelete: MayDelete,
                         maySubmit: MaySubmit)

object MailboxRights {
//  implicit val decoder: JsonCodec[MailboxRights] = DeriveJsonCodec.gen[MailboxRights]
}


case class MailboxNamespace(value: Boolean) extends AnyVal

case class Username(value: String) extends AnyVal
case class Right(value: String) extends AnyVal
case class Rights(rights: Map[Username, Seq[Right]])



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
                   namespace: MailboxNamespace,
                   rights: Rights,
//                   quotas: Quotas
                  )
case class UnparsedMailboxId(value: String) extends AnyVal
case class NotFound(value: Set[UnparsedMailboxId]) {
  def merge(other: NotFound): NotFound = NotFound(this.value ++ other.value)
}

case class MailboxGetResponse(accountId: AccountId,
                              state: State,
                              list: List[Mailbox],
                              notFound: NotFound)