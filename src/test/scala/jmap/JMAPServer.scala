package jmap

import com.dimafeng.testcontainers.GenericContainer
import org.apache.commons.net.imap.IMAPClient
import org.testcontainers.containers.BindMode

sealed trait JMAPServer {
  val name: String
  val container: GenericContainer
  def init()
}

case object Cyrus extends JMAPServer {
  override val name: String = "Cyrus"

  override val container: GenericContainer = createContainer()

  def createContainer(): GenericContainer = {
    val container = GenericContainer("cyrus-jmap").configure(configProvider => {
      configProvider.addExposedPort(143)
      configProvider.addExposedPort(80)
      configProvider.addExposedPort(25)
    })

    container.underlyingUnsafeContainer.withCreateContainerCmdModifier(cmd => cmd.withHostName("jmap.test"))
    container
  }

  override def init(): Unit = {
    val imapClient = new IMAPClient()
    imapClient.connect(container.container.getHost, container.container.getMappedPort(143))
    imapClient.login("bob", "bob")
  }
}

case object James extends JMAPServer {
  override val name: String = "James"

  override val container: GenericContainer = createContainer()

  def createContainer(): GenericContainer = {
    val container = GenericContainer("linagora/james-memory:branch-master").configure(configProvider => {
      configProvider.addExposedPort(143)
      configProvider.addExposedPort(80)
      configProvider.addExposedPort(25)
    })

    container.underlyingUnsafeContainer.withCreateContainerCmdModifier(cmd => cmd.withHostName("jmap.test"))
    container.underlyingUnsafeContainer.withClasspathResourceMapping("jwt_publickey", "/root/conf/jwt_publickey", BindMode.READ_ONLY)
    container
  }

  override def init(): Unit = {
    container.execInContainer("java", "-jar", "james-cli.jar", "addDomain", "jmap.test")
    container.execInContainer("java", "-jar", "james-cli.jar", "addUser", "alice@jmap.test", "p@sSw0ord")
    container.execInContainer("java", "-jar", "james-cli.jar", "addUser", "bob@jmap.test", "p@sSw0ord")
  }
}