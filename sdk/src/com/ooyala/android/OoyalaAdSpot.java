package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.*;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metadata for the specified content item.
 *
 */
public class OoyalaAdSpot extends AdSpot implements AuthorizableItem, PlayableItem
{
  protected Set<Stream> _streams = new HashSet<Stream>();
  protected String _embedCode = null;
  protected boolean _isAuthorized = false;
  protected int _authCode = Constants.AUTH_CODE_NOT_REQUESTED;

  public OoyalaAdSpot()
  {
  }

  public OoyalaAdSpot(String embedCode)
  {
    _embedCode = embedCode;
  }

  public OoyalaAdSpot(JSONObject data, PlayerAPIClient api)
  {
    _api = api;
    update(data);
  }

  /**
   * Get the embedCode for this content item.
   * @return embedCode of this content item
   */
  public String getEmbedCode()
  {
    return _embedCode;
  }

  public ReturnState update(JSONObject data)
  {
    switch (super.update(data))
    {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try
    {
      if (_embedCode != null && !data.isNull(_embedCode))
      {
        JSONObject myData = data.getJSONObject(_embedCode);
        if (!myData.isNull(Constants.KEY_AUTHORIZED))
        {
          _isAuthorized = myData.getBoolean(Constants.KEY_AUTHORIZED);
          if (!myData.isNull(Constants.KEY_CODE))
          {
            int theAuthCode = myData.getInt(Constants.KEY_CODE);
            if (theAuthCode < Constants.AUTH_CODE_MIN_AUTH_CODE || theAuthCode > Constants.AUTH_CODE_MAX_AUTH_CODE)
            {
              _authCode = Constants.AUTH_CODE_UNKNOWN;
            }
            else
            {
              _authCode = theAuthCode;
            }
          }
          if (_isAuthorized && !myData.isNull(Constants.KEY_STREAMS))
          {
            JSONArray streams = myData.getJSONArray(Constants.KEY_STREAMS);
            if (streams.length() > 0)
            {
              _streams.clear();
              for (int i = 0; i < streams.length(); i++)
              {
                Stream stream = new Stream(streams.getJSONObject(i));
                if (stream != null)
                {
                  _streams.add(stream);
                }
              }
            }
          }
        }
        return ReturnState.STATE_MATCHED;
      }
      if (data.isNull(Constants.KEY_AD_EMBED_CODE))
      {
        System.out.println("ERROR: Fail to update OoyalaAdSpot with dictionary because no ad embed code exists!");
        return ReturnState.STATE_FAIL;
      }
      _embedCode = data.getString(Constants.KEY_AD_EMBED_CODE);
      return ReturnState.STATE_MATCHED;
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
  }

  public boolean fetchPlaybackInfo()
  {
    return _api.authorize(this);
  }

  public Stream getStream()
  {
    return Stream.bestStream(_streams);
  }

  public List<String> embedCodesToAuthorize()
  {
    List<String> embedCodes = new ArrayList<String>();
    embedCodes.add(_embedCode);
    return embedCodes;
  }

}