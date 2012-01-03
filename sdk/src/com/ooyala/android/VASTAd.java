package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class VASTAd {
  public String _adID;                                                         /**< the ID of the Ad */
  public String _system;                                                       /**< the System */
  public String _systemVersion;                                                /**< the System Version */
  public String _title;                                                        /**< the title of the Ad */
  public String _description;                                                  /**< the description of the Ad */
  public Set<String> _surveyURLs = new HashSet<String>();                      /**< the survey URLs of the Ad */
  public Set<String> _errorURLs = new HashSet<String>();                       /**< the error URLs of the Ad */
  public Set<String> _impressionURLs = new HashSet<String>();                  /**< the impression URLs of the Ad */
  public List<VASTSequenceItem> _sequence = new ArrayList<VASTSequenceItem>(); /**< the ordered sequence of the Ad (List of VASTSequenceItem) */
  public Element _extensions;                                                  /**< the extensions of the Ad */

  /** @internal
   * Initialize a VASTAd using the specified xml (subclasses should override this)
   * @param[in] data the Element containing the xml to use to initialize this VASTAd
   * @returns the initialized VASTAd
   */
  public VASTAd(Element data) {
    if (!data.getTagName().equals(Constants.ELEMENT_AD)) { return; }
    _adID = data.getAttribute(Constants.ATTRIBUTE_ID);
    update(data);
  }

  /** @internal
   * Update the VASTAd using the specified xml (subclasses should override this)
   * @param[in] xml the TBXMLElement containing the xml to use to update this VASTAd
   * @returns YES if the XML was properly formatter, NO if not
   */
  public boolean update(Element xml) {
    if (!(xml.getFirstChild() instanceof Element)) { return false; }
    Element type = (Element)xml.getFirstChild();
    boolean isWrapper = type.getTagName().equals(Constants.ELEMENT_WRAPPER);
    boolean isInLine = type.getTagName().equals(Constants.ELEMENT_IN_LINE);
    if (isInLine || isWrapper) {
      String vastAdTagURI = null;
      Node child = type.getFirstChild();
      while (child != null) {
        if (!(child instanceof Element)) { child = child.getNextSibling(); continue; }
        String text = child.getTextContent();
        boolean textExists = !Utils.isNullOrEmpty(text);
        if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_AD_SYSTEM)) {
          _system = text;
          _systemVersion = ((Element)child).getAttribute(Constants.ATTRIBUTE_VERSION);
        } else if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_AD_TITLE)) {
          _title = text;
        } else if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_DESCRIPTION)) {
          _description = text;
        } else if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_SURVEY)) {
          _surveyURLs.add(text);
        } else if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_ERROR)) {
          _errorURLs.add(text);
        } else if (textExists && ((Element)child).getTagName().equals(Constants.ELEMENT_IMPRESSION)) {
          _impressionURLs.add(text);
        } else if (((Element)child).getTagName().equals(Constants.ELEMENT_EXTENSIONS)) {
          _extensions = (Element)child;
        } else if (isWrapper && ((Element)child).getTagName().equals(Constants.ELEMENT_VAST_AD_TAG_URI)) {
          vastAdTagURI = text;
        } else if (((Element)child).getTagName().equals(Constants.ELEMENT_CREATIVES)) {
          Node creative = child.getFirstChild();
          while (creative != null) {
            if (creative instanceof Element) { addCreative((Element)creative); }
            creative = creative.getNextSibling();
          }
          Collections.sort(_sequence);
        }
        child = child.getNextSibling();
      }
      if (vastAdTagURI != null) {
        try {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          Document doc = db.parse(new InputSource((new URL(vastAdTagURI)).openStream()));
          Element vast = doc.getDocumentElement();
          if (!vast.getTagName().equals(Constants.ELEMENT_VAST)) { return false; }
          String vastVersion = vast.getAttribute(Constants.ATTRIBUTE_VERSION);
          if (Double.parseDouble(vastVersion) < Constants.MINIMUM_SUPPORTED_VAST_VERSION) { return false; }
          Node ad = vast.getFirstChild();
          while (ad != null) {
            if (!(ad instanceof Element) || !((Element)ad).getTagName().equals(Constants.ELEMENT_AD)) { ad = ad.getNextSibling(); continue; }
            if (_adID.equals(((Element)ad).getAttribute(Constants.ATTRIBUTE_ID))) {
              if (update((Element)ad)) { break; }
              else { return false; }
            }
            ad = ad.getNextSibling();
          }
        } catch (Exception e) {
          System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }
  
  /** @internal
   * Add a Creative TBXMLElement
   * @note [jigish]: this assumes that the element is in fact a creative.
   * @param[in] creative the creative to add
   */
  private void addCreative(Element creative) {
    Node type = creative.getFirstChild();
    if (type == null || !(type instanceof Element)) { return; };
    String sequenceNumStr = creative.getAttribute(Constants.ATTRIBUTE_SEQUENCE);
    VASTLinearAd ad = null;
    Element nonLinears = null;
    Element companions = null;
    if (((Element)type).getTagName().equals(Constants.ELEMENT_LINEAR)) {
      ad = new VASTLinearAd((Element)type);
    } else if (((Element)type).getTagName().equals(Constants.ELEMENT_NON_LINEAR_ADS)) {
      nonLinears = (Element)type;
    } else if (((Element)type).getTagName().equals(Constants.ELEMENT_COMPANION_ADS)) {
      companions = (Element)type;
    }
    if (ad == null && nonLinears == null && companions == null) { return; }
    if (sequenceNumStr != null) {
      int sequenceNum = Integer.parseInt(sequenceNumStr);
      boolean added = false;
      for (VASTSequenceItem item : _sequence) {
        int currentSequenceNum = item.getNumber();
        if (currentSequenceNum == sequenceNum) {
          if (ad != null) {
            item.setLinear(ad);
          } else if (nonLinears != null) {
            item.setNonLinears(nonLinears);
          } else if (companions != null) {
            item.setCompanions(companions);
          }
          added = true;
          break;
        }
      }
      if (!added) {
        VASTSequenceItem item = new VASTSequenceItem();
        item.setNumber(sequenceNum);
        if (ad != null) {
          item.setLinear(ad);
        } else if (nonLinears != null) {
          item.setNonLinears(nonLinears);
        } else if (companions != null) {
          item.setCompanions(companions);
        }
        _sequence.add(item);
      }
    } else {
      VASTSequenceItem item = new VASTSequenceItem();
      item.setNumber(_sequence.size());
      if (ad != null) {
        item.setLinear(ad);
      } else if (nonLinears != null) {
        item.setNonLinears(nonLinears);
      } else if (companions != null) {
        item.setCompanions(companions);
      }
      _sequence.add(item);
    }
  }
}
