package semilattice.uphubd.monitor

import scalaz.Deriving
import scalaz.Order
import scalaz.zio
import scalaz.zio.duration.Duration
import semilattice.util.orphan.durationInstance

/**
 * Schedule at which to perform a mechanism.
 */
final case class Schedule(interval: Duration) {
    /**
     * Turn a defunctionalized schedule into a functionalized schedule.
     */
    def functionalize: zio.Schedule[Any, Unit] =
        zio.Schedule.fixed(interval).void
}

object Schedule extends ScheduleInstances0

trait ScheduleInstances0 {
    implicit val orderSchedule =
        Deriving.gen[Order, Schedule]
}
