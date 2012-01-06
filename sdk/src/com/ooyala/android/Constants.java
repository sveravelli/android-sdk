package com.ooyala.android;

class Constants
{
  public static final int    RESPONSE_LIFE_SECONDS    = 5*60;

  public static final String JS_ANALYTICS_HOST        = "http://player.ooyala.com";
  public static final String JS_ANALYTICS_URI         = "/reporter.js";

  public static final String API_CONTENT_TREE         = "content_tree";
  public static final String API_CONTENT_TREE_NEXT    = "content_tree_next";
  public static final String API_AUTHORIZE            = "authorize";
  public static final String API_METADATA             = "metadata";

  public static final String AUTHORIZE_HOST           = "http://eng-dynamic-238.v101.mtv:4567"; // TODO fix this once we push sas
  public static final String AUTHORIZE_CONTENT_ID_URI = "/sas/player_api/authorization/content_id/%s/%s";
  public static final String AUTHORIZE_EMBED_CODE_URI = "/sas/player_api/authorization/embed_code/%s/%s";
  public static final String AUTHORIZE_PUBLIC_KEY_B64 = "MCgCIQD1PX86jvLr5bB3b5IFEze7TiWGEaRSHl5Ls7/3AKO5IwIDAQAB";
  public static final String AUTHORIZE_PUBLIC_KEY_NAME = "sas_public_key";
  public static final int    AUTHORIZE_SIGNATURE_DIGEST_LENGTH  = 20;

  public static final String CONTENT_TREE_HOST        = "http://player.ooyala.com";
  public static final String CONTENT_TREE_URI         = "/player_api/content_tree/embed_code/%s/%s";
  public static final String CONTENT_TREE_BY_EXTERNAL_ID_URI = "/player_api/content_tree/external_id/%s/%s";
  public static final String CONTENT_TREE_NEXT_URI    = "/player_api/content_tree/next/%s/%s";

  public static final String METADATA_HOST            = "http://player.ooyala.com";
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
  public static final String KEY_CLOSED_CAPTIONS      = "closed_captions";
  public static final String KEY_LANGUAGES            = "languages";
  public static final String KEY_DEFAULT_LANGUAGE     = "default_language";
  public static final String KEY_NEXT_TOKEN           = "next_token";
  public static final String KEY_PARENT               = "parent";
  public static final String KEY_API                  = "api";
  public static final String KEY_CALLBACK             = "callback";

  public static final String DEVICE_ANDROID_SDK       = "android_sdk";

  public static final String CONTENT_TYPE_CHANNEL_SET = "MultiChannel";
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
  public static final String SEPARATOR_NEWLINE        = "\n";
  public static final String SEPARATOR_COLON          = ":";

  public static final String DELIVERY_TYPE_HLS        = "hls";
  public static final String DELIVERY_TYPE_MP4        = "mp4";
  public static final String DELIVERY_TYPE_REMOTE_ASSET = "remote_asset";

  public static final int    DEFAULT_AD_TIME_SECONDS  = 0;

  public static final double MINIMUM_SUPPORTED_VAST_VERSION = 2.0;
  public static final String ELEMENT_VAST             = "VAST";
  public static final String ELEMENT_AD               = "Ad";
  public static final String ELEMENT_IN_LINE          = "InLine";
  public static final String ELEMENT_WRAPPER          = "Wrapper";
  public static final String ELEMENT_AD_SYSTEM        = "AdSystem";
  public static final String ELEMENT_AD_TITLE         = "AdTitle";
  public static final String ELEMENT_DESCRIPTION      = "Description";
  public static final String ELEMENT_SURVEY           = "Survey";
  public static final String ELEMENT_ERROR            = "Error";
  public static final String ELEMENT_IMPRESSION       = "Impression";
  public static final String ELEMENT_CREATIVES        = "Creatives";
  public static final String ELEMENT_CREATIVE         = "Creative";
  public static final String ELEMENT_LINEAR           = "Linear";
  public static final String ELEMENT_NON_LINEAR_ADS   = "NonLinearAds";
  public static final String ELEMENT_COMPANION_ADS    = "CompanionAds";
  public static final String ELEMENT_EXTENSIONS       = "Extensions";
  public static final String ELEMENT_DURATION         = "Duration";
  public static final String ELEMENT_TRACKING_EVENTS  = "TrackingEvents";
  public static final String ELEMENT_TRACKING         = "Tracking";
  public static final String ELEMENT_AD_PARAMETERS    = "AdParameters";
  public static final String ELEMENT_VIDEO_CLICKS     = "VideoClicks";
  public static final String ELEMENT_CLICK_THROUGH    = "ClickThrough";
  public static final String ELEMENT_CLICK_TRACKING   = "ClickTracking";
  public static final String ELEMENT_CUSTOM_CLICK     = "CustomClick";
  public static final String ELEMENT_MEDIA_FILES      = "MediaFiles";
  public static final String ELEMENT_MEDIA_FILE       = "MediaFile";
  public static final String ELEMENT_VAST_AD_TAG_URI  = "VASTAdTagURI";
  public static final String ELEMENT_TT               = "tt";
  public static final String ELEMENT_HEAD             = "head";
  public static final String ELEMENT_BODY             = "body";
  public static final String ELEMENT_STYLING          = "styling";
  public static final String ELEMENT_STYLE            = "style";
  public static final String ELEMENT_DIV              = "div";
  public static final String ELEMENT_P                = "p";
  public static final String ELEMENT_SPAN             = "span";
  public static final String ELEMENT_BR               = "br";
  public static final String ATTRIBUTE_VERSION        = "version";
  public static final String ATTRIBUTE_ID             = "id";
  public static final String ATTRIBUTE_SEQUENCE       = "sequence";
  public static final String ATTRIBUTE_EVENT          = "event";
  public static final String ATTRIBUTE_DELIVERY       = "delivery";
  public static final String ATTRIBUTE_TYPE           = "type";
  public static final String ATTRIBUTE_BITRATE        = "bitrate";
  public static final String ATTRIBUTE_WIDTH          = "width";
  public static final String ATTRIBUTE_HEIGHT         = "height";
  public static final String ATTRIBUTE_SCALABLE       = "scalable";
  public static final String ATTRIBUTE_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";
  public static final String ATTRIBUTE_API_FRAMEWORK  = "apiFramework";
  public static final String ATTRIBUTE_BEGIN          = "begin";
  public static final String ATTRIBUTE_END            = "end";
  public static final String ATTRIBUTE_DUR            = "dur";
  public static final String ATTRIBUTE_XML_LANG       = "xml:lang";

  public static final String MIME_TYPE_MP4            = "video/mp4";
  public static final String MIME_TYPE_M3U8           = "application/x-mpegURL";

  public enum ReturnState { STATE_MATCHED, STATE_UNMATCHED, STATE_FAIL };
}
