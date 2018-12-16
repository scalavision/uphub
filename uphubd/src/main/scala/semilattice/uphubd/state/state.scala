package semilattice.uphubd.state

import scalaz.==>>
import scalaz.zio.Fiber
import scalaz.zio.IO
import scalaz.zio.stream.Sink
import semilattice.uphubd.monitor.Monitor
import semilattice.uphubd.monitor.Perform

/**
 * The state of the system contains a mapping from monitors to their instances.
 */
final case class State(monitors: Monitor ==>> Monitor.Instance) extends AnyVal {
    /**
     * Apply a command to the state. Be sure to discard the old state.
     */
    def command(p: Perform, c: Command): IO[Nothing, State] =
        c match {
            case Command.StartMonitor(m) => startMonitor(p, m)
            case Command.StopMonitor(m) => stopMonitor(p, m)
        }

    /**
     * Construct a sink that applies commands to this state.
     */
    def commands(p: Perform): Sink[Nothing, Nothing, Command, State] =
        Sink.foldM(IO.point(this)) { (s, c) =>
            s.command(p, c).map(Sink.Step.more)
        }

    private
    def startMonitor(p: Perform, m: Monitor): IO[Nothing, State] =
        // NOTE: Keep in mind the properties documented under
        // NOTE: [[Command.StartMonitor]].
        monitors.lookup(m) match {
            case Some(_) => IO.point(this)
            case None => p.perform(m).fork.map(f => State(monitors + (m, f)))
        }

    private
    def stopMonitor(p: Perform, m: Monitor): IO[Nothing, State] =
        // NOTE: Keep in mind the properties documented under
        // NOTE: [[Command.StopMonitor]].
        monitors.lookup(m) match {
            case Some(f) => f.interrupt.map(_ => State(monitors - m))
            case None => IO.point(this)
        }
}

object State {
    /**
     * The initial state has no running monitors.
     */
    val initial: State =
        State(==>>.empty)
}
