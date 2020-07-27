/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.backend.sdk.ws.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dpppt.backend.sdk.data.DPPPTDataService;
import org.dpppt.backend.sdk.model.BucketList;
import org.dpppt.backend.sdk.model.ExposedOverview;
import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.model.ExposeeRequest;
import org.dpppt.backend.sdk.model.ExposeeRequestList;
import org.dpppt.backend.sdk.model.proto.Exposed;
import org.dpppt.backend.sdk.ws.security.ValidateRequest;
import org.dpppt.backend.sdk.ws.security.ValidateRequest.InvalidDateException;
import org.dpppt.backend.sdk.ws.util.ValidationUtils;
import org.dpppt.backend.sdk.ws.util.ValidationUtils.BadBatchReleaseTimeException;
import org.dpppt.backend.sdk.ws.gateway.Gateway;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import com.google.protobuf.ByteString;

@Controller
@RequestMapping("/v1")
public class DPPPTController {

	private final DPPPTDataService dataService;
	private final String appSource;
	private final int exposedListCacheControl;
	private final ValidateRequest validateRequest;
	private final ValidationUtils validationUtils;
	private final long batchLength;
	private final long requestTime;
	public Gateway federationGateway;


	public DPPPTController(DPPPTDataService dataService, String appSource,
			int exposedListCacheControl, ValidateRequest validateRequest, ValidationUtils validationUtils, long batchLength,
			long requestTime) {
		this.dataService = dataService;
		this.appSource = appSource;
		this.exposedListCacheControl = exposedListCacheControl/1000/60;
		this.validateRequest = validateRequest;
		this.validationUtils = validationUtils;
		this.batchLength = batchLength;
		this.requestTime = requestTime;
		this.federationGateway = new Gateway(dataService, appSource);
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<String> hello() {
		return ResponseEntity.ok().header("X-HELLO", "dp3t").body("<h1>Hello from DP3T WS</h1>");
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@PostMapping(value = "/exposed")
	public @ResponseBody ResponseEntity<String> addExposee(@Valid @RequestBody ExposeeRequest exposeeRequest,
			@RequestHeader(value = "User-Agent", required = true) String userAgent,
			@AuthenticationPrincipal Object principal) throws InvalidDateException {
		long now = System.currentTimeMillis();
		if (!this.validateRequest.isValid(exposeeRequest)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		if (!validationUtils.isValidBase64Key(exposeeRequest.getKey())) {
			return new ResponseEntity<>("No valid base64 key", HttpStatus.BAD_REQUEST);
		}
		// TODO: should we give that information?
		Exposee exposee = new Exposee();
		exposee.setKey(exposeeRequest.getKey());
		long keyDate = this.validateRequest.getKeyDate(principal, exposeeRequest);

		exposee.setKeyDate(keyDate);
		exposee.setCountryCodeList(exposeeRequest.getCountryCodeList());

		if (!this.validateRequest.isFakeRequest(principal, exposeeRequest)) {
			dataService.upsertExposee(exposee, appSource);
		}

		long after = System.currentTimeMillis();
		long duration = after - now;
		try {
			Thread.sleep(Math.max(this.requestTime - duration, 0));
		} catch (Exception ex) {

		}
		return ResponseEntity.ok().build();
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@PostMapping(value = "/exposedlist")
	public @ResponseBody ResponseEntity<String> addExposee(@Valid @RequestBody ExposeeRequestList exposeeRequests,
			@RequestHeader(value = "User-Agent", required = true) String userAgent,
			@AuthenticationPrincipal Object principal) throws InvalidDateException {
		long now = System.currentTimeMillis();
		if (!this.validateRequest.isValid(exposeeRequests)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<Exposee> exposees = new ArrayList<>();
		for (var exposedKey : exposeeRequests.getExposedKeys()) {
			if (!validationUtils.isValidBase64Key(exposedKey.getKey())) {
				return new ResponseEntity<>("No valid base64 key", HttpStatus.BAD_REQUEST);
			}

			Exposee exposee = new Exposee();
			exposee.setKey(exposedKey.getKey());
			long keyDate = this.validateRequest.getKeyDate(principal, exposedKey);

			exposee.setKeyDate(keyDate);
			exposees.add(exposee);
		}

		if (!this.validateRequest.isFakeRequest(principal, exposeeRequests)) {
			dataService.upsertExposees(exposees, appSource);
		}

		long after = System.currentTimeMillis();
		long duration = after - now;
		try {
			Thread.sleep(Math.max(this.requestTime - duration, 0));
		} catch (Exception ex) {

		}
		return ResponseEntity.ok().build();
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/exposedjson/{batchReleaseTime}", produces = "application/json")
	public @ResponseBody ResponseEntity<ExposedOverview> getExposedByDayDate(@PathVariable long batchReleaseTime,
			WebRequest request) throws BadBatchReleaseTimeException{
		if(!validationUtils.isValidBatchReleaseTime(batchReleaseTime)) {
			return ResponseEntity.notFound().build();
		}

		List<Exposee> exposeeList = dataService.getSortedExposedForBatchReleaseTime(batchReleaseTime, batchLength);
		ExposedOverview overview = new ExposedOverview(exposeeList);
		overview.setBatchReleaseTime(batchReleaseTime);
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(exposedListCacheControl)))
				.header("X-BATCH-RELEASE-TIME", Long.toString(batchReleaseTime)).body(overview);
	}

	//Helper function to remove duplicate keys from returns
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/exposed/{batchReleaseTime}/{coi}", produces = "application/x-protobuf")
	public @ResponseBody ResponseEntity<Exposed.ProtoExposedList> getExposedByBatch(@PathVariable long batchReleaseTime,
	       @PathVariable String coi, WebRequest request) throws BadBatchReleaseTimeException {
		if(!validationUtils.isValidBatchReleaseTime(batchReleaseTime)) {
			return ResponseEntity.notFound().build();
		}

		String[] coiArray = coi.split(", ");
		List<Exposee> exposeeListAll = dataService.getLocalExposedForBatchReleaseTime(batchReleaseTime, batchLength);
		for(String country : coiArray) {
			exposeeListAll.addAll(dataService.getExposedForBatchReleaseTimeAndCountry(batchReleaseTime, batchLength, country));
		}

		List<Exposee> exposeeList = exposeeListAll.stream().filter(distinctByKey(Exposee::getKey)).collect(Collectors.toList());

		List<Exposed.ProtoExposee> exposees = new ArrayList<>();
		for (Exposee exposee : exposeeList) {
			Exposed.ProtoExposee protoExposee = Exposed.ProtoExposee.newBuilder()
					.setKey(ByteString.copyFrom(Base64.getDecoder().decode(exposee.getKey())))
					.setKeyDate(exposee.getKeyDate()).build();
			exposees.add(protoExposee);
		}
		Exposed.ProtoExposedList protoExposee = Exposed.ProtoExposedList.newBuilder().addAllExposed(exposees)
				.setBatchReleaseTime(batchReleaseTime).build();

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(exposedListCacheControl)))
				.header("X-BATCH-RELEASE-TIME", Long.toString(batchReleaseTime)).body(protoExposee);
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/buckets/{dayDateStr}", produces = "application/json")
	public @ResponseBody ResponseEntity<BucketList> getListOfBuckets(@PathVariable String dayDateStr) {
		OffsetDateTime day = LocalDate.parse(dayDateStr).atStartOfDay().atOffset(ZoneOffset.UTC);
		OffsetDateTime currentBucket = day;
		OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
		List<Long> bucketList = new ArrayList<>();
		while (currentBucket.toInstant().toEpochMilli() < Math.min(day.plusDays(1).toInstant().toEpochMilli(),
				now.toInstant().toEpochMilli())) {
			bucketList.add(currentBucket.toInstant().toEpochMilli());
			currentBucket = currentBucket.plusSeconds(batchLength / 1000);
		}
		BucketList list = new BucketList();
		list.setBuckets(bucketList);
		return ResponseEntity.ok(list);
	}

	@ExceptionHandler({IllegalArgumentException.class, InvalidDateException.class, JsonProcessingException.class,
			MethodArgumentNotValidException.class, BadBatchReleaseTimeException.class, DateTimeParseException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> invalidArguments() {
		return ResponseEntity.badRequest().build();
	}

	@CrossOrigin(origins = { "https://editor.swagger.io" })
	@GetMapping(value = "/notify")
	public @ResponseBody ResponseEntity<String> getCallback(
			@RequestParam(value = "batchTag") String batchTag,
			@RequestParam(value = "date") String date)
	{
		try {
			federationGateway.addToBeDownloaded(date,batchTag);
			federationGateway.downloadNewKeys(date);
			return ResponseEntity.ok().build();
		} catch(Exception e) {
			System.out.println(e);
			return ResponseEntity.status(500).build();
		}
	}

}
