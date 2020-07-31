/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.data.gaen;

import java.time.Duration;
import java.util.List;

import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.model.gaen.GaenKey;

public interface GAENDataService {

	/**
	 * Upserts the given list of exposed keys
	 * 
	 * @param keys the list of exposed keys to upsert
	 * @param appSource application source name
	 * @param collectionName name of collection
	 */
	void upsertExposees(List<GaenKey> keys, String appSource, String collectionName);

	/**
	 * Returns all exposees for the given rolling period and country.
	 *
	 * @param keyDate
	 * @param publishedAfter
	 * @param publishedUntil
	 * @param country
	 * @return
	 */
	List<GaenKey> getExposedForKeyDateAndCountry(Long keyDate, Long publishedAfter, Long publishedUntil, String country);

	/**
	 * Returns all exposees from Cyprus.
	 *
	 * @param keyDate
	 * @param publishedAfter
	 * @param publishedUntil
	 * @return
	 */
	List<GaenKey> getLocalExposedForKeyDate(Long keyDate, Long publishedAfter, Long publishedUntil);

	/**
	 * deletes entries older than retentionperiod
	 *
	 * @param retentionPeriod
	 */
	void cleanDB(Duration retentionPeriod);
}
