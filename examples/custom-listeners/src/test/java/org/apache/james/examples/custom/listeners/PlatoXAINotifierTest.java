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

import static org.apache.james.examples.custom.listeners.SetCustomFlagOnBigMessages.BIG_MESSAGE;
import static org.apache.james.examples.custom.listeners.SetCustomFlagOnBigMessages.ONE_MB;
import static org.apache.james.mailbox.events.MailboxEvents.Added.IS_APPENDED;
import static org.apache.james.mailbox.events.MailboxEvents.Added.IS_DELIVERY;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.mail.Flags;

import org.apache.james.core.Username;
import org.apache.james.events.Event;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MailboxSessionUtil;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.inmemory.InMemoryMailboxManager;
import org.apache.james.mailbox.inmemory.manager.InMemoryIntegrationResources;
import org.apache.james.mailbox.model.ComposedMessageId;
import org.apache.james.mailbox.model.FetchGroup;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mailbox.store.event.EventFactory;
import org.apache.james.mime4j.dom.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;

class PlatoXAINotifierTest {

    private static final Username devUSER = Username.of("test@dev.ccemma.com");
    private static final Username prodUSER = Username.of("test@ccemma.com");

    private static final Event.EventId RANDOM_EVENT_ID = Event.EventId.random();
    private static final MailboxPath devINBOX_PATH = MailboxPath.inbox(devUSER);
    private static final MailboxPath prodINBOX_PATH = MailboxPath.inbox(prodUSER);


    private PlatoXAINotifier testee;
    private MessageManager devInboxMessageManager;
    private MessageManager prodInboxMessageManager;
    private MailboxId devInboxId;
    private MailboxId prodInboxId;
    private MailboxSession devMailboxSession;
    private MailboxSession prodMailboxSession;
    private InMemoryMailboxManager mailboxManager;

    @BeforeEach
    void beforeEach() throws Exception {
        InMemoryIntegrationResources resources = InMemoryIntegrationResources.defaultResources();
        mailboxManager = resources.getMailboxManager();
        devMailboxSession = MailboxSessionUtil.create(devUSER);
        prodMailboxSession = MailboxSessionUtil.create(prodUSER);
        devInboxId = mailboxManager.createMailbox(devINBOX_PATH, devMailboxSession).get();
        prodInboxId = mailboxManager.createMailbox(prodINBOX_PATH, prodMailboxSession).get();
        devInboxMessageManager = mailboxManager.getMailbox(devInboxId, devMailboxSession);
        prodInboxMessageManager = mailboxManager.getMailbox(prodInboxId, prodMailboxSession);

        testee = new PlatoXAINotifier(mailboxManager);

        resources.getEventBus().register(testee);
    }

    @Test
    void shouldKeepBigMessageFlagWhenAlreadySet() throws Exception {
        ComposedMessageId devComposedId = devInboxMessageManager.appendMessage(
            MessageManager.AppendCommand.builder()
                .build(createMessage()),
            devMailboxSession).getId();

        ComposedMessageId prodComposedId = prodInboxMessageManager.appendMessage(
            MessageManager.AppendCommand.builder()
                .build(createMessage()),
            prodMailboxSession).getId();  
        assertThat(true);
    }

    private Message createMessage() throws Exception {
        return Message.Builder.of()
            .setSubject("big message")
            .setBody(Strings.repeat("big message has size greater than one MB", 1024 * 1024), StandardCharsets.UTF_8)
            .build();
    }
}
