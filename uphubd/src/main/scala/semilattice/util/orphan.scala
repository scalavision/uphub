package semilattice.util

import scalaz.Order
import scalaz.Ordering
import scalaz.zio.duration.Duration

object orphan {
    implicit val durationInstance =
        new Order[Duration] {
            def order(l: Duration, r: Duration) =
                Ordering.fromInt(l compareTo r)
        }
}
