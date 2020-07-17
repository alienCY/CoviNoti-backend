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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.annotation.Transactional;

public class MongoDataServiceImpl implements DPPPTDataService {

    private static final Logger logger = LoggerFactory.getLogger(MongoDataServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ExposeeRepository exposeeRepository;

    private ExposeeDocMapper mapper;

    public MongoDataServiceImpl(DataSource dataSource) {
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
          Criteria.where("received_at").lt(LocalDateTime.ofInstant(Instant.ofEpochMilli(batchReleaseTime), ZoneOffset.UTC))
                .andOperator(Criteria.where("received_at").gte(LocalDateTime.from(Instant.ofEpochMilli(batchReleaseTime - batchLength))))
        );
        List<Exposee> list = mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class));
        System.out.println("Synced!");
        for(var e : list){
            System.out.println(e.getKey());
        }
        return mapper.unDoc(mongoTemplate.find(query, ExposeeDoc.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exposee> getSortedExposedForBatchReleaseTimeAndCountry(long batchReleaseTime, long batchLength, String country, boolean countryOfOrigin) {
        String sql = new String();
        if(countryOfOrigin == false) { //get all entries for country
            sql = "select pk_exposed_id, key, key_date, countries_of_interest from t_exposed where :country = any(countries_of_interest) and received_at >= :startBatch and received_at < :batchReleaseTime order by pk_exposed_id desc";
        } else { //get entries with first country - country of origin the *country*
            sql = "select pk_exposed_id, key, key_date, countries_of_interest from t_exposed where countries_of_interest[1] = :country and received_at >= :startBatch and received_at < :batchReleaseTime order by pk_exposed_id desc";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("country", country);
        params.addValue("batchReleaseTime", Date.from(Instant.ofEpochMilli(batchReleaseTime)));
        params.addValue("startBatch", Date.from(Instant.ofEpochMilli(batchReleaseTime - batchLength)));
        return jt.query(sql, params, new ExposeeRowMapper());
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
