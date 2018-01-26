/**
 * The MIT License
 * Copyright (c) 2015 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ee.ria.xroad.common.conf.globalconf;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple thread-safe time based object cache.
 */
@Slf4j
public class TimeBasedObjectCache {

  @Value
  private static class TimeAndValue {
    private LocalDateTime time;
    private Object value;
  }

  private final int expireSeconds;

  private Map<String, TimeAndValue> values = new ConcurrentHashMap<>();

  /**
   * Constructor.
   */
  public TimeBasedObjectCache(int expireSeconds) {
    if (expireSeconds < 0) {
      throw new IllegalArgumentException("Cache expiration period cannot be negative");
    }
    this.expireSeconds = expireSeconds;
    log.trace("creating TimeBasedObjectCache with expiration of {} seconds", expireSeconds);
  }

  /**
   * Check if cache value is valid. That is, not null and not yet expired.
   */
  public boolean isValid(String key) {
    TimeAndValue timeAndValue = values.get(key);
    LocalDateTime now = LocalDateTime.now().minusSeconds(expireSeconds);
    return timeAndValue != null && timeAndValue.getTime().isAfter(now);
  }

  /**
   * Get value from cache. The user should first check the validity with isValid.
   */
  public Object getValue(String key) {
    return values.get(key).getValue();
  }

  /**
   * Set cache value. Can also be used to invalidate cache value by setting it to null.
   */
  public void setValue(String key, Object value) {
    values.put(key, new TimeAndValue(LocalDateTime.now(), value));
  }

  /**
   * Tells whether the cache is enabled or not
   * @return true if enabled
   */
  public boolean isEnabled() {
      return expireSeconds > 0;
  }
}
