package com.atech.glcamera.utils;

import android.content.Context;

import com.atech.glcamera.filters.BaseFilter;
import com.atech.glcamera.filters.BeautyFilter;
import com.atech.glcamera.filters.BlackCatFilter;
import com.atech.glcamera.filters.BlackFilter;
import com.atech.glcamera.filters.HealthyFilter;
import com.atech.glcamera.filters.BrooklynFilter;
import com.atech.glcamera.filters.CalmFilter;
import com.atech.glcamera.filters.CoolFilter;
import com.atech.glcamera.filters.LatteFilter;
import com.atech.glcamera.filters.AmaroFilter;
import com.atech.glcamera.filters.AntiqueFilter;
import com.atech.glcamera.filters.BrannanFilter;
import com.atech.glcamera.filters.SweetsFilter;
import com.atech.glcamera.filters.WarmFilter;
import com.atech.glcamera.filters.RomanceFilter;
import com.atech.glcamera.filters.SakuraFilter;
import com.atech.glcamera.filters.SkinWhitenFilter;
import com.atech.glcamera.filters.OriginalFilter;
import com.atech.glcamera.filters.SunriseFilter;
import com.atech.glcamera.filters.SunsetFilter;
import com.atech.glcamera.filters.WhiteCatFilter;

public class FilterFactory {

    public enum FilterType {

        SkinWhiten,
        BlackWhite,
        BlackCat,
        WhiteCat,
        Healthy,
        Romance,
        Original,
        Sunrise,
        Sunset,
        Sakura,
        Latte,
        Warm,
        Calm,
        Brooklyn,
        Cool,
        Sweets,
        Amaro,
        Antique,
        Brannan,
        Beauty
    }

    public static BaseFilter createFilter(Context c, FilterType filterType) {

        BaseFilter baseFilter = null;

        switch (filterType) {

            case Sakura:

                baseFilter = new SakuraFilter(c);
                break;
            case Sunset:

                baseFilter = new SunsetFilter(c);
                break;

            case Healthy:

                baseFilter = new HealthyFilter(c);

                break;
            case Romance:

                baseFilter = new RomanceFilter(c);

                break;

            case Sunrise:

                baseFilter = new SunriseFilter(c);

                break;
            case BlackCat:

                baseFilter = new BlackCatFilter(c);

                break;

            case Original:

                baseFilter = new OriginalFilter(c);

                break;

            case WhiteCat:

                baseFilter = new WhiteCatFilter(c);

                break;

            case BlackWhite:

                baseFilter = new BlackFilter(c);

                break;

            case SkinWhiten:

                baseFilter = new SkinWhitenFilter(c);

                break;

            case Calm:

                baseFilter = new CalmFilter(c);
                break;

            case Warm:

                baseFilter = new WarmFilter(c);

                break;
            case Latte:

                baseFilter = new LatteFilter(c);

                break;

            case Cool:

                baseFilter = new CoolFilter(c);
                break;

            case Sweets:

                baseFilter = new SweetsFilter(c);
                break;

            case Brooklyn:

                baseFilter = new BrooklynFilter(c);

                break;
            case Amaro:

                baseFilter = new AmaroFilter(c);
                break;

            case Antique:

                baseFilter = new AntiqueFilter(c);

                break;
            case Brannan:

                baseFilter = new BrannanFilter(c);

                break;
            case Beauty:

                baseFilter = new BeautyFilter(c);

                break;


        }

        return baseFilter;
    }


}
