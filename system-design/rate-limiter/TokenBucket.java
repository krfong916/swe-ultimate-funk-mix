
/**
 * There are three properties of the Token Bucket Algorithm
 * - maximum number of tokens allowed in the bucket
 * - current number of tokens in the bucket
 * - the rate at which tokens are refilled to the bucket
 */
public class TokenBucket {
  // class fields
  private final long maxBucketSize;
  private double currentBucketSize;
  private final long lastRefillTimestamp;
  private final long refillRate;

  // constructor
  public TokenBucket(long maxBucketSize, long refillRate) {
    this.maxBucketSize = maxBucketSize;
    this.refillRate = refillRate;

    currentBucketSize = maxBucketSize; // number of tokens is initially equal to the max capacity
    lastRefillTimestamp = System.nanoTime(); // current time in nanoseconds
  }

  /**
   * We use synchronized because several threads may be calling the method
   * concurrently.
   * We return true if we have enough available tokens in our bucket for the
   * number of tokens needed for a request.
   *
   * @param  {int} tokens represents the cost per operation (request), usually one. slow operations may have a higher cost
   * @return {boolean}
   */
  public synchronized boolean allowRequest(int tokens) {
    refill(); // refill the bucket with tokens accumulated since the last call

    if (currentBucketSize > tokens) { // if the bucket has enough tokens, the call is allowed
      currentBucketSize -= tokens;
      return true;
    }

    return false; // the request is throttled as the bucket does not have enough tokens

  }

  private void refill() {
    long now = System.nanoTime();
    double tokensToAdd = (now - lastRefillTimestamp) * refillRate / 1e9; // the number of tokens accumulated since the last refill
    currentBucketSize = Math.min(currentBucketSize + tokensToAdd, maxBucketSize); // the number of tokens should never exceed max capacity
    lastRefillTimestamp = now;
  }
}
