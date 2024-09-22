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
package io.vertx.proton.sasl.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.proton.sasl.ProtonSaslMechanism;
import io.vertx.proton.sasl.ProtonSaslMechanismFactory;

public class ProtonSaslMechanismFinderImpl {

  private static Logger LOG = LoggerFactory.getLogger(ProtonSaslMechanismFinderImpl.class);

  /**
   * Attempts to find a matching Mechanism implementation given a list of supported mechanisms from a remote peer. Can
   * return null if no matching Mechanisms are found.
   *
   * @param username
   *          the username, or null if there is none
   * @param password
   *          the password, or null if there is none
   * @param localPrincipal
   *          the socket SSLSession local Principal, or null if there is none
   * @param mechRestrictions
   *          The possible mechanism(s) to which the client should restrict its mechanism selection to if offered by the
   *          server, or null/empty if there is no restriction
   * @param remoteMechanisms
   *          list of mechanism names that are supported by the remote peer.
   *
   * @return the best matching Mechanism for the supported remote set.
   */
  public static ProtonSaslMechanism findMatchingMechanism(String username, String password,
                                                          Principal localPrincipal, Set<String> mechRestrictions, String... remoteMechanisms) {

    ProtonSaslMechanism match = null;
    List<ProtonSaslMechanism> found = new ArrayList<ProtonSaslMechanism>();

    for (String remoteMechanism : remoteMechanisms) {
      ProtonSaslMechanismFactory factory = findMechanismFactory(remoteMechanism);
      if (factory != null) {
        ProtonSaslMechanism mech = factory.createMechanism();
        if (mechRestrictions != null && !mechRestrictions.isEmpty() && !mechRestrictions.contains(remoteMechanism)) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Skipping " + remoteMechanism + " mechanism because it is not in the configured mechanisms restriction set");
          }
        } else if (mech.isApplicable(username, password, localPrincipal)) {
          found.add(mech);
        } else {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Skipping " + mech + " mechanism because the available credentials are not sufficient");
          }
        }
      }
    }

    if (!found.isEmpty()) {
      // Sorts by priority using mechanism comparison and return the last value in
      // list which is the mechanism deemed to be the highest priority match.
      Collections.sort(found);
      match = found.get(found.size() - 1);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Best match for SASL auth was: " + match);
    }

    return match;
  }

  /**
   * Searches for a mechanism factory by using the scheme from the given name.
   *
   * @param name
   *          The name of the authentication mechanism to search for.
   *
   * @return a mechanism factory instance matching the name, or null if none was created.
   */
  protected static ProtonSaslMechanismFactory findMechanismFactory(String name) {
    if (name == null || name.isEmpty()) {
      LOG.warn("No SASL mechanism name was specified");
      return null;
    }

    ProtonSaslMechanismFactory factory = null;

    // TODO: make it pluggable?
    if (ProtonSaslPlainImpl.MECH_NAME.equals(name)) {
      factory = new ProtonSaslPlainFactoryImpl();
    } else if (ProtonSaslAnonymousImpl.MECH_NAME.equals(name)) {
      factory = new ProtonSaslAnonymousFactoryImpl();
    } else if (ProtonSaslExternalImpl.MECH_NAME.equals(name)) {
      factory = new ProtonSaslExternalFactoryImpl();
    }

    return factory;
  }
}
