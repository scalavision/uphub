package semilattice.util

import java.io.PrintStream
import scalaz.Applicative
import scalaz.Apply
import scalaz.Contravariant
import scalaz.Monoid
import scalaz.Semigroup
import scalaz.syntax.apply.ToApplyOps
import scalaz.zio.IO

final case class Logger[F[_], A](<< : A => F[Unit]) extends AnyVal

object Logger extends IOLoggers with LoggerInstances1

trait IOLoggers {
    def swallowAll[E, A](l: Logger[IO[E, ?], A]) =
        Logger[IO[Nothing, ?], A](a => l.<<(a).catchAll(_ => IO.point(())))

    def println(w: PrintStream) =
        Logger[IO[Exception, ?], String](a => IO.syncException(w.println(a)))

    def stdout: Logger[IO[Exception, ?], String] =
        println(System.out)

    def stderr: Logger[IO[Exception, ?], String] =
        println(System.err)
}

trait LoggerInstances1 extends LoggerInstances0 {
    implicit def contravariantLogger[F[_]] = new Contravariant[Logger[F, ?]] {
        def contramap[A, B](r: Logger[F, A])(f: B => A) =
            Logger(a => r << f(a))
    }

    implicit def semigroupLogger[F[_]: Apply, A] = new Semigroup[Logger[F, A]] {
        def append(l: Logger[F, A], r: => Logger[F, A]) =
            Logger(a => l.<<(a) *> r.<<(a))
    }
}

trait LoggerInstances0 {
    implicit def monoidLogger[F[_]: Applicative, A] = new Monoid[Logger[F, A]] {
        def zero =
            Logger(_ => Applicative[F].point(()))

        def append(l: Logger[F, A], r: => Logger[F, A]) =
            Semigroup[Logger[F, A]].append(l, r)
    }
}
