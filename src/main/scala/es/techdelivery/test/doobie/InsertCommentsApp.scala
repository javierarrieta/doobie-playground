package es.techdelivery.test.doobie

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import doobie.imports._
import fs2.{NonEmptyChunk, Pure, Stream, Task}

object InsertCommentsApp extends App with StrictLogging {

  private def cleanupDB(xa: Transactor[Task]): Either[Throwable, Int] = {
    import DBOps._

    dropTableIfExists.run.transact(xa).unsafeAttemptRun()
  }

  private def createSchema(xa: Transactor[Task]): Either[Throwable, Int] = {
    import DBOps._

    createTable.run.transact(xa).unsafeAttemptRun()
  }

  private def insertData(stream: Stream[Pure, CommentEntity])(xa: Transactor[Task]): Either[Throwable, Int] = {
    import DBOps._

    val applyInsertSideEffect: NonEmptyChunk[CommentEntity] => ConnectionIO[Int] = insertComment.updateMany


    val chunkedStream = stream.rechunkN(conf.chunkSize).chunks

    val insertSideEffect: Stream[ConnectionIO, Int] = chunkedStream.evalMap(applyInsertSideEffect)

    val flow: Stream[Task, Int] = insertSideEffect.transact(xa)

    val task: Task[TraversableOnce[Int]] = flow.runLog

    task.unsafeAttemptRun().right.map(_.sum)
  }

  import DBOps._
  import net.ceedubs.ficus.Ficus._
  import net.ceedubs.ficus.readers.ArbitraryTypeReader._

  final case class InsertConf(db: DBConf, chunkSize: Int, count: Int)

  val conf = ConfigFactory.load().as[InsertConf]("out")

  val xa = createTransactor[Task](conf.db)
  val handler: Throwable => Unit = { t => logger.error(s"""Exception when inserting comments: ${t.getMessage}""", t) }

  val elements = (1 to conf.count).map(i => CommentEntity(i, s"Comment no $i"))
  val stream = Stream.pure(elements: _*)

  //Drop table if exists, create table and insert data

  val result = for {
    _ <- cleanupDB(xa).right
    _ <- createSchema(xa).right
    rows <- insertData(stream)(xa).right
  } yield rows

  result.fold(handler, rows => println(s"""Successfully inserted $rows comments"""))
}
