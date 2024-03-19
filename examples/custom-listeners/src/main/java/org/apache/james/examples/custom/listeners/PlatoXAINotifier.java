/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.examples.custom.listeners;

import javax.inject.Inject;

import org.apache.james.events.Event;
import org.apache.james.events.EventListener;
import org.apache.james.events.Group;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.events.MailboxEvents.Added;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;
import java.io.*;


/**
 * A Listener to send notification to PlatoX-AI server for added messages.
 *
 *
 */
class PlatoXAINotifier implements EventListener.GroupEventListener {
    public static class PositionPlatoXAINotifierGroup extends Group {

    }

    private static final PositionPlatoXAINotifierGroup GROUP = new PositionPlatoXAINotifierGroup();
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatoXAINotifier.class);

    private final MailboxManager mailboxManager;

    @Inject
    PlatoXAINotifier(MailboxManager mailboxManager) {
        this.mailboxManager = mailboxManager;
    }

    @Override
    public void event(Event event) {
        if (event instanceof Added) {
            Added addedEvent = (Added) event;
            addedEvent.getUids().stream()
                .forEach(messageUid -> SendNotification(addedEvent, messageUid));
        }
    }

    private void SendNotification(Added addedEvent, MessageUid messageUid) {
        String urlStrDev = "https://curiosity-dev.fly.dev/mail/notification";
        String urlStrProd = "https://curiosity-prod.fly.dev/mail/notification";
        String urlStrCcmonet = "https://ccmonet-prod.fly.dev/mail/notification";

        String emailDomainDev = "dev.ccemma.com";
        String emailDomainProd = "ccemma.com";
        String emailDomainCcmonet = "ccmonet.com";

        URL url = null;
        try {
            MailboxSession session = mailboxManager.createSystemSession(addedEvent.getUsername());

            LOGGER.info("Message Received, with uid {} in mailbox {} of user {}",
            messageUid.asLong(), addedEvent.getMailboxId(), addedEvent.getUsername().asString());

            String username = addedEvent.getUsername().asString();
            if(username.endsWith(emailDomainDev)) {
                url = new URL(urlStrDev);
            } else if(username.endsWith(emailDomainProd)) {
                url = new URL(urlStrProd);
            } else if(username.endsWith(emailDomainCcmonet)) {
                url = new URL(urlStrCcmonet);
            } else {
                LOGGER.error("Unknown email domain for user {}", username);
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            String input = "{\"event\":{\"type\":\"new\", \"mail\":\"" + addedEvent.getUsername().asString() + "\"}}";
            os.write(input.getBytes());
            os.flush();
            os.close();
            
            int status = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer responseBody = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();
            conn.disconnect();

            LOGGER.info("Rquested URL: " + url + " Response: " + status + " requestBody: " + input + " responseBody: " + responseBody.toString());
            System.out.flush();

            mailboxManager.endProcessingRequest(session);
        } catch (Exception e) {
            LOGGER.error("error happens when requesting URL {}, {}", url, e.getMessage());
            System.out.flush();
        }
    }

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }
}
