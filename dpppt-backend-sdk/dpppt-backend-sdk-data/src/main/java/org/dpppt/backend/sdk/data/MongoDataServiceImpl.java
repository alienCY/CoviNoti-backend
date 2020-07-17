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

import javax.sql.DataSource;

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

    @Autowired
    private ExposeeRepository exposeeRepository;

    private ExposeeDocMapper mapper;

    public MongoDataServiceImpl() {
        this.mapper = new ExposeeDocMapper();
    }

    @Override
    @Transactional(readOnly = false)
    public void upsertExposee(Exposee exposee, String appSource) {
        exposeeRepository.save(mapper.toDoc(exposee, appSource));
    }

    @Override
    @Transactional(readOnly = false)
    public void upsertExposees(List<Exposee> exposees, String appSource) {
        List<ExposeeDoc> exposeeDocs = new ArrayList<>();
        for(var exposee : exposees) {
            exposeeDocs.add(mapper.toDoc(exposee, appSource));
        }
        exposeeRepository.saveAll(exposeeDocs);
    }

    //UNUSED
    @Override
    @Transactional(readOnly = true)
    public int getMaxExposedIdForBatchReleaseTime(long batchReleaseTime, long batchLength) {
       return 0;
    }

    //DEPRECATED
    @Override
    @Transactional(readOnly = true)
    public List<Exposee> getSortedExposedForBatchReleaseTime(long batchReleaseTime, long batchLength) {
        Query query = new Query();
        query.addCriteria(
          Criteria.where("received_at").lt(Date.from(Instant.ofEpochMilli(batchReleaseTime)))
                .andOperator(Criteria.where("received_at").gte(Date.from(Instant.ofEpochMilli(batchReleaseTime - batchLength))))
        );
        return mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exposee> getSortedExposedForBatchReleaseTimeAndCountry(long batchReleaseTime, long batchLength, String country, boolean countryOfOrigin) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("received_at").lt(Date.from(Instant.ofEpochMilli(batchReleaseTime)))
                        .andOperator(Criteria.where("received_at").gte(Date.from(Instant.ofEpochMilli(batchReleaseTime - batchLength))))
        );
        if(countryOfOrigin == false) { //get all entries for country
            query.addCriteria(Criteria.where("countryCodeList").is(country));
        } else { //get entries with first element the country (origin)
            query.addCriteria(Criteria.where("countryCodeList.0").is(country));
        }
        return mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class));
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
