package semilattice.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.SECONDS
import scalaz.zio.IO
import scalaz.zio.RTS
import scalaz.zio.duration.Duration
import semilattice.util.stream.io

// TODO: Research how executors work and how to configure [[HttpServer]] to use
// TODO: the executor that ZIO uses, if that is desired.

object Server {

    ////////////////////////////////////////////////////////////////////////////
    // High-level

    /**
     * A service turns a request into a response.
     */
    type Service = Request => IO[Throwable, Response]

    /**
     * Bind a new HTTP server to an address and serve the given service.
     *
     * @param rts The current implementation uses a library that necessitates
     *            the use of [[RTS.unsafeRun]].
     * @param backlog If less than one, a default backlog is used.
     */
    def serve(rts: RTS)(address: InetSocketAddress, backlog: Int)(service: Service): IO[Throwable, Nothing] =
        createS.bracket(stopS) { server =>
            for {
                _ <- bindS(server, address, backlog)
                _ <- createContextS(server, "/", handler(rts)(service))
                _ <- startS(server)
                r <- delayForever
            } yield r
        }

    private
    def handler(rts: RTS)(service: Service): HttpHandler =
        new HttpHandler {
            override
            def handle(exc: HttpExchange): Unit =
                rts.unsafeRun(h0ndle(exc))

            private
            def h0ndle(exc: HttpExchange): IO[Throwable, Unit] =
                for {
                    req <- readRequest(exc)
                    res <- service(req)
                    _ <- writeResponse(exc, res)
                } yield ()

            private
            def readRequest(exc: HttpExchange): IO[Throwable, Request] =
                for {
                    rawBody <- getRequestBodyE(exc)
                    body = io.inputStream(rawBody)
                } yield Request(body)

            private
            def writeResponse(exc: HttpExchange, res: Response): IO[Throwable, Unit] =
                for {
                    _ <- sendResponseHeadersE(exc, res.statusCode, 0)
                    rawBody <- getResponseBodyE(exc)
                    body = io.outputStream(rawBody)
                    _ <- res.body(body)
                    _ <- closeE(exc)
                } yield ()
        }

    ////////////////////////////////////////////////////////////////////////////
    // Low-level

    private
    def bindS(server: HttpServer, address: InetSocketAddress, backlog: Int): IO[Throwable, Unit] =
        IO.syncThrowable(server.bind(address, backlog))

    private
    def createS: IO[Throwable, HttpServer] =
        IO.syncThrowable(HttpServer.create())

    private
    def createContextS(server: HttpServer, path: String, handler: HttpHandler): IO[Throwable, Unit] =
        IO.syncThrowable(server.createContext(path, handler))

    private
    def startS(server: HttpServer): IO[Throwable, Unit] =
        IO.syncThrowable(server.start())

    private
    def stopS(server: HttpServer): IO[Nothing, Unit] =
        IO.syncThrowable(server.stop(0))
            .catchAll(_ => IO.point(()))

    private
    def closeE(exchange: HttpExchange): IO[Nothing, Unit] =
        IO.syncThrowable(exchange.close())
            .catchAll(_ => IO.point(()))

    private
    def getRequestBodyE(exchange: HttpExchange): IO[Throwable, InputStream] =
        IO.syncThrowable(exchange.getRequestBody())

    private
    def getResponseBodyE(exchange: HttpExchange): IO[Throwable, OutputStream] =
        IO.syncThrowable(exchange.getResponseBody())

    private
    def sendResponseHeadersE(exchange: HttpExchange, rCode: Int, responseLength: Long): IO[Throwable, Unit] =
        IO.syncThrowable(exchange.sendResponseHeaders(rCode, responseLength))

    ////////////////////////////////////////////////////////////////////////////
    // Miscellaneous

    private
    def delayForever: IO[Nothing, Nothing] =
        IO.point(()).delay(Duration(3600, SECONDS)).forever

}
