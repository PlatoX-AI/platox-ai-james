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

import jakarta.mail.Flags;

import org.apache.james.events.Event;
import org.apache.james.events.EventListener;
import org.apache.james.events.Group;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.MessageManager.FlagsUpdateMode;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.events.MailboxEvents.Added;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;
import java.io.*;


/**
 * A Listener to determine the size of added messages.
 *
 * If the size is greater or equals than the BIG_MESSAGE size threshold ({@value ONE_MB}).
 * Then it will be considered as a big message and added BIG_MESSAGE {@value BIG_MESSAGE} flag
 *
 */
class PlatoXAINotifier implements EventListener.GroupEventListener {
    public static class PositionCustomFlagOnBigMessagesGroup extends Group {

    }

    private static final PositionCustomFlagOnBigMessagesGroup GROUP = new PositionCustomFlagOnBigMessagesGroup();
    private static final Logger LOGGER = LoggerFactory.getLogger(SetCustomFlagOnBigMessages.class);

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
        String urlStr = "https://api.myip.com";
        try {
            MailboxSession session = mailboxManager.createSystemSession(addedEvent.getUsername());
            MessageManager messageManager = mailboxManager.getMailbox(addedEvent.getMailboxId(), session);

            LOGGER.info("Message Received, with uid {} in mailbox {} of user {}",
            messageUid.asLong(), addedEvent.getMailboxId(), addedEvent.getUsername().asString());

            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int status = con.getResponseCode();
            String body = "";
            switch(status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    body = sb.toString();
                    break;
            }
            LOGGER.info("Rquested URL: " + urlStr + " Response: " + status + " Body: " + body);
            System.out.flush();

            mailboxManager.endProcessingRequest(session);
        } catch (Exception e) {
            LOGGER.error("error happens when requesting URL {}, {}", urlStr, e.getMessage());
            System.out.flush();
        }
    }

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }
}
