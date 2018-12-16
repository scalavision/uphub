package semilattice.uphubd.monitor

import scalaz.Deriving
import scalaz.Order
import scalaz.std.AllInstances.stringInstance
import scalaz.zio.duration.Duration
import semilattice.util.orphan.durationInstance

/**
 * A mechanism describes how to poll a service to perform a health check. There
 * are several mechanisms, defined in the companion object of this class.
 */
sealed abstract class Mechanism

object Mechanism extends MechanismInstances0 {
    /**
     * Send an ICMP echo request to the configured host and await the echo
     * reply. The service is up iff the echo reply arrives within the
     * configured timeout.
     */
    final case class Ping(host: String, timeout: Duration) extends Mechanism

    /**
     * Request the resource at the configured URL using an HTTP HEAD request,
     * await the HTTP response, and verify its status. The service is up iff
     * the HTTP response arrives within the configured timeout and has a status
     * code in the 200-299 range.
     */
    // TODO: Extend this to support other types of requests and responses.
    final case class Http(url: String, timeout: Duration) extends Mechanism
}

trait MechanismInstances0 {
    import Mechanism.Http
    import Mechanism.Ping

    implicit val orderMechanism =
        Deriving.gen[Order, Mechanism]
}
