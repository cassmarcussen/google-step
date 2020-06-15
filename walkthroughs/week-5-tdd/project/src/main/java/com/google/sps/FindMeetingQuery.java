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
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  private boolean doEventAndMeetingShareAttendee(Event event, MeetingRequest meeting) {
    
    Collection<String> attendeesOfMeeting = meeting.getAttendees();

    Set<String> attendeesOfEventSet = event.getAttendees();

    // Convert to array because Set does not have a method to get a certain element
    Object[] attendeesOfEvent =  attendeesOfEventSet.toArray();

    // eventAndMeetingShareAttendee is true if the event and the meeting share at least one attendee
    boolean eventAndMeetingShareAttendee = false;
    for (int i = 0; i < attendeesOfEvent.length; i++)  
    { 
        if(attendeesOfMeeting.contains((String)attendeesOfEvent[i])){
            eventAndMeetingShareAttendee = true;
        }
    } 

    return eventAndMeetingShareAttendee;

  }

  private Collection<TimeRange> getViableMeetingDurations(Collection<TimeRange> badMeetingTimes, int durationOfMeeting) {

      Collection<TimeRange> viableMeetingDurations= new ArrayList<>();
      // First, now that all bad meeting times have been added (without duplicates), convert badMeetingTimes to ArrayList and 
      List<TimeRange> badMeetingList = new ArrayList<TimeRange>(badMeetingTimes);
      
      // sort by start time of TimeRange. TimeRange.ORDER_BY_START is a custom pre-built comparator for sorting TimeRange by the start time
      Collections.sort(badMeetingList, TimeRange.ORDER_BY_START);

      if (badMeetingList.size() <= 0) {
          //no bad meeting times
          TimeRange viableRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY + 1, false);
          viableMeetingDurations.add(viableRange);

          return viableMeetingDurations;
      }

      // start time is TimeRange start time
      int startOfViableRange = TimeRange.START_OF_DAY;
      // end time good is first start time of bad time
      int endOfViableRange = badMeetingList.get(0).start();

      int comparingIndex = 0;

      while (comparingIndex < badMeetingList.size()) {

          TimeRange firstBadRange = badMeetingList.get(comparingIndex);
          if (endOfViableRange - startOfViableRange >= durationOfMeeting) {
              TimeRange viableRange = TimeRange.fromStartEnd(startOfViableRange, endOfViableRange, false);
              viableMeetingDurations.add(viableRange);
          }

          comparingIndex++;
          while(comparingIndex < badMeetingList.size() && firstBadRange.overlaps(badMeetingList.get(comparingIndex))) {
              //since noninclusive for end, if overlap at end, break, don't consider overlapped
              /*if(firstBadRange.end() == (badMeetingList.get(comparingIndex)).start()) {
                  break;
              }
              startOfViableRange = badMeetingList.get(comparingIndex).end();*/
              comparingIndex++;
          }
          
           startOfViableRange = badMeetingList.get(comparingIndex).end();

          if(comparingIndex < badMeetingList.size()) {
            endOfViableRange = badMeetingList.get(comparingIndex).start() + 1;
          } else {
            endOfViableRange = TimeRange.END_OF_DAY + 1;
            break;
          }
          
      }

      // Add the last viable range, after we break out of while loop
      if (endOfViableRange - startOfViableRange >= durationOfMeeting) {
          //deal with noOptionsForTooLongOfARequest (doesn't work! fix)
        TimeRange lastViableRange = TimeRange.fromStartEnd(startOfViableRange - 15, endOfViableRange, false);
        viableMeetingDurations.add(lastViableRange);
      }

      // if greater than or equal to duration, add
      // new start time good is end of bad time
      // while overlap with next time interval bad time, incrememnt start time to next end of bad time
      // end time good is next start of bad time
      // if greater than or equal to duration, add

      return viableMeetingDurations;

  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    //throw new UnsupportedOperationException("TODO: Implement this method.");

    // Use Collection so no duplicate elements
    Collection<TimeRange> badMeetingTimes= new HashSet<>();

    // The Collection of viable meeting durations to return
    Collection<TimeRange> viableMeetingDurations;

    //convert to int, since MeetingRequest has long duration, but TimeRange has int duration
    int durationOfMeeting = (int)request.getDuration();
    int startOfDay = TimeRange.START_OF_DAY;
    int endOfDay = TimeRange.END_OF_DAY;

    // Increment every 15 minutes, and add all potential meeting times with duration to meetingTimes.
    // My algorithm will work by deleting entires out of meetingTimes.
    /*for (int startOfMeeting = startOfDay; startOfMeeting < endOfDay; startOfMeeting += 15) {
        TimeRange meeting =  TimeRange.fromStartDuration(startOfMeeting, durationOfMeeting);
        meetingTimes.add(meeting);
    }*/

    for (Event event : events) {

        TimeRange timeRangeOfEvent = event.getWhen();
        int startOfEvent = timeRangeOfEvent.start();
        int durationOfEvent = timeRangeOfEvent.duration();
        int endOfEvent = timeRangeOfEvent.end();

        // eventAndMeetingShareAttendee is true if the event and the meeting share at least one attendee
        boolean eventAndMeetingShareAttendee = doEventAndMeetingShareAttendee(event, request);

        if (eventAndMeetingShareAttendee) {
            // = or +15? b/c maybe can start/end same time different events?
            for (int startTime = startOfEvent; startTime < endOfEvent; startTime += 15){
                TimeRange conflictingMeeting = TimeRange.fromStartEnd(startTime, startTime + durationOfMeeting, false);
                badMeetingTimes.add(conflictingMeeting);
            }
        }

    }

    viableMeetingDurations = getViableMeetingDurations(badMeetingTimes, durationOfMeeting);

    return viableMeetingDurations;

  }
}

