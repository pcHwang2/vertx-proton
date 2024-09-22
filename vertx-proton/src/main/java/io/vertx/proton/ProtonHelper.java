/*
* Copyright 2016 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.vertx.proton;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface ProtonHelper {

  /**
   * Creates a bare Message object.
   *
   * @return the message
   */
  public static Message message() {
    return Proton.message();
  }

  /**
   * Creates a Message object with the given String contained as an AmqpValue body.
   *
   * @param body
   *          the string to set as an AmqpValue body
   * @return the message
   */
  public static Message message(String body) {
    Message value = message();
    value.setBody(new AmqpValue(body));
    return value;
  }

  /**
   * Creates a Message object with the given String contained as an AmqpValue body, and the 'to' address set as given.
   *
   * @param address
   *          the 'to' address to set
   * @param body
   *          the string to set as an AmqpValue body
   * @return the message
   */
  public static Message message(String address, String body) {
    Message value = message(body);
    value.setAddress(address);
    return value;
  }

  /**
   * Create an ErrorCondition with the given error condition value and error description string.
   *
   * See the AMQP specification
   * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error">error
   * sections</a> for details of various standard error condition values.
   *
   * For useful condition value constants, see {@link org.apache.qpid.proton.amqp.transport.AmqpError AmqpError},
   * {@link org.apache.qpid.proton.amqp.transport.ConnectionError ConnectionError},
   * {@link org.apache.qpid.proton.amqp.transport.SessionError SessionError} and
   * {@link org.apache.qpid.proton.amqp.transport.LinkError LinkError}.
   *
   * @param condition
   *          the error condition value
   * @param description
   *          String description of the error, may be null
   * @return the condition
   */
  public static ErrorCondition condition(Symbol condition, String description) {
    return new ErrorCondition(condition, description);
  }

  /**
   * Create an ErrorCondition with the given error condition value (which will be converted to the required Symbol type)
   * and error description string.
   *
   * See {@link #condition(Symbol, String)} for more details.
   *
   * @param condition
   *          the error condition value, to be converted to a Symbol
   * @param description
   *          String description of the error, may be null
   * @return the condition
   */
  public static ErrorCondition condition(String condition, String description) {
    return condition(Symbol.valueOf(condition), description);
  }

  /**
   * Creates a byte[] for use as a delivery tag by UTF-8 converting the given string.
   *
   * @param tag
   *          the string to convert
   * @return byte[] for use as tag
   */
  public static byte[] tag(String tag) {
    return tag.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Accept the given delivery by applying Accepted disposition state, and optionally settling.
   *
   * @param delivery
   *          the delivery to update
   * @param settle
   *          whether to settle
   * @return the delivery
   */
  public static ProtonDelivery accepted(ProtonDelivery delivery, boolean settle) {
    delivery.disposition(Accepted.getInstance(), settle);
    return delivery;
  }

  /**
   * Reject the given delivery by applying Rejected disposition state, and optionally settling.
   *
   * @param delivery
   *          the delivery to update
   * @param settle
   *          whether to settle
   * @return the delivery
   */
  public static ProtonDelivery rejected(ProtonDelivery delivery, boolean settle) {
    delivery.disposition(new Rejected(), settle);
    return delivery;
  }

  /**
   * Release the given delivery by applying Released disposition state, and optionally settling.
   *
   * @param delivery
   *          the delivery to update
   * @param settle
   *          whether to settle
   * @return the delivery
   */
  public static ProtonDelivery released(ProtonDelivery delivery, boolean settle) {
    delivery.disposition(Released.getInstance(), settle);
    return delivery;
  }

  /**
   * Modify the given delivery by applying Modified disposition state, with deliveryFailed and underliverableHere flags
   * as given, and optionally settling.
   *
   * @param delivery
   *          the delivery to update
   * @param settle
   *          whether to settle
   * @param deliveryFailed
   *          whether the delivery should be treated as failed
   * @param undeliverableHere
   *          whether the delivery is considered undeliverable by the related receiver
   * @return the delivery
   */
  public static ProtonDelivery modified(ProtonDelivery delivery, boolean settle, boolean deliveryFailed,
                                        boolean undeliverableHere) {
    Modified modified = new Modified();
    modified.setDeliveryFailed(deliveryFailed);
    modified.setUndeliverableHere(undeliverableHere);

    delivery.disposition(modified, settle);
    return delivery;
  }

  static <T> AsyncResult<T> future(T value, ErrorCondition err) {
    if (err.getCondition() != null) {
      return Future.failedFuture(err.toString());
    } else {
      return Future.succeededFuture(value);
    }
  }
}
