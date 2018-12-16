package semilattice.uphubd.monitor

import scalaz.Deriving
import scalaz.Order
import scalaz.zio.Fiber

/**
 * A monitor pairs a mechanism with a schedule. The mechanism is to be
 * performed according to the schedule.
 */
final case class Monitor(mechanism: Mechanism, schedule: Schedule)

object Monitor extends MonitorInstances0 {
    /**
     * Instance of a monitor. It is currently running.
     */
    type Instance = Fiber[Nothing, Nothing]
}

trait MonitorInstances0 {
    implicit val orderMonitor =
        Deriving.gen[Order, Monitor]
}
