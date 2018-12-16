package semilattice.http

import scalaz.zio.IO
import scalaz.zio.stream.Chunk
import scalaz.zio.stream.Sink
import scalaz.zio.stream.Stream

/**
 * HTTP request.
 */
final case class Request(
    body: Stream[Throwable, Chunk[Byte]],
)

/**
 * HTTP response.
 */
final case class Response(
    statusCode: Int,
    body: Sink[Throwable, Nothing, Chunk[Byte], Unit] => IO[Throwable, Unit],
)
