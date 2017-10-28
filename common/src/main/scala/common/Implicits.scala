package common

import fs2.Strategy

import scala.concurrent.ExecutionContext.Implicits.global

object Implicits {
  implicit val strategy: Strategy = Strategy.fromExecutionContext(global)
}
