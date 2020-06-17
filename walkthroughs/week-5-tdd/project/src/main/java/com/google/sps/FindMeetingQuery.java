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

  /* doEventAndMeetingShareAttendee returns whether the meeting to schedule and a particular event (from the Events Collection) 
  share attendees. If so, later on, the event must be added to the "bad meeting times" list, since a meeting cannot be scheduled 
  during the time range of the event.
   */
  private boolean doEventAndMeetingShareAttendee(Event event, MeetingRequest meeting, boolean onlyConsiderMandatoryAttendees) {
    
    Collection<String> mandatoryAttendeesOfMeeting = meeting.getAttendees();
    Collection<String> optionalAttendeesOfMeeting = meeting.getOptionalAttendees();

    Set<String> attendeesOfEventSet = event.getAttendees();

    // eventAndMeetingShareAttendee is true if the event and the meeting share at least one attendee
    boolean eventAndMeetingShareAttendee = false;
    for (String attendee : attendeesOfEventSet)  
    { 
        if (mandatoryAttendeesOfMeeting.contains(attendee)) {
            eventAndMeetingShareAttendee = true;
        }

        /* Only check the optional attendees collection for comparison if we should include optional attendees, 
        as signified by the boolean parameter onlyConsiderMandatoryAttendees */
        if (!onlyConsiderMandatoryAttendees) {
             if (optionalAttendeesOfMeeting.contains(attendee)) {
                eventAndMeetingShareAttendee = true;
            }
        }

    } 

    return eventAndMeetingShareAttendee;

  }

  private Collection<TimeRange> getViableMeetingDurations(Collection<TimeRange> badMeetingTimes, int durationOfMeeting) {

      Collection<TimeRange> viableMeetingDurations= new ArrayList<>();
      // First, now that all bad meeting times have been added (without duplicates), convert badMeetingTimes to ArrayList 
      List<TimeRange> badMeetingList = new ArrayList<TimeRange>(badMeetingTimes);
      
      // Sort by start time of TimeRange. TimeRange.ORDER_BY_START is a custom pre-built comparator for sorting TimeRange by the start time
      Collections.sort(badMeetingList, TimeRange.ORDER_BY_START);

      if (badMeetingList.size() <= 0) {
          //no bad meeting times
          TimeRange viableRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY + 1, false);
          viableMeetingDurations.add(viableRange);

          return viableMeetingDurations;
      }

      // The initial start time is TimeRange start time of day
      int startOfViableRange = TimeRange.START_OF_DAY;
      // The initial end time good is the start time of the first (earliest) bad meeting
      int endOfViableRange = badMeetingList.get(0).start();

      int comparingIndex = 0;

      // Add a viable meeting duration to the viableMeetingDurations list
      while (comparingIndex < badMeetingList.size()) {

          // Do not include a duration greater than a whole day (error / edge case checking)
          if((endOfViableRange - startOfViableRange) > TimeRange.WHOLE_DAY.duration()) {
              return viableMeetingDurations;
          }

          // If the duration is long enough between the end and start of the viable range, add the duration to viableMeetingDurations
          TimeRange firstBadRange = badMeetingList.get(comparingIndex);
          if ((endOfViableRange - startOfViableRange) >= durationOfMeeting) {
              TimeRange viableRange = TimeRange.fromStartEnd(startOfViableRange, endOfViableRange, false);
              viableMeetingDurations.add(viableRange);
          }

          comparingIndex++;

          int furthestBadEnd =  badMeetingList.get(comparingIndex - 1).end();    

          /* Handle the overlapping events. If events overlap, so long as our comparingIndex is 
          still in the badMeetingList size range, we want to push our 'furthestBadEnd' (the latest end 
          time of the overlapping bad meetings) to later times. This handles the case of nested events as well, 
          since the bad meeting times are sorted by start time from earliest to latest and not end time.
          */
          while(comparingIndex < badMeetingList.size() ) {

               if(!firstBadRange.overlaps(badMeetingList.get(comparingIndex))) {

                   break;
               }

               if ( badMeetingList.get(comparingIndex).end() > furthestBadEnd ) {
                   furthestBadEnd =  badMeetingList.get(comparingIndex).end();
               }

               comparingIndex++;

          }

          /* We set the start of the viable range equal to the furthest bad end time, i.e. we can begin our meeting 
          in the range that starts with the end of the last overlapping event time.
          */
          startOfViableRange = furthestBadEnd;

          if(comparingIndex < badMeetingList.size()) {
            endOfViableRange = badMeetingList.get(comparingIndex).start();
          } else {
            endOfViableRange = TimeRange.END_OF_DAY + 1;
            break;
          }
          
      }

      // Add the last viable range, after we break out of while loop
      if ((endOfViableRange - startOfViableRange) >= durationOfMeeting && (endOfViableRange - startOfViableRange) < TimeRange.WHOLE_DAY.duration()) {
        TimeRange lastViableRange = TimeRange.fromStartEnd(startOfViableRange, endOfViableRange, false);
        viableMeetingDurations.add(lastViableRange);
      }

      return viableMeetingDurations;

  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Use Collection so that no duplicate elements (i.e. bad meeting times) are added. No optional attendees considered.
    Collection<TimeRange> badMeetingTimesNoOptional = new HashSet<>();
    // If include optional attendees
    Collection<TimeRange> badMeetingTimesWithOptional = new HashSet<>();

    // viableMeetingDurations is the Collection of viable meeting durations to return
    // no optional attendees included
    Collection<TimeRange> viableMeetingDurationsNoOptional;   
    // with optional attendees 
    Collection<TimeRange> viableMeetingDurationsWithOptional;


    // Convert to int, since MeetingRequest has long duration, but TimeRange has int duration
    int durationOfMeeting = (int)request.getDuration();

    if(durationOfMeeting > TimeRange.WHOLE_DAY.duration()) {
        // return empty (no options) for too long of a request (handles the noOptionsForTooLongOfARequest() test)
        return new ArrayList<TimeRange>();
    }

    int startOfDay = TimeRange.START_OF_DAY;
    int endOfDay = TimeRange.END_OF_DAY;


    /* For each of the events in the Collection of Events, if the event and the meeting have shared attendees, we 
    need to signify that the meeting cannot be scheduled during the duration of the event. Therefore, we add the 
    time range of this event to badMeetingTimes.
    */
    for (Event event : events) {

        TimeRange timeRangeOfEvent = event.getWhen();

        // eventAndMeetingShareMandatoryAttendee is true if the event and the meeting share at least one mandatory attendee
        boolean eventAndMeetingShareMandatoryAttendee = doEventAndMeetingShareAttendee(event, request, true);

         // eventAndMeetingShareAnyAttendee is true if the event and the meeting share at least one attendee, either mandatory or optional
        boolean eventAndMeetingShareAnyAttendee = doEventAndMeetingShareAttendee(event, request, false);

        if (eventAndMeetingShareMandatoryAttendee) {
            badMeetingTimesNoOptional.add(timeRangeOfEvent);
        }

        if (eventAndMeetingShareAnyAttendee) {
            badMeetingTimesWithOptional.add(timeRangeOfEvent);
        }

    }

    viableMeetingDurationsWithOptional = getViableMeetingDurations(badMeetingTimesWithOptional, durationOfMeeting);
    viableMeetingDurationsNoOptional = getViableMeetingDurations(badMeetingTimesNoOptional, durationOfMeeting);

    if (viableMeetingDurationsWithOptional.size() == 0 && request.getAttendees().size() > 0) {
        /* If no meetings worked when including the optional attendees, and there exist mandatory attendees, 
        return the time slots that fit just the mandatory attendees.
        */
        return viableMeetingDurationsNoOptional;
    } else if (viableMeetingDurationsWithOptional.size() == 0 && request.getAttendees().size() == 0) {
        /* If no meetings worked when including the optional attendees, and there do not exist mandatory attendees, 
        i.e. there only exists optional attendees, then no options will work, and return an empty ArrayList. 
        */
        return new ArrayList<>();
    }

    return viableMeetingDurationsWithOptional;

  }
}

