/*
 * Copyright (c) 2020 Georgios Christodoulou
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.data.gaen;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.dpppt.backend.sdk.model.gaen.GaenKey;
import org.dpppt.backend.sdk.model.gaen.GaenKeyDoc;
import org.dpppt.backend.sdk.model.gaen.GaenUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

public class JDBCGAENDataServiceImpl implements GAENDataService {

	private static final Logger logger = LoggerFactory.getLogger(JDBCGAENDataServiceImpl.class);

	private final Duration bucketLength;

	GaenKeyDocMapper mapper = new GaenKeyDocMapper();

	@Autowired
	private MongoTemplate mongoTemplate;

	public JDBCGAENDataServiceImpl(Duration bucketLength) {
		this.bucketLength = bucketLength;
	}

	@Override
	@Transactional(readOnly = false)
	public void upsertExposees(List<GaenKey> gaenKeys, String appSource, String collectionName) {
		for(var exposee : gaenKeys) {
			mongoTemplate.save(mapper.toDoc(exposee, appSource), collectionName);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<GaenKey> getExposedForKeyDateAndCountry(Long keyDate, Long publishedAfter, Long publishedUntil, String country) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("rollingStartNumber").gte(GaenUnit.TenMinutes.between(Instant.ofEpochMilli(0), Instant.ofEpochMilli(keyDate)))
						.andOperator(Criteria.where("rollingStartNumber").lt(GaenUnit.TenMinutes.between(Instant.ofEpochMilli(0),
								Instant.ofEpochMilli(keyDate).atOffset(ZoneOffset.UTC).plusDays(1).toInstant())))
		);
		query.addCriteria(
				Criteria.where("receivedAt").lt(new Date(publishedUntil))
		);
		if(publishedAfter != null)
			query.addCriteria(
					Criteria.where("receivedAt").gte(new Date(publishedAfter))
			);
		query.addCriteria(
				Criteria.where("countryCodeList").is(country)
		);
		return mapper.unDoc(mongoTemplate.find(query, GaenKeyDoc.class, "global"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<GaenKey> getLocalExposedForKeyDate(Long keyDate, Long publishedAfter, Long publishedUntil) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("rollingStartNumber").gte(GaenUnit.TenMinutes.between(Instant.ofEpochMilli(0), Instant.ofEpochMilli(keyDate)))
						.andOperator(Criteria.where("rollingStartNumber").lt(GaenUnit.TenMinutes.between(Instant.ofEpochMilli(0),
								Instant.ofEpochMilli(keyDate).atOffset(ZoneOffset.UTC).plusDays(1).toInstant())))
		);
		query.addCriteria(
				Criteria.where("receivedAt").lt(new Date(publishedUntil))
		);
		if(publishedAfter != null)
			query.addCriteria(
					Criteria.where("receivedAt").gte(new Date(publishedAfter))
			);
		return mapper.unDoc(mongoTemplate.find(query, GaenKeyDoc.class, "local"));
	}


	@Override
	@Transactional(readOnly = false)
	public void cleanDB(Duration retentionPeriod) {
		OffsetDateTime retentionTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minus(retentionPeriod);
		logger.info("Cleanup DB entries before: " + retentionTime);
		Query query = new Query();
		query.addCriteria(Criteria.where("receivedAt").lt(retentionTime.toLocalDateTime()));
		mongoTemplate.findAllAndRemove(query, GaenKeyDoc.class);
	}

}
