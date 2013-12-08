/*
 * Copyright 2013 Twitter inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.storehaus.kafka

import org.specs2.mutable.Specification
import kafka.serializer.StringDecoder
import com.twitter.util.{Future, Await}
import kafka.consumer.{ConsumerTimeoutException, Whitelist}

/**
 * @author Mansur Ashraf
 * @since 12/7/13
 */
class KafkaStoreSpec extends Specification {

  "Kafka store" should {
    "put a value on a topic" in new KafkaContext {
      Await.result(store(topic).put("testKey", "testValue"))
      try {
        val stream = consumer.createMessageStreamsByFilter[String](new Whitelist(topic), 1, new StringDecoder)(0)
        stream.iterator().next().message === "testValue"
      }
      catch {
        case e: ConsumerTimeoutException => failure("test failed as consumer timed out without getting any msges")
      }
    }

    "put multiple values on a topic" in new KafkaContext {
      private val map = Map(
        "Key_1" -> "value_1",
        "Key_2" -> "value_2",
        "Key_3" -> "value_3"
      )

      private val multiputResponse = store(multiput_topic).multiPut(map)
      Await.result(Future.collect(multiputResponse.values.toList))
      try {
        val stream = consumer.createMessageStreamsByFilter[String](new Whitelist(multiput_topic), 1, new StringDecoder)(0)
        stream.iterator().next().message === "value_1"
        stream.iterator().next().message === "value_2"
        stream.iterator().next().message === "value_3"
      }
      catch {
        case e: ConsumerTimeoutException => failure("test failed as consumer timed out without getting any msges")
      }
    }
  }
}
