package semilattice.uphubd.monitor

import scala.concurrent.ExecutionContext
import scalaz.zio.IO
import scalaz.zio.duration.Duration
import scalaz.zio.interop.future.IOObjOps
import semilattice.util.Logger

final case class Perform(
    logger: Logger[IO[Nothing, ?], Perform.Message],
    ec: ExecutionContext,
) {
    /**
     * Perform a monitor. See also [[performMechanism]].
     */
    def perform(m: Monitor): IO[Nothing, Nothing] =
        // TODO: How to properly deal with infinite schedules?
        performMechanism(m.mechanism)
            .repeat(m.schedule.functionalize)
            .flatMap(_ => IO.sync(???))

    /**
     * Perform a mechanism. What effects this has for each mechanism is
     * documented under the subclasses of [[Mechanism]].
     */
    def performMechanism(m: Mechanism): IO[Nothing, Status] =
        m match {
            case Mechanism.Ping(host, timeout) => performPing(host, timeout)
            case Mechanism.Http(url, timeout) => performHttp(url, timeout)
        }

    private
    def performPing(host: String, timeout: Duration): IO[Nothing, Status] =
        ???

    private
    def performHttp(url: String, timeout: Duration): IO[Nothing, Status] =
        fixUp(timeout) {
            for {
                _ <- logger << Perform.Message.Http(url)
                http <- IO.syncException(dispatch.Http.default)
                req <- IO.syncException(dispatch.url(url).HEAD)
                res <- IO.fromFuture(() => http(req)(ec))(ec)
                resCode <- IO.syncException(res.getStatusCode())
            } yield Status.iff(200 <= resCode && resCode < 300)
        }

    private
    def fixUp[E](timeout: Duration)(action: IO[E, Status]): IO[Nothing, Status] =
        Status.catchAll(Status.timeout(timeout)(action))
}

object Perform {
    sealed abstract class Message

    object Message {
        final case class Http(url: String) extends Message
    }
}
