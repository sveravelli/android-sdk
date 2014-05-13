package com.ooyala.android;

import org.w3c.dom.Element;

public class Caption {
  protected double _begin = 0;
  protected double _end = 0;
  protected String _text = null;

  Caption() {}

  Caption(Element element) {
    if (!element.getTagName().equals(ClosedCaptions.ELEMENT_P)) { return; }

    String beginStr = element.getAttribute(ClosedCaptions.ATTRIBUTE_BEGIN);
    String durationStr = element.getAttribute(ClosedCaptions.ATTRIBUTE_DUR);
    String endStr = element.getAttribute(ClosedCaptions.ATTRIBUTE_END);

    if (Utils.isNullOrEmpty(beginStr)) { return; }
    _begin = Utils.secondsFromTimeString(beginStr);

    if (!Utils.isNullOrEmpty(endStr)) {
      _end = Utils.secondsFromTimeString(endStr);
    } else if (!Utils.isNullOrEmpty(durationStr)) {
      _end = _begin + Utils.secondsFromTimeString(durationStr);
    } else {
      return;
    }

    _text = "";
    for (int i = 0; i < element.getChildNodes().getLength(); i++) {
      for (String t : element.getChildNodes().item(i).getTextContent().split("[\r\n]")) {
        _text += t.trim();
      }
      if (element.getChildNodes().item(i).getNodeName().equals("br")) _text += "\n";
    }
  }

  /**
   * Fetch the time to begin showing this Caption
   * @return the time to begin showing this Caption (seconds)
   */
  public double getBegin() {
    return _begin;
  }

  /**
   * Fetch the time to stop showing this Caption
   * @return the time to stop showing this Caption (seconds)
   */
  public double getEnd() {
    return _end;
  }

  /**
   * Fetch the text of this Caption
   * @return the text of this Caption
   */
  public String getText() {
    return _text;
  }
}
