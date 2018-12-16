package semilattice.util.stream

import java.io.InputStream
import java.io.OutputStream
import scalaz.std.AllInstances.intInstance
import scalaz.syntax.equal.ToEqualOps
import scalaz.zio.IO
import scalaz.zio.stream.Chunk
import scalaz.zio.stream.Sink
import scalaz.zio.stream.Stream

object io {
    def inputStream(s: InputStream): Stream[Throwable, Chunk[Byte]] =
        Stream.unfoldM(())(_ => read(s).map(_.map(x => (x, ()))))

    def outputStream(s: OutputStream): Sink[Throwable, Nothing, Chunk[Byte], Unit] =
        Sink.foldM(IO.point(()))((_, c) => write(s, c).map(_ => Sink.Step.more(())))

    def read(s: InputStream): IO[Throwable, Option[Chunk[Byte]]] =
        for {
            b <- IO.sync(new Array[Byte](1024))
            n <- IO.syncThrowable(s.read(b))
            r = if (n ≟ -1) None else Some(Chunk.fromArray(b).take(n))
        } yield r

    def write(s: OutputStream, c: Chunk[Byte]): IO[Throwable, Unit] =
        IO.syncThrowable(s.write(c.toArray))
}
