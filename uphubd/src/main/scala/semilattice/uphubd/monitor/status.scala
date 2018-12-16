package semilattice.uphubd.monitor

import scalaz.Deriving
import scalaz.Order
import scalaz.zio.IO
import scalaz.zio.duration.Duration

/**
 * The result of performing a mechanism.
 */
sealed abstract class Status

object Status {
    case object Up extends Status
    case object Down extends Status

    implicit val orderStatus =
        Deriving.gen[Order, Status]

    /**
     * [[Up]] iff the Boolean is true.
     */
    def iff(a: Boolean): Status =
        if (a) Status.Up else Status.Down

    /**
     * [[Down]] if the action throws.
     */
    def catchAll[E](a: IO[E, Status]): IO[Nothing, Status] =
        a.catchAll(_ => IO.point(Down))

    /**
     * [[Down]] if the action times out.
     */
    def timeout[E](t: Duration)(a: IO[E, Status]): IO[E, Status] =
        a.timeout(t).map(_.getOrElse(Down))
}
