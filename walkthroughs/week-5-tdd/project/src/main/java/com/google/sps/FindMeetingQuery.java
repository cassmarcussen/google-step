// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    //throw new UnsupportedOperationException("TODO: Implement this method.");

    Collection<TimeRange> meetingTimes= new HashSet<>();

    //convert to int, since MeetingRequest has long duration, but TimeRange has int duration
    int durationOfMeeting = (int)request.getDuration();
    int startOfDay = TimeRange.START_OF_DAY;
    int endOfDay = TimeRange.END_OF_DAY;

    // Increment every 15 minutes, and add all potential meeting times with duration to meetingTimes.
    // My algorithm will work by deleting entires out of meetingTimes.
    for (int startOfMeeting = startOfDay; startOfMeeting < endOfDay; startOfMeeting += 15) {
        TimeRange meeting =  TimeRange.fromStartDuration(startOfMeeting, durationOfMeeting);
        meetingTimes.add(meeting);
    }

    for (Event event : events) {

        
    }

    return meetingTimes;

  }
}

