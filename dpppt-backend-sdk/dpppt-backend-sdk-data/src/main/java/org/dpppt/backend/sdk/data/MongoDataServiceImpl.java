/*
 * Copyright (c) Georgios Christodoulou
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.data;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.model.ExposeeDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

public class MongoDataServiceImpl implements DPPPTDataService {

    private static final Logger logger = LoggerFactory.getLogger(MongoDataServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private ExposeeDocMapper mapper;

    public MongoDataServiceImpl() {
        this.mapper = new ExposeeDocMapper();
    }

    @Override
    @Transactional(readOnly = false)
    public void upsertExposee(Exposee exposee, String appSource) {
        if(exposee.getCountryCodeList().indexOf("CY") == 0)
            mongoTemplate.save(mapper.toDoc(exposee, appSource), "local");
        else
            mongoTemplate.save(mapper.toDoc(exposee, appSource), "global");
    }

    @Override
    @Transactional(readOnly = false)
    public void upsertExposees(List<Exposee> exposees, String appSource, String collectionName) {
        for(var exposee : exposees) {
            mongoTemplate.save(mapper.toDoc(exposee, appSource), collectionName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exposee> getExposedForBatchReleaseTimeAndCountry(long batchReleaseTime, long batchLength, String country) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("received_at").lt(Date.from(Instant.ofEpochMilli(batchReleaseTime)))
                        .andOperator(Criteria.where("received_at").gte(Date.from(Instant.ofEpochMilli(batchReleaseTime - batchLength))))
        );
        query.addCriteria(
                Criteria.where("countryCodeList").is(country)
        );

        return mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class, "global"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exposee> getLocalExposedForBatchReleaseTime(long batchReleaseTime, long batchLength) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("received_at").lt(Date.from(Instant.ofEpochMilli(batchReleaseTime)))
                        .andOperator(Criteria.where("received_at").gte(Date.from(Instant.ofEpochMilli(batchReleaseTime - batchLength))))
        );
        return mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class, "local"));
    }

    @Override
    @Transactional(readOnly = false)
    public void cleanDB(Duration retentionPeriod) {
        OffsetDateTime retentionTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minus(retentionPeriod);
        logger.info("Cleanup DB entries before: " + retentionTime);
        Query query = new Query();
        query.addCriteria(Criteria.where("received_at").lt(retentionTime.toLocalDateTime()));
        mongoTemplate.findAllAndRemove(query, ExposeeDoc.class);
    }
}
