package semilattice.uphubd.state

import scalaz.Deriving
import scalaz.Order
import semilattice.uphubd.monitor.Monitor

/**
 * A command changes the state of the system.
 */
sealed abstract class Command

object Command extends CommandInstances0 {
    /**
     * Ensure that the specified monitor is running.
     *
     * This command is idempotent.
     */
    final case class StartMonitor(monitor: Monitor) extends Command

    /**
     * Ensure that the specified monitor is not running.
     *
     * This command is idempotent.
     */
    final case class StopMonitor(monitor: Monitor) extends Command
}

trait CommandInstances0 {
    implicit val orderCommand =
        Deriving.gen[Order, Command]
}
