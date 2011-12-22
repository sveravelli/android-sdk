package com.ooyala.android;

class Constants
{
  public static final int    RESPONSE_LIFE_SECONDS    = 5*60;

  public static final String API_CONTENT_TREE         = "content_tree";
  public static final String API_CONTENT_TREE_NEXT    = "content_tree_next";
  public static final String API_AUTHORIZE            = "authorize";
  public static final String API_METADATA             = "metadata";

  public static final String AUTHORIZE_HOST           = "http://dev.corp.ooyala.com:4567";
  public static final String AUTHORIZE_CONTENT_ID_URI = "/sas/player_api/authorization/content_id/%s/%s";
  public static final String AUTHORIZE_EMBED_CODE_URI = "/sas/player_api/authorization/embed_code/%s/%s";
  public static final String AUTHORIZE_PUBLIC_KEY_B64 = "MCgCIQD1PX86jvLr5bB3b5IFEze7TiWGEaRSHl5Ls7/3AKO5IwIDAQAB";
  public static final String AUTHORIZE_PUBLIC_KEY_NAME = "sas_public_key";
  public static final int    AUTHORIZE_SIGNATURE_DIGEST_LENGTH  = 20;

  public static final String CONTENT_TREE_HOST        = "http://dev.corp.ooyala.com:3000";
  public static final String CONTENT_TREE_URI         = "/player_api/content_tree/embed_code/%s/%s";
  public static final String CONTENT_TREE_BY_EXTERNAL_ID_URI = "/player_api/content_tree/external_id/%s/%s";
  public static final String CONTENT_TREE_NEXT_URI    = "/player_api/content_tree/next/%s/%s";

  public static final String METADATA_HOST            = "http://dev.corp.ooyala.com:3000";
  public static final String METADATA_CONTENT_ID_URI  = "/player_api/metadata/content_id/%s/%s/android";
  public static final String METADATA_EMBED_CODE_URI  = "/player_api/metadata/embed_code/%s/%s/android";

  public static final String KEY_EMBED_CODE           = "embed_code";
  public static final String KEY_EXTERNAL_ID          = "external_id";
  public static final String KEY_API_KEY              = "api_key";
  public static final String KEY_DOMAIN               = "domain";
  public static final String KEY_EXPIRES              = "expires";
  public static final String KEY_SIGNATURE            = "signature";
  public static final String KEY_DEVICE               = "device";
  public static final String KEY_ERRORS               = "errors";
  public static final String KEY_CODE                 = "code";
  public static final String KEY_AUTHORIZATION_DATA   = "authorization_data";
  public static final String KEY_AUTHORIZED           = "authorized";
  public static final String KEY_CONTENT_TREE         = "content_tree";
  public static final String KEY_CONTENT_TOKEN        = "content_token";
  public static final String KEY_TITLE                = "title";
  public static final String KEY_DESCRIPTION          = "description";
  public static final String KEY_CONTENT_TYPE         = "content_type";
  public static final String KEY_CHILDREN             = "children";
  public static final String KEY_VIDEO_BITRATE        = "video_bitrate";
  public static final String KEY_AUDIO_BITRATE        = "audio_bitrate";
  public static final String KEY_VIDEO_CODEC          = "video_codec";
  public static final String KEY_HEIGHT               = "height";
  public static final String KEY_WIDTH                = "width";
  public static final String KEY_FRAMERATE            = "framerate";
  public static final String KEY_DELIVERY_TYPE        = "delivery_type";
  public static final String KEY_URL                  = "url";
  public static final String KEY_DATA                 = "data";
  public static final String KEY_FORMAT               = "format";
  public static final String KEY_STREAMS              = "streams";
  public static final String KEY_MESSAGE              = "message";
  public static final String KEY_ADS                  = "ads";
  public static final String KEY_TYPE                 = "type";
  public static final String KEY_AD_EMBED_CODE        = "ad_embed_code";
  public static final String KEY_TIME                 = "time";
  public static final String KEY_CLICK_URL            = "click_url";
  public static final String KEY_TRACKING_URL         = "tracking_url";
  public static final String KEY_IS_LIVE_STREAM       = "is_live_stream";
  public static final String KEY_ASPECT_RATIO         = "aspect_ratio";
  public static final String KEY_NEXT_CHILDREN        = "next_children";
  public static final String KEY_DURATION             = "duration";
  public static final String KEY_NEXT_TOKEN           = "next_token";
  public static final String KEY_PARENT               = "parent";
  public static final String KEY_API                  = "api";
  public static final String KEY_CALLBACK             = "callback";

