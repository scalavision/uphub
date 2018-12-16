package semilattice.uphubd

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.ExecutionContext
import scalaz.std.AllInstances.stringInstance
import scalaz.syntax.contravariant.ToContravariantOps
import scalaz.syntax.foldable.ToFoldableOps
import scalaz.syntax.semigroup.ToSemigroupOps
import scalaz.zio.App
import scalaz.zio.IO
import scalaz.zio.RTS
import scalaz.zio.duration.Duration
import scalaz.zio.interop.scalaz72.ioInstances
import scalaz.zio.stream.Stream
import semilattice.http.Response
import semilattice.http.Server
import semilattice.uphubd.monitor.Mechanism
import semilattice.uphubd.monitor.Monitor
import semilattice.uphubd.monitor.Perform
import semilattice.uphubd.monitor.Schedule
import semilattice.uphubd.state.Command
import semilattice.uphubd.state.State
import semilattice.util.Logger

object Main extends App {
    override
    def run(args: List[String]): IO[Nothing, ExitStatus] = {
        val logger = Logger.swallowAll(Logger.stderr ∙ ((s: String) => "LOG " ⊹ s))
        val main = Main(this, logger)
        main.rin
    }
}

final case class Main(rts: RTS, logger: Logger[IO[Nothing, ?], String]) {

    ////////////////////////////////////////////////////////////////////////////
    // Entry point

    def rin: IO[Nothing, Main.ExitStatus] =
        ron .catchAll(ex => logger << ex.toString())
            .map(_ => Main.ExitStatus.ExitNow(1))

    def ron: IO[Throwable, Nothing] =
        for {
            ec <- IO.syncException(ExecutionContext.global)
            perform = Perform(logger ∙ (_.toString), ec)

            url = "http://localhost:8080"
            timeout = Duration(1, SECONDS)
            schedule = Schedule(Duration(2, SECONDS))
            monitor = Monitor(Mechanism.Http(url, timeout), schedule)

            commands = Stream(
                Command.StartMonitor(monitor),
                Command.StartMonitor(monitor),
                Command.StopMonitor(monitor),
                Command.StartMonitor(monitor),
                Command.StopMonitor(monitor),
            )

            s <- commands.run(State.initial.commands(perform))

            _ <- IO.syncThrowable(System.out.println(s))
            r <- Server.serve(rts)(new InetSocketAddress(8080), 128) { request =>
                IO.point(Response(200, request.body.run))
            }
        } yield r
}
