package com.ooyala.android;

import org.w3c.dom.Element;

public class VASTStream extends Stream {
  private boolean _scalable;            /**< if this stream is scalable */
  private boolean _maintainAspectRatio; /**< if this stream must maintain the aspect ratio */
  private String _vastDeliveryType;     /**< the vast delivery type of this stream */
  private String _apiFramework;         /**< the apiFramework of this stream */

  /** @internal
   * Initialize a Stream using the specified VAST MediaFile XML (subclasses should override this)
   * @param[in] data the Element containing the xml to use to initialize this Stream
   * @returns the initialized Stream
   */
  public VASTStream(Element data) {
    if (!data.getTagName().equals(Constants.ELEMENT_MEDIA_FILE)) { return; }
    this._vastDeliveryType = data.getAttribute(Constants.ATTRIBUTE_DELIVERY);
    this._apiFramework = data.getAttribute(Constants.ATTRIBUTE_API_FRAMEWORK);
    String scalableStr = data.getAttribute(Constants.ATTRIBUTE_SCALABLE);
    if (scalableStr != null) { this._scalable = Boolean.getBoolean(scalableStr); }
    String maintainAspectRatioStr = data.getAttribute(Constants.ATTRIBUTE_MAINTAIN_ASPECT_RATIO);
    if (maintainAspectRatioStr != null) { this._maintainAspectRatio = Boolean.getBoolean(maintainAspectRatioStr); }
    String type = data.getAttribute(Constants.ATTRIBUTE_TYPE);
    if (type != null) {
      if (type.equals(Constants.MIME_TYPE_M3U8)) { this._deliveryType = Constants.DELIVERY_TYPE_HLS; }
      if (type.equals(Constants.MIME_TYPE_MP4)) { this._deliveryType = Constants.DELIVERY_TYPE_MP4; }
      else { this._deliveryType = type; }
    }
    String bitrate = data.getAttribute(Constants.ATTRIBUTE_BITRATE);
    if (bitrate != null) { this._videoBitrate = Integer.parseInt(bitrate); }
    String theWidth = data.getAttribute(Constants.ATTRIBUTE_WIDTH);
    if (theWidth != null) { this._width = Integer.parseInt(Constants.ATTRIBUTE_WIDTH); }
    String theHeight = data.getAttribute(Constants.ATTRIBUTE_HEIGHT);
    if (theHeight != null) {this._height = Integer.parseInt(Constants.ATTRIBUTE_HEIGHT); }
    this._urlFormat = "text";
    this._url = data.getTextContent();
  }

  public boolean isScalable() {
    return _scalable;
  }

  public void setScalable(boolean scalable) {
    this._scalable = scalable;
  }

  public boolean isMaintainAspectRatio() {
    return _maintainAspectRatio;
  }

  public void setMaintainAspectRatio(boolean maintainAspectRatio) {
    this._maintainAspectRatio = maintainAspectRatio;
  }

  public String getVastDeliveryType() {
    return _vastDeliveryType;
  }

  public void setVastDeliveryType(String vastDeliveryType) {
    this._vastDeliveryType = vastDeliveryType;
  }

  public String getApiFramework() {
    return _apiFramework;
  }

  public void set_apiFramework(String apiFramework) {
    this._apiFramework = apiFramework;
  }
}