  public static final String DEVICE_IPHONE            = "iphone";
  public static final String DEVICE_IPAD              = "ipad";

  public static final String CONTENT_TYPE_CHANNEL_SET = "ChannelSet";
  public static final String CONTENT_TYPE_CHANNEL     = "Channel";
  public static final String CONTENT_TYPE_VIDEO       = "Video";
  public static final String CONTENT_TYPE_LIVE_STREAM = "LiveStream";

  public static final String STREAM_URL_FORMAT_TEXT   = "text";
  public static final String STREAM_URL_FORMAT_B64    = "encoded";

  public static final String AD_TYPE_OOYALA           = "ooyala";
  public static final String AD_TYPE_VAST             = "vast";

  public static final String METHOD_GET               = "GET";
  public static final String METHOD_PUT               = "PUT";
  public static final String METHOD_POST              = "POST";

  public static final String SEPARATOR_AMPERSAND      = "&";
  public static final String SEPARATOR_COMMA          = ",";
  public static final String SEPARATOR_EMPTY          = "";

  public static final String DELIVERY_TYPE_HLS        = "hls";
  public static final String DELIVERY_TYPE_MP4        = "mp4";
  public static final String DELIVERY_TYPE_REMOTE_ASSET = "remote_asset";

  public static final int DEFAULT_AD_TIME_SECONDS     = 0;

  public enum ReturnState { STATE_MATCHED, STATE_UNMATCHED, STATE_FAIL };

  public static final int AUTH_CODE_UNKNOWN = -2;                       /**< The authorization code was invalid */
  public static final int AUTH_CODE_NOT_REQUESTED = -1;                 /**< The authorization has not been requested for this item */
  public static final int AUTH_CODE_MIN_AUTH_CODE = 0;                  /**< The minimum value for auth codes from the server */
  public static final int AUTH_CODE_AUTHORIZED = 0;                     /**< The item is authorized */
  public static final int AUTH_CODE_UNAUTHORIZED_PARENT = 1;            /**< The item's parent is unauthorized */
  public static final int AUTH_CODE_UNAUTHORIZED_DOMAIN = 2;            /**< The item is not authorized for this domain */
  public static final int AUTH_CODE_UNAUTHORIZED_LOCATION = 3;          /**< The item is not authorized for this location */
  public static final int AUTH_CODE_BEFORE_FLIGHT_TIME = 4;             /**< The item has been requested before its flight time */
  public static final int AUTH_CODE_AFTER_FLIGHT_TIME = 5;              /**< The item has been requested after its flight time */
  public static final int AUTH_CODE_OUTSIDE_RECURRING_FLIGHT_TIMES = 6; /**< The item has been requested outside of its recurring flight time */
  public static final int AUTH_CODE_BAD_EMBED_CODE = 7;                 /**< The item's embed code is invalid */
  public static final int AUTH_CODE_INVALID_SIGNATURE = 8;              /**< The signature of the request is invalid */
  public static final int AUTH_CODE_MISSING_PARAMS = 9;                 /**< The request had missing params */
  public static final int AUTH_CODE_MISSING_RULE_SET = 10;              /**< The server is missing its rule set */
  public static final int AUTH_CODE_UNAUTHORIZED = 11;                  /**< The item is unauthorized */
  public static final int AUTH_CODE_MISSING_PCODE = 12;                 /**< The request was missing the pcode */
  public static final int AUTH_CODE_UNAUTHORIZED_DEVICE = 13;           /**< The item is not authorized for this device */
  public static final int AUTH_CODE_INVALID_TOKEN = 14;                 /**< The request's token was invalid */
  public static final int AUTH_CODE_TOKEN_EXPIRED = 15;                 /**< The request's token was expired */
  public static final int AUTH_CODE_MAX_AUTH_CODE = 16;                 /**< The maximum value for auth codes from the server */

}
