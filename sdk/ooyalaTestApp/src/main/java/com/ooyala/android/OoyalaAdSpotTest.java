package com.ooyala.android;

import android.test.AndroidTestCase;

public class OoyalaAdSpotTest extends AndroidTestCase {
  public OoyalaAdSpotTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  /**
   * Test create. Also tests update.
   */
  public void testInitializers() {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
    assertNotNull(adSpot);
    assertEquals(OoyalaAdSpot.class, adSpot.getClass());
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", adSpot.getEmbedCode());
    assertEquals(0, adSpot.getTime());
    assertEquals("http://www.bhangraempire.com", adSpot.getClickURL().toString());
    assertEquals(1, adSpot.getTrackingURLs().size());
    assertEquals("http://www.ooyala.com/track", adSpot.getTrackingURLs().get(0).toString());
  }

  /**
   * Test embedCodesToAuthorize
   */
  public void testEmbedCodesToAuthorize() {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
    assertEquals(1, adSpot.embedCodesToAuthorize().size());
    assertEquals(adSpot.getEmbedCode(), adSpot.embedCodesToAuthorize().get(0));
  }

  /**
   * Test fetchPlaybackInfo and getStream
   */
  public void testStreamsInfo() {
    PlayerDomain domain = new PlayerDomain("http://www.ooyala.com");
    PlayerAPIClient api = new PlayerAPIClient(TestConstants.TEST_PCODE, domain, null);
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_AD_OOYALA), new OoyalaAPIClient(api));
    assertTrue(adSpot.fetchPlaybackInfo());
    // FIXME: This test asset has multiple streams with the same resolution and bitrate...
    String url = adSpot.getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));
  }
}