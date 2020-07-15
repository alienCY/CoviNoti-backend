/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.model;

import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection="exposed")
public class Exposee {

	@Id
	@JsonIgnore
	private Integer Id;

	@NotNull
	private String key;

	@NotNull
	private long keyDate;

	private ArrayList<String> countryCodeList;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@JsonIgnore
	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public long getKeyDate() {
		return keyDate;
	}

	public void setKeyDate(long keyDate) {
		this.keyDate = keyDate;
	}

	public ArrayList<String> getCountryCodeList() {
		return countryCodeList;
	}

	public void setCountryCodeList(ArrayList<String> countryCodeList) {
		this.countryCodeList = countryCodeList;
	}
}
