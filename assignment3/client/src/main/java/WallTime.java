final class WallTime {
  private final long wallTimePost;
  private final long wallTimeGet;

  public WallTime(long wallTimePost, long wallTimeGet) {
    this.wallTimePost = wallTimePost;
    this.wallTimeGet = wallTimeGet;
  }

  public long getWallTimePost() {
    return wallTimePost;
  }

  public long getWallTimeGet() {
    return wallTimeGet;
  }
}


