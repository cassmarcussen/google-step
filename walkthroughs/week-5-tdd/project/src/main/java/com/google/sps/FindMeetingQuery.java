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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  /* doEventAndMeetingShareAttendee returns whether the meeting to schedule and a particular event (from the Events Collection) 
  share attendees. If so, later on, the event must be added to the "bad meeting times" list, since a meeting cannot be scheduled 
  during the time range of the event.
   */
  private boolean doEventAndMeetingShareAttendee(Event event, MeetingRequest meeting, boolean considerMandatoryAttendees, boolean considerOptionalAttendees) {

    Collection<String> mandatoryAttendeesOfMeeting = meeting.getAttendees();
    Collection<String> optionalAttendeesOfMeeting = meeting.getOptionalAttendees();

    Set<String> attendeesOfEventSet = event.getAttendees();

    // eventAndMeetingShareAttendee is true if the event and the meeting share at least one attendee
    boolean eventAndMeetingShareAttendee = false;
    for (String attendee : attendeesOfEventSet)  
    { 
        /* Only check the mandatory attendees collection for comparison if we should include mandatory attendees, 
        as signified by the boolean parameter considerMandatoryAttendees */
        if (considerMandatoryAttendees) {
            if (mandatoryAttendeesOfMeeting.contains(attendee)) {
                eventAndMeetingShareAttendee = true;
            }
        }

        /* Only check the optional attendees collection for comparison if we should include optional attendees, 
        as signified by the boolean parameter considerOptionalAttendees */
        if (considerOptionalAttendees) {
             if (optionalAttendeesOfMeeting.contains(attendee)) {
                eventAndMeetingShareAttendee = true;
            }
        }

    } 

    return eventAndMeetingShareAttendee;

  }

  /* Before we find the viable meeting times, we want to sort and reduce the bad meeting times, or in other words: 
  sort the bad meeting times, then find the overlap in meeting times, then reduce the bad meeting times to larger, non-overlapping 
  bad meeting times which encompass all of the original bad meeting times. This reduces the work-load of 
  getViableMeetingDurations later on, and is a neater solution to this problem than just including this code in another function.
  */
  private List<TimeRange> sortAndReduce(List<TimeRange> badMeetingTimesParam) {

    // Original bad meeting times
    List<TimeRange> badMeetingTimes = badMeetingTimesParam;
    // The merged bad meeting times to return
    List<TimeRange> mergedBadMeetingTimes = new ArrayList<TimeRange>();
    // Sort, so we can find the overlapping times
    Collections.sort(badMeetingTimes, TimeRange.ORDER_BY_START);

    /* Handle the overlapping events. If events overlap, so long as our comparingIndex is 
    still in the badMeetingList size range, we want to push our 'furthestBadEnd' (the latest end 
    time of the overlapping bad meetings) to later times. This handles the case of nested events as well, 
    since the bad meeting times are sorted by start time from earliest to latest and not end time.
    */
    TimeRange firstBadRange;

    int currIndex = 0;
    int furthestBadStart = 0;
    int furthestBadEnd = 0;

    while (currIndex < badMeetingTimes.size()) {

        firstBadRange = badMeetingTimes.get(currIndex);

        furthestBadStart = firstBadRange.start();
        furthestBadEnd = firstBadRange.end();

        int comparingIndex = currIndex + 1;

        // Need to check the comparingIndex < badMeetingTimes.size condition because we remove from the badMeetingTimes
        while(comparingIndex < badMeetingTimes.size() && firstBadRange.overlaps(badMeetingTimes.get(comparingIndex))) {
            if ( badMeetingTimes.get(comparingIndex).end() > furthestBadEnd ) {
                furthestBadEnd =  badMeetingTimes.get(comparingIndex).end();
            }

            comparingIndex++;
        }

        TimeRange combinedBadRange = TimeRange.fromStartEnd(furthestBadStart, furthestBadEnd, false);

        // Add the merged bad time range (will be the initial bad time range we looked at if no overlap, 
        // and will be the combined overlapped bad times if there was overlap
        mergedBadMeetingTimes.add(combinedBadRange);

        currIndex = comparingIndex;
    }

    return mergedBadMeetingTimes;
  }

  private Collection<TimeRange> getViableMeetingDurations(Collection<TimeRange> badMeetingTimes, int durationOfMeeting) {

      Collection<TimeRange> viableMeetingDurations= new ArrayList<>();
      // First, now that all bad meeting times have been added (without duplicates), store local copy of badMeetingTimes as ArrayList 
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

          startOfViableRange = badMeetingList.get(comparingIndex).end(); 

          comparingIndex++;

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

  /* 
  Note: This is an optional feature, and is not yet completely implemented.
  */
  private Collection<TimeRange> findOptimalMeetings(Collection<TimeRange> viableMeetingMandatoryOnly, Collection<Event> eventsWithOptionalAttendees, MeetingRequest request) { 
      
      // HashMap with TimeRange and number of optional attendees who can attend that duration
      HashMap<TimeRange, Integer> viableMeetingDurationsOptimized = new HashMap<>();

      // Find the events that each optional attendee is going to
      HashMap<String, ArrayList<TimeRange>> badTimesForEachOptionalAttendee = new HashMap<>();

      Collection<String> optionalAttendees = request.getOptionalAttendees();

      Integer numOptionalAttendeesOverall = optionalAttendees.size();

      // First, assume all optional attendees can attend the viable meeting times for mandatory only.
      // The algorithm works by removing optional attendees from the viable meeting times if they can't attend

      for (TimeRange viableTime : viableMeetingMandatoryOnly) {
        viableMeetingDurationsOptimized.put(viableTime, numOptionalAttendeesOverall);
      }


      for (String optionalAttendee : optionalAttendees) {

        ArrayList<TimeRange> eventsForOptionalAttendee = new ArrayList<TimeRange>();

        for (Event event : eventsWithOptionalAttendees) {
            TimeRange eventTime = event.getWhen();
            if (event.getAttendees().contains(optionalAttendee)) {
                eventsForOptionalAttendee.add(eventTime);
            }
        }

        badTimesForEachOptionalAttendee.put(optionalAttendee, eventsForOptionalAttendee);

      }

      for (String optionalAttendee : badTimesForEachOptionalAttendee.keySet()) {
           ArrayList<TimeRange> badTimesForOptionalAttendee = badTimesForEachOptionalAttendee.get(optionalAttendee);

            // Note: in each iteration, viableMeetinMandatoryOnly updates. If the next person can attend the viable time, 
            // do nothing. If there is overlap, then you break apart the viable meeting time into smaller times and reduce the number who can attend 
            // of the larger block.
            for (TimeRange viableTime : viableMeetingMandatoryOnly) {
                
                ArrayList<TimeRange> overlappingBadTimes = new ArrayList<TimeRange>();
                for (TimeRange badTime : badTimesForOptionalAttendee) {
                    
                    if(badTime.overlaps(viableTime)) {
                        overlappingBadTimes.add(badTime);
                    }

                }

                Collections.sort(overlappingBadTimes, TimeRange.ORDER_BY_START);

                // If overlapping event for this attendee, the attendee cannot attend this longer duration
                if (!overlappingBadTimes.isEmpty()) {

                    int numOriginalAttendeesForViableTimeBlock = viableMeetingDurationsOptimized.get(viableTime);
                    viableMeetingDurationsOptimized.replace(viableTime, numOriginalAttendeesForViableTimeBlock - 1);

                    // We need to find the furthest end time to compare it to the end of the viable meeting time block
                    int furthestEndTime = overlappingBadTimes.get(0).end();

                    // we need to access index before and after current index, so that's why we iterate by index
                    for (int i=0; i <overlappingBadTimes.size(); i++) {
                        
                        //add in all of the smaller times
                        //HashMap<TimeRange, Integer> viableMeetingDurationsOptimized = new HashMap<>();
                        
                        TimeRange badTime = overlappingBadTimes.get(i);

                        if (badTime.end() > furthestEndTime) {
                            furthestEndTime = badTime.end();
                        }

                        // i==0 since this is for the first (earliest starting) event
                        if (i == 0 && badTime.start() - viableTime.start() >= request.getDuration()) {

                            //everyone can attend this block; it's just a shorter block
                            TimeRange newViableTime = TimeRange.fromStartEnd(viableTime.start(), badTime.start(), false);
                            viableMeetingDurationsOptimized.put(newViableTime, numOriginalAttendeesForViableTimeBlock);
                        }
                        
                        // If the current bad time considered and the next one don't overlap and have a time block in between them greater than the duration time, 
                        // add the time block in between them to the optimized viable meeting durations hashmap.
                        if (i + 1 < overlappingBadTimes.size() && !overlappingBadTimes.get(i+1).overlaps(badTime) && overlappingBadTimes.get(i+1).start() - badTime.end() >= request.getDuration()) {
                            //everyone can attend this block; it's just a shorter block
                            TimeRange newViableTime = TimeRange.fromStartEnd(badTime.end(), overlappingBadTimes.get(i+1).start(), false);
                            viableMeetingDurationsOptimized.put(newViableTime, numOriginalAttendeesForViableTimeBlock);
                        }

                    }

                    if (viableTime.end() - furthestEndTime >= request.getDuration()) {
                        TimeRange newViableTime = TimeRange.fromStartEnd(furthestEndTime, viableTime.end(), false);
                        viableMeetingDurationsOptimized.put(newViableTime, numOriginalAttendeesForViableTimeBlock);
                    }

                }

            }
      }

    /* Now, we have all the blocks of time that all mandatory attendees can attend, and some optional attendees can attend, 
    along with the number of optional attendees who can attend each block of time.
    Just return those blocks of time with the greatest number of optional attendees that can attend.
    */
    int topNumberOfOptionalAttendeesForAnyBlock = 0;

    for (Integer numOptionalAttendeesForEvent : viableMeetingDurationsOptimized.values()) {
        if ((int) numOptionalAttendeesForEvent > topNumberOfOptionalAttendeesForAnyBlock) {
            topNumberOfOptionalAttendeesForAnyBlock = (int) numOptionalAttendeesForEvent;
        }
    }

    ArrayList<TimeRange> timeRangesWithTopNumOfOptionalAttendees = new ArrayList<>();
    for (TimeRange blockOfTime : viableMeetingDurationsOptimized.keySet()) {
        if (viableMeetingDurationsOptimized.get(blockOfTime) == topNumberOfOptionalAttendeesForAnyBlock) {
            timeRangesWithTopNumOfOptionalAttendees.add(blockOfTime);
        }
    }

    Collections.sort(timeRangesWithTopNumOfOptionalAttendees, TimeRange.ORDER_BY_START);

    return timeRangesWithTopNumOfOptionalAttendees;

  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Use Collection so that no duplicate elements (i.e. bad meeting times) are added. No optional attendees considered.
    List<TimeRange> badMeetingTimesNoOptional = new ArrayList<TimeRange>();
    // If include optional attendees
    List<TimeRange> badMeetingTimesWithOptional = new ArrayList<TimeRange>();
    // Used for optimizing for including all mandatory and as many optional attendees as possible 
    List<TimeRange> badMeetingTimesOptionalOnly = new ArrayList<TimeRange>();

    // viableMeetingDurations is the Collection of viable meeting durations to return
    // no optional attendees included
    Collection<TimeRange> viableMeetingDurationsNoOptional;   
    // with optional attendees 
    Collection<TimeRange> viableMeetingDurationsWithOptional;

    Collection<Event> eventsWithOptionalAttendees = new HashSet<>();

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
        boolean eventAndMeetingShareMandatoryAttendee = doEventAndMeetingShareAttendee(event, request, true, false);

         // eventAndMeetingShareAnyAttendee is true if the event and the meeting share at least one attendee, either mandatory or optional
        boolean eventAndMeetingShareAnyAttendee = doEventAndMeetingShareAttendee(event, request, true, true);

        // eventAndMeetingShareOptionalAttendee is true if the event and the meeting share at least one optional attendee
        boolean eventAndMeetingShareOptionalAttendee = doEventAndMeetingShareAttendee(event, request, false, true);

        // check that contains will work, call the equals of timerange
        if (eventAndMeetingShareMandatoryAttendee && !badMeetingTimesNoOptional.contains(timeRangeOfEvent)) {
            badMeetingTimesNoOptional.add(timeRangeOfEvent);
        }

        if (eventAndMeetingShareAnyAttendee && !badMeetingTimesWithOptional.contains(timeRangeOfEvent)) {
            badMeetingTimesWithOptional.add(timeRangeOfEvent);
        }

        // Used for optimizing the meeting times to include as many optional attendees as possible
        if (eventAndMeetingShareOptionalAttendee && !eventsWithOptionalAttendees.contains(event)) {
            //badMeetingTimesOptionalOnly.add(timeRangeOfEvent);

            // For the optimizing to include all mandatory attendees and as many optional attendees as possible (we want the specific events 
            // since we need to know the number of optional attendees
            eventsWithOptionalAttendees.add(event);

        }

    }

    badMeetingTimesNoOptional = sortAndReduce(badMeetingTimesNoOptional);
    badMeetingTimesWithOptional = sortAndReduce(badMeetingTimesWithOptional);

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
    } else if (viableMeetingDurationsWithOptional.size() > 0 && request.getAttendees().size() == 0) {
        /* If meetings worked including the optional attendees, and there does not exist mandatory attendees, 
        return the time slots that fit just the optional attendees.
        */
       return viableMeetingDurationsWithOptional;
    } else {
        /* Implement an optimized version of the optional attendee functionality. If no time exists for all optional 
        and mandatory attendees, find the time slots that allow mandatory attendees and the greatest possible number of optional 
        attendees to attend. */

        Collection<String> optionalAttendees = request.getOptionalAttendees();
        return findOptimalMeetings(viableMeetingDurationsNoOptional, eventsWithOptionalAttendees, request);

    }
 
  }
}

