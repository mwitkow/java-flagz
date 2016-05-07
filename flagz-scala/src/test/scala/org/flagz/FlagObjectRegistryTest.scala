package org.flagz

import org.flagz.testclasses.{TestObjectOne, TestObjectTwo}
import org.scalatest.WordSpec

class FlagObjectRegistryTest extends WordSpec {

  "FlagObject registry" must {

    "parse objects annotated with FlagContainer" in {
      val args = Array[String]("--test_one_int=4", "--test_two_int=5")
      val flagRegistry = ScalaFlagz.parse(args)

      assert(TestObjectOne.flagInt.get() == 4)
      assert(TestObjectTwo.flagInt.get() == 5)
    }
  }

}
