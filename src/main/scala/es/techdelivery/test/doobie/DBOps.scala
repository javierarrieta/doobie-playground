package es.techdelivery.test.doobie

import cats.{Eval, Foldable}
import fs2.NonEmptyChunk

object DBOps {
  import doobie.imports._

  def createTransactor[T[_]](dBConf: DBConf)(implicit ev1 : fs2.util.Catchable[T], ev2 : fs2.util.Suspendable[T]) =
    DriverManagerTransactor[T](dBConf.driver, dBConf.url, dBConf.user, dBConf.pass)

  val dropTableIfExists: Update0 = sql"""DROP TABLE IF EXISTS "comments";""".update

  val createTable: Update0 = sql"""CREATE TABLE "comments" ("c_id" int PRIMARY KEY NOT NULL, "c_comment" VARCHAR(50) NOT NULL);""".update

  val insertComment = Update[CommentEntity]("""INSERT INTO "comments" ("c_id", "c_comment") VALUES (?, ?);""")

  val readComments = sql"""SELECT "c_id", "c_comments" FROM "comments" ORDER BY "c_id";"""

  implicit val foldableNEChunk = new Foldable[NonEmptyChunk] {
    override def foldLeft[A, B](fa: NonEmptyChunk[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

    override def foldRight[A, B](fa: NonEmptyChunk[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      fa.foldRight(lb)(f)
  }
}
