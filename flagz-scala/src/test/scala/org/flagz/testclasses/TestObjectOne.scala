package org.flagz.testclasses

import org.flagz._

object TestObjectOne extends FlagContainer {
  @FlagInfo(name = "test_one_int", help = "One's int")
  final val flagInt = ScalaFlagz.valueOf(300)

}

