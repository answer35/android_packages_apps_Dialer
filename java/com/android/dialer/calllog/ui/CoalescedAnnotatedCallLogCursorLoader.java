/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dialer.calllog.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import com.android.dialer.CoalescedIds;
import com.android.dialer.DialerPhoneNumber;
import com.android.dialer.NumberAttributes;
import com.android.dialer.calllog.database.contract.AnnotatedCallLogContract.CoalescedAnnotatedCallLog;
import com.android.dialer.calllog.model.CoalescedRow;
import com.google.protobuf.InvalidProtocolBufferException;

/** CursorLoader for the coalesced annotated call log. */
final class CoalescedAnnotatedCallLogCursorLoader extends CursorLoader {

  // Indexes for CoalescedAnnotatedCallLog.ALL_COLUMNS
  private static final int ID = 0;
  private static final int TIMESTAMP = 1;
  private static final int NUMBER = 2;
  private static final int FORMATTED_NUMBER = 3;
  private static final int NUMBER_PRESENTATION = 4;
  private static final int IS_READ = 5;
  private static final int NEW = 6;
  private static final int GEOCODED_LOCATION = 7;
  private static final int PHONE_ACCOUNT_COMPONENT_NAME = 8;
  private static final int PHONE_ACCOUNT_ID = 9;
  private static final int FEATURES = 10;
  private static final int NUMBER_ATTRIBUTES = 11;
  private static final int IS_VOICEMAIL_CALL = 12;
  private static final int VOICEMAIL_CALL_TAG = 13;
  private static final int CALL_TYPE = 14;
  private static final int COALESCED_IDS = 15;

  CoalescedAnnotatedCallLogCursorLoader(Context context) {
    // CoalescedAnnotatedCallLog requires that PROJECTION be ALL_COLUMNS and the following params be
    // null.
    super(
        context,
        CoalescedAnnotatedCallLog.CONTENT_URI,
        CoalescedAnnotatedCallLog.ALL_COLUMNS,
        null,
        null,
        null);
  }

  /** Creates a new {@link CoalescedRow} from the provided cursor using the current position. */
  static CoalescedRow toRow(Cursor cursor) {
    DialerPhoneNumber number;
    try {
      number = DialerPhoneNumber.parseFrom(cursor.getBlob(NUMBER));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Couldn't parse DialerPhoneNumber bytes");
    }

    CoalescedIds coalescedIds;
    try {
      coalescedIds = CoalescedIds.parseFrom(cursor.getBlob(COALESCED_IDS));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Couldn't parse CoalescedIds bytes");
    }

    NumberAttributes numberAttributes;
    try {
      numberAttributes = NumberAttributes.parseFrom(cursor.getBlob(NUMBER_ATTRIBUTES));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Couldn't parse NumberAttributes bytes");
    }

    CoalescedRow.Builder coalescedRowBuilder =
        CoalescedRow.newBuilder()
            .setId(cursor.getLong(ID))
            .setTimestamp(cursor.getLong(TIMESTAMP))
            .setNumber(number)
            .setNumberPresentation(cursor.getInt(NUMBER_PRESENTATION))
            .setIsRead(cursor.getInt(IS_READ) == 1)
            .setIsNew(cursor.getInt(NEW) == 1)
            .setFeatures(cursor.getInt(FEATURES))
            .setCallType(cursor.getInt(CALL_TYPE))
            .setNumberAttributes(numberAttributes)
            .setIsVoicemailCall(cursor.getInt(IS_VOICEMAIL_CALL) == 1)
            .setCoalescedIds(coalescedIds);

    String formattedNumber = cursor.getString(FORMATTED_NUMBER);
    if (!TextUtils.isEmpty(formattedNumber)) {
      coalescedRowBuilder.setFormattedNumber(formattedNumber);
    }

    String geocodedLocation = cursor.getString(GEOCODED_LOCATION);
    if (!TextUtils.isEmpty(geocodedLocation)) {
      coalescedRowBuilder.setGeocodedLocation(geocodedLocation);
    }

    String phoneAccountComponentName = cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME);
    if (!TextUtils.isEmpty(phoneAccountComponentName)) {
      coalescedRowBuilder.setPhoneAccountComponentName(
          cursor.getString(PHONE_ACCOUNT_COMPONENT_NAME));
    }

    String phoneAccountId = cursor.getString(PHONE_ACCOUNT_ID);
    if (!TextUtils.isEmpty(phoneAccountId)) {
      coalescedRowBuilder.setPhoneAccountId(phoneAccountId);
    }

    String voicemailCallTag = cursor.getString(VOICEMAIL_CALL_TAG);
    if (!TextUtils.isEmpty(voicemailCallTag)) {
      coalescedRowBuilder.setVoicemailCallTag(voicemailCallTag);
    }

    return coalescedRowBuilder.build();
  }

  static long getTimestamp(Cursor cursor) {
    return cursor.getLong(TIMESTAMP);
  }
}
