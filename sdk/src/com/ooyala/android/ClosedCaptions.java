package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.json.*;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ooyala.android.Constants.ReturnState;

public class ClosedCaptions {
  protected Set<String> _languages = new HashSet<String>();
  protected String _defaultLanguage = null;
  protected URL _url = null;
  protected Map<String, String> _styles = new HashMap<String, String>();
  protected Map<String, List<Caption>> _captions = new HashMap<String, List<Caption>>();

  ClosedCaptions() {}

  ClosedCaptions(JSONObject data) {
    update(data);
  }

  ReturnState update(JSONObject data) {
    if (data == null) { return ReturnState.STATE_FAIL; }

    try {
      if (data.isNull(Constants.KEY_LANGUAGES)) {
        System.out.println("ERROR: Fail to update closed captions because no languages exist!");
        return ReturnState.STATE_FAIL;
      }
      JSONArray theLanguages = data.getJSONArray(Constants.KEY_LANGUAGES);

      if (data.isNull(Constants.KEY_URL)) {
        System.out.println("ERROR: Fail to update closed captions because no url exists!");
        return ReturnState.STATE_FAIL;
      }
      String theURL = data.getString(Constants.KEY_URL);

      _languages.clear();
      for (int i = 0; i < theLanguages.length(); i++) {
        String language = theLanguages.getString(i);
        _languages.add(language);
        _captions.put(language, new ArrayList<Caption>());
      }
      try {
        _url = new URL(theURL);
      } catch (MalformedURLException e) {
        System.out.println("ERROR: Fail to update closed captions because url is invalid: " + theURL);
        return ReturnState.STATE_FAIL;
      }

      if (!data.isNull(Constants.KEY_DEFAULT_LANGUAGE)) {
        _defaultLanguage = data.getString(Constants.KEY_DEFAULT_LANGUAGE);
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
    return ReturnState.STATE_MATCHED;
  }

  /**
   * Fetch the Set of supported languages
   * @return the Set of supported languages
   */
  public Set<String> getLanguages() {
    return _languages;
  }

  /**
   * Fetch the default language to display ClosedCaptions in.
   * @return the default language to display ClosedCaptions in.
   */
  public String getDefaultLanguage() {
    return _defaultLanguage;
  }

  /**
   * Fetch the URL of the ClosedCaptions file.
   * @return the URL pf the ClosedCaptions file.
   */
  public URL getURL() {
    return _url;
  }

  private boolean parseHeadXML(Element head) {
    if (!head.getTagName().equals(Constants.ELEMENT_HEAD)) { return false; }
    // TODO: support the same DFXP that we do in flash (no layout, basic styling)
    return true;
  }

  private boolean parseBodyXML(Element body) {
    if (!body.getTagName().equals(Constants.ELEMENT_BODY)) { return false; }

    /**
     * NOTE: we do not support div tags with temporal elements. we only support one div per language, each
     * with a set of p elements inside. this comes from the flash player's ClosedCaptionParser.parseBody. see
     * rui's comment there for more information.
     */
    NodeList divs = body.getElementsByTagName(Constants.ELEMENT_DIV);
    for (int i = 0; i < divs.getLength(); i++) {
      Element div = (Element) divs.item(i);
      String lang = div.getAttribute(Constants.ATTRIBUTE_XML_LANG);
      List<Caption> captionsForLang = Utils.isNullOrEmpty(lang) ? null : _captions.get(lang);
      String begin = div.getAttribute(Constants.ATTRIBUTE_BEGIN);
      if (!Utils.isNullOrEmpty(begin) || captionsForLang == null) {
        continue;
      }

      NodeList ps = div.getElementsByTagName(Constants.ELEMENT_P);
      for (int j = 0; j < ps.getLength(); j++) {
        Element p = (Element) ps.item(j);
        Caption caption = new Caption(p);
        if (caption != null) {
          captionsForLang.add(caption);
        }
      }
    }

    return true;
  }

  private boolean update(Element xml) {
    if (!xml.getTagName().equals(Constants.ELEMENT_TT)) { return false; }

    NodeList headList = xml.getElementsByTagName(Constants.ELEMENT_HEAD);
    if (headList != null && headList.getLength() > 0) {
      if (!parseHeadXML((Element) headList.item(0))) { return false; }
    }

    NodeList bodyList = xml.getElementsByTagName(Constants.ELEMENT_BODY);
    if (bodyList != null && bodyList.getLength() > 0) {
      if (!parseBodyXML((Element) bodyList.item(0))) { return false; }
    }

    return true;
  }

  /**
   * Fetch the ClosedCaptions information required for playback. This should get called automatically.
   * @return true if success, false if failure
   */
  public boolean fetchClosedCaptionsInfo() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(_url.openStream()));
      Element element = doc.getDocumentElement();
      return update(element);
    } catch (Exception e) {
      Log.e(this.getClass().getName(), "ERROR: Unable to fetch closed captions info: " + e);
      return false;
    }
  }

  /**
   * Fetch the list of Caption objects for the language specified.
   * @param language the language to fetch the Caption objects for
   * @return a list of Caption objects
   */
  @SuppressLint("DefaultLocale")
  public List<Caption> closedCaptionsForLanguage(String language) {
    return _captions.get(language.toLowerCase());
  }

  /**
   * Fetch the Caption object corresponding to the language and time specified
   * @param language the language to fetch the Caption object for
   * @param time the time to fetch the Caption object for
   * @return the Caption object
   */
  public Caption getCaption(String language, double time) {
    List<Caption> captionsForLanguage = closedCaptionsForLanguage(language);
    if (captionsForLanguage == null || captionsForLanguage.size() == 0) { return null; }

    // Binary Search!
    int prevCurrIdx = -1;
    int currIdx = captionsForLanguage.size() / 2;
    int topIdx = captionsForLanguage.size() - 1;
    int botIdx = 0;
    boolean found = false;
    while (!found && topIdx >= 0 && botIdx < captionsForLanguage.size()) {
      // check current
      Caption curr = captionsForLanguage.get(currIdx);
      if (curr.getBegin() <= time && time < curr.getEnd()) {
        found = true;
        break;
      }
      // adjust indicies
      if (prevCurrIdx == currIdx) {
        break;
      }
      if (topIdx == botIdx) {
        break;
      }
      if (time < curr.getBegin()) {
        topIdx = currIdx - 1;
      } else {
        botIdx = currIdx + 1;
      }
      prevCurrIdx = currIdx;
      currIdx = botIdx + ((topIdx - botIdx) / 2);
    }
    if (found) { return captionsForLanguage.get(currIdx); }
    return null;
  }

}
