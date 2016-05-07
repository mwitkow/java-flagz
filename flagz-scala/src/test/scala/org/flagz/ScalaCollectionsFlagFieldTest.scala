package org.flagz

import org.flagz.testclasses.TestCollectionObject
import org.scalatest.WordSpec

class ScalaCollectionsFlagFieldTest extends WordSpec {

  "ListFlagField" must {

    "preserve defaults" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]())
      assert(TestCollectionObject.flagList.defaultValue() == TestCollectionObject.flagList.get())
    }

    "parse objects annotated with FlagContainer" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_list=0,1,2,3,4"))
      assert(List(0, 1, 2, 3, 4) == TestCollectionObject.flagList.get())
    }

    "unwrap to the original String implementation" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_list=0,1,2,3,4"))
      val flagField = TestCollectionObject.flagList.asInstanceOf[FlagField[List[Int]]]
      assert("0,1,2,3,4" == flagField.valueString(flagField.get()))
    }
  }

  "SetFlagField" must {

    "preserve defaults" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]())
      assert(TestCollectionObject.flagSet.defaultValue() == TestCollectionObject.flagSet.get())
    }

    "parse objects annotated with FlagContainer" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_set=foo,boo,zoo"))
      assert(Set("foo", "boo", "zoo") == TestCollectionObject.flagSet.get())
    }

    "unwrap to the original String implementation" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_set=foo,boo,zoo"))
      val flagField = TestCollectionObject.flagSet.asInstanceOf[FlagField[Set[Int]]]
      assert("foo,boo,zoo" == flagField.valueString(flagField.get()))
    }
  }

  "MapFlagField" must {

    "preserve defaults" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]())
      assert(TestCollectionObject.flagMap.defaultValue() == TestCollectionObject.flagMap.get())
    }

    "parse objects annotated with FlagContainer" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_map=foo:2,boo:3"))
      assert(Map("foo" -> 2, "boo" -> 3) == TestCollectionObject.flagMap.get())
    }

    "unwrap to the original String implementation" in {
      val flagRegistry = ScalaFlagz.parse(Array[String]("--test_coll_map=foo:2,boo:3"))
      val flagField = TestCollectionObject.flagMap.asInstanceOf[FlagField[Map[String, Int]]]
      assert("foo:2,boo:3" == flagField.valueString(flagField.get()))
    }
  }

}
