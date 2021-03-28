package net.tourbook.weather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OWMResults {
   static public class OWMResponse {

      public String               GMN_localDateTime  = null;

      public String               GMN_UTCDateTime    = null;

      public Integer              GMN_index          = null;

      public Integer              GMN_ElapsedSeconds = null;

      public Float                lat                = null;

      public Float                lon                = null;

      public String               timezone           = null;
      public Integer              timezone_offset    = null;

      public OWMResponse_Current  current            = null;

      public OWMResponse_Hourly[] hourly;
   }

   static public class OWMResponse_Current {

      public Integer               dt         = null;

      public Integer               sunrise    = null;

      public Integer               sunset     = null;

      public Float                 temp       = null;

      public Float                 feels_like = null;

      public Integer               pressure   = null;

      public Integer               humidity   = null;

      public Float                 dew_point  = null;

      public Float                 uvi        = null;

      public Integer               clouds     = null;

      public Integer               visibility = null;

      public Float                 wind_speed = null;

      public Float                 wind_gust  = null;

      public OWMResponse_Rain      rain       = null;

      public OWMResponse_Snow      snow       = null;

      public Integer               wind_deg   = null;

      public OWMResponse_Weather[] weather;
   }

   static public class OWMResponse_Hourly {

      public Integer               dt         = null;

      public Float                 temp       = null;

      public Float                 feels_like = null;

      public Integer               pressure   = null;

      public Integer               humidity   = null;

      public Float                 dew_point  = null;

      public Integer               clouds     = null;

      public Integer               visibility = null;

      public Float                 wind_speed = null;

      public Float                 wind_gust  = null;

      public OWMResponse_Rain      rain       = null;

      public OWMResponse_Snow      snow       = null;

      public Integer               wind_deg   = null;

      public OWMResponse_Weather[] weather;
   }

   static public class OWMResponse_Rain {

      @JsonProperty("1h")
      public Float h1 = null;

      @JsonProperty("3h")
      public Float h3 = null;
   }

   static public class OWMResponse_Snow {

      @JsonProperty("1h")
      public Float h1 = null;

      @JsonProperty("3h")
      public Float h3 = null;
   }

   static public class OWMResponse_Weather {

      public Integer id          = null;

      public String  main        = null;

      public String  description = null;

      public String  icon        = null;
   }

   static public class OWMWeather_Description_Map {

      public Integer weight        = 0;
      public Float   weightPercent = (float) 0.0;
      public String  description   = null;
      public Integer id            = null;
   }

   static public class OWMWeather_Main_Map {

      public Integer                                 weight          = 0;
      public Float                                   weightPercent   = (float) 0.0;
      public String                                  main            = null;

      @JsonIgnore()
      public Map<String, OWMWeather_Description_Map> descriptionsMap = new HashMap<>();

      public List<OWMWeather_Description_Map>        descriptions    = new ArrayList<>();
   }

   static public class SortOWMWeather_Description_Mapbyweight implements Comparator<OWMWeather_Description_Map>
   {
      // Used for sorting OWMWeather_Description_Map in ascending order of
      // weight number
       @Override
      public int compare(final OWMWeather_Description_Map a, final OWMWeather_Description_Map b)
       {
           return a.weight - b.weight;
       }
   }

   static public class SortOWMWeather_Main_Mapbyweight implements Comparator<OWMWeather_Main_Map> {
      // Used for sorting OWMWeather_Main_Map in ascending order of
      // weight number
      @Override
      public int compare(final OWMWeather_Main_Map a, final OWMWeather_Main_Map b) {
         return a.weight - b.weight;
      }
   }

   static public class WIND_Compute {

      public Float tail   = null;//+tail wind; -headwind [cos(wind°-heading°)*wind speed]
      public Float cross  = null;//+crosswind East; -crosswind West [sin(wind°-heading°)*wind speed]
      public Float gpsDir = null;//GPS direction 0 to 360°
   }
}
