package tdc.java.imagedownloader.url;


import org.junit.Assert;
import org.junit.Test;

/**
 * @author Cuong Truong
 */
public class UrlPatternHandlingSessionTest {
  @Test
  public void test() {
    UrlPatternHandlingSession session = new UrlPatternHandlingSession("http://www.foo.com/foo1/foo{type=int,index=0,initialValue=2,upperBound=5,maxStringSize=3,order=asc}.jpg");
    Assert.assertEquals("http://www.foo.com/foo1/foo002.jpg", session.getNextUrl());
    Assert.assertEquals("http://www.foo.com/foo1/foo003.jpg", session.getNextUrl());
    Assert.assertEquals("http://www.foo.com/foo1/foo004.jpg", session.getNextUrl());
    Assert.assertNull(session.getNextUrl());
  }
}
