package baristat.Geography;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Class puts constraints on geography based functionality.
 *
 * @author jbreuch
 *
 */
public final class Geography {
  private static Set<String> states = setStates();

  private Geography() {

  }

  /**
   * This gets the collection of states.
   *
   * @return the collection of states
   */
  public static Collection<String> getStates() {
    return ImmutableSet.copyOf(states);
  }

  private static Set<String> setStates() {
    Set<String> availableStates = new HashSet<>();
    availableStates.addAll(Arrays.asList("AK", "AL", "AR", "AZ", "CA", "CO",
        "CT", "DC", "DE", "FL", "GA", "GU", "HI", "IA", "ID", "IL", "IN", "KS",
        "KY", "LA", "MA", "MD", "ME", "MH", "MI", "MN", "MO", "MS", "MT", "NC",
        "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "PR",
        "PW", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VI", "VT", "WA", "WI",
        "WV", "WY"));
    return ImmutableSet.copyOf(availableStates);
  }

  /**
   * Returns whether or not state is a valid state abbreviation.
   *
   * @param state
   *          the state to check
   * @return true if state is a valid abbreviation, false if not
   */
  public static boolean isValidState(String state) {
    if (state == null || state.length() != 2) {
      throw new IllegalArgumentException(
          "ERROR: State abbreviations must be two valid letters");
    }
    return states.contains(state.toUpperCase());
  }
}
