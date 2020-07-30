/*
 * Copyright (c) 2020 Georgios Christodoulou
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.data;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

@Document
class RedeemUUID {
    private final String uuid;

    private final Date receivedAt;

    RedeemUUID(String uuid, Date receivedAt) {
        this.uuid = uuid;
        this.receivedAt = receivedAt;
    }
}

public class JDBCRedeemDataServiceImpl implements RedeemDataService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Logger logger = LoggerFactory.getLogger(JDBCRedeemDataServiceImpl.class);

    private Clock currentClock = Clock.systemUTC();

    public JDBCRedeemDataServiceImpl() {
    }

    @Override
    @Transactional(readOnly = false)
    public boolean checkAndInsertPublishUUID(String uuid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uuid").is(uuid));
        long count = mongoTemplate.count(query, RedeemUUID.class, "redeem");
        if (count > 0) {
            return false;
        } else {
            // set the received_at to the next day, with no time information
            // it will stay longer in the DB but we mitigate the risk that the JWT
            // can be used twice (c.f. testTokensArentDeletedBeforeExpire).
            long startOfDay = LocalDate.now(currentClock).atStartOfDay(ZoneOffset.UTC).plusDays(1).toInstant().toEpochMilli();
            mongoTemplate.save(new RedeemUUID(uuid, new Date(startOfDay)), "redeem");
            return true;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void cleanDB(Duration retentionPeriod) {
        OffsetDateTime retentionTime = OffsetDateTime.now(currentClock).minus(retentionPeriod);
        logger.info("Cleanup DB entries before: " + retentionTime);
        Query query = new Query();
        query.addCriteria(Criteria.where("receivedAt").lt(retentionTime.toLocalDateTime()));
        mongoTemplate.findAllAndRemove(query, RedeemUUID.class);
    }
